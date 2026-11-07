package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion.CrearNotificacionRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAdministrativoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoEntregaNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoValidezLegalNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoEventoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreparacionNotificacionService {

    private final NotificacionTributariaRepository notificacionRepository;
    private final TipoNotificacionTributariaRepository tipoRepository;
    private final MedioNotificacionTributariaRepository medioRepository;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final RepresentanteContribuyenteRepository representanteRepository;
    private final DomicilioContribuyenteRepository domicilioRepository;
    private final ObligacionTributariaRepository obligacionRepository;
    private final ConvenioFraccionamientoTributarioRepository convenioRepository;
    private final SeguimientoDeudaTributariaRepository seguimientoDeudaRepository;
    private final DocumentoRepository documentoRepository;
    private final DetalleNotificacionObligacionRepository detalleObligacionRepository;
    private final HistorialNotificacionService historialService;

    @Transactional
    public NotificacionTributaria prepararNotificacion(CrearNotificacionRequest request,
                                                       Usuario operador,
                                                       Oficina oficina) {
        // 1. Control de Idempotencia
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<NotificacionTributaria> existente = notificacionRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existente.isPresent()) {
                log.info("Idempotencia aplicada para notificación con clave {}", request.getIdempotencyKey());
                return existente.get();
            }
        }

        // 2. Validar Tipo de Notificación
        TipoNotificacionTributaria tipo = tipoRepository.findById(request.getTipoNotificacionId())
                .orElseThrow(() -> new BusinessException("Tipo de notificación no válido: " + request.getTipoNotificacionId()));

        // 3. Validar Contribuyente
        ContribuyenteTributario contribuyente = contribuyenteRepository.findById(request.getContribuyenteId())
                .orElseThrow(() -> new BusinessException("Contribuyente no encontrado con ID: " + request.getContribuyenteId()));

        // 4. Validar Representante si se especificó
        RepresentanteContribuyente representante = null;
        if (request.getRepresentanteId() != null) {
            representante = representanteRepository.findById(request.getRepresentanteId())
                    .orElseThrow(() -> new BusinessException("Representante legal no encontrado con ID: " + request.getRepresentanteId()));
        }

        // 5. Validar Medio de Notificación
        MedioNotificacionTributaria medio = medioRepository.findById(request.getMedioNotificacionId())
                .orElseThrow(() -> new BusinessException("Medio de notificación no válido: " + request.getMedioNotificacionId()));

        if (Boolean.TRUE.equals(medio.getRequiereDireccionFisica()) &&
            (request.getDomicilioNotificacion() == null || request.getDomicilioNotificacion().isBlank())) {
            throw new BusinessException("El medio seleccionado requiere obligatoriamente un domicilio físico válido para notificación.");
        }

        // 6. Validar Documento de Origen si el tipo lo requiere
        Documento documentoOrigen = null;
        if (request.getDocumentoOrigenId() != null) {
            documentoOrigen = documentoRepository.findById(request.getDocumentoOrigenId()).orElse(null);
        }
        if (Boolean.TRUE.equals(tipo.getRequiereDocumento()) && documentoOrigen == null &&
            request.getObligacionId() == null && request.getConvenioId() == null) {
            throw new BusinessException("Este tipo de notificación formal exige vincular un documento de origen, obligación o resolución.");
        }

        // 7. Generar Código Institucional Único
        String codigoNotif = generarCodigoNotificacion();

        // 8. Vincular Obligación, Convenio o Seguimiento si existen
        ObligacionTributaria obligacion = null;
        if (request.getObligacionId() != null) {
            obligacion = obligacionRepository.findById(request.getObligacionId()).orElse(null);
        }

        ConvenioFraccionamientoTributario convenio = null;
        if (request.getConvenioId() != null) {
            convenio = convenioRepository.findById(request.getConvenioId()).orElse(null);
        }

        SeguimientoDeudaTributaria seguimiento = null;
        if (request.getSeguimientoDeudaId() != null) {
            seguimiento = seguimientoDeudaRepository.findById(request.getSeguimientoDeudaId()).orElse(null);
        }

        // 9. Construir Entidad Principal
        NotificacionTributaria notificacion = NotificacionTributaria.builder()
                .codigoNotificacion(codigoNotif)
                .tipoNotificacion(tipo)
                .contribuyente(contribuyente)
                .destinatarioNombre(request.getDestinatarioNombre().trim())
                .destinatarioNumeroDoc(request.getDestinatarioNumeroDoc().trim())
                .representante(representante)
                .domicilioNotificacion(request.getDomicilioNotificacion().trim())
                .referenciaUbicacion(request.getReferenciaUbicacion())
                .medioNotificacion(medio)
                .documentoOrigen(documentoOrigen)
                .obligacion(obligacion)
                .convenio(convenio)
                .seguimientoDeuda(seguimiento)
                .fechaCreacion(OffsetDateTime.now())
                .fechaProgramada(request.getFechaProgramada())
                .estadoAdministrativo(EstadoAdministrativoNotificacion.BORRADOR)
                .estadoEntrega(request.getFechaProgramada() != null ? EstadoEntregaNotificacion.PROGRAMADA : EstadoEntregaNotificacion.NO_PROGRAMADA)
                .estadoValidezLegal(tipo.getRequiereValidacionLegal() ? EstadoValidezLegalNotificacion.PENDIENTE_VERIFICACION : EstadoValidezLegalNotificacion.NO_APLICA)
                .oficinaResponsable(oficina)
                .usuarioResponsable(operador)
                .idempotencyKey(request.getIdempotencyKey())
                .observaciones(request.getObservaciones())
                .build();
        notificacion.setCreatedBy(operador != null ? operador.getUsername() : "SISTEMA");

        NotificacionTributaria guardada = notificacionRepository.save(notificacion);

        // 10. Vincular detalle de obligaciones múltiples si fueron proporcionadas
        List<UUID> oblIds = request.getObligacionesIds();
        if (oblIds != null && !oblIds.isEmpty()) {
            for (UUID oblId : oblIds) {
                obligacionRepository.findById(oblId).ifPresent(ob -> {
                    BigDecimal insoluto = ob.getSaldo() != null ? ob.getSaldo() : (ob.getImporteDeterminado() != null ? ob.getImporteDeterminado() : BigDecimal.ZERO);
                    BigDecimal interes = BigDecimal.ZERO;
                    BigDecimal total = insoluto.add(interes);

                    DetalleNotificacionObligacion det = DetalleNotificacionObligacion.builder()
                            .notificacion(guardada)
                            .obligacion(ob)
                            .periodo(ob.getPeriodo() != null ? ob.getPeriodo().getAnio() + " - C" + ob.getCuotaNumero() : "Cuota " + ob.getCuotaNumero())
                            .montoInsoluto(insoluto)
                            .montoInteres(interes)
                            .montoTotal(total)
                            .build();
                    det.setCreatedBy(operador != null ? operador.getUsername() : "SISTEMA");
                    detalleObligacionRepository.save(det);
                });
            }
        } else if (obligacion != null) {
            BigDecimal insoluto = obligacion.getSaldo() != null ? obligacion.getSaldo() : (obligacion.getImporteDeterminado() != null ? obligacion.getImporteDeterminado() : BigDecimal.ZERO);
            BigDecimal interes = BigDecimal.ZERO;
            BigDecimal total = insoluto.add(interes);

            DetalleNotificacionObligacion det = DetalleNotificacionObligacion.builder()
                    .notificacion(guardada)
                    .obligacion(obligacion)
                    .periodo(obligacion.getPeriodo() != null ? obligacion.getPeriodo().getAnio() + " - C" + obligacion.getCuotaNumero() : "Cuota " + obligacion.getCuotaNumero())
                    .montoInsoluto(insoluto)
                    .montoInteres(interes)
                    .montoTotal(total)
                    .build();
            det.setCreatedBy(operador != null ? operador.getUsername() : "SISTEMA");
            detalleObligacionRepository.save(det);
        }

        historialService.registrarEvento(
                guardada,
                TipoEventoNotificacion.CREACION,
                "Notificación preparada en borrador para destinatario: " + guardada.getDestinatarioNombre() +
                        " (" + guardada.getDestinatarioNumeroDoc() + ") en domicilio: " + guardada.getDomicilioNotificacion(),
                null,
                guardada.getCodigoNotificacion(),
                operador,
                null
        );

        return guardada;
    }

    private synchronized String generarCodigoNotificacion() {
        int anio = LocalDate.now().getYear();
        long total = notificacionRepository.countTotalNotificaciones() + 1;
        String codigo = String.format("NOTIF-%d-%06d", anio, total);
        while (notificacionRepository.existsByCodigoNotificacion(codigo)) {
            total++;
            codigo = String.format("NOTIF-%d-%06d", anio, total);
        }
        return codigo;
    }
}

