package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion.CrearLoteNotificacionesRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion.CrearNotificacionRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAdministrativoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoLoteNotificaciones;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoteNotificacionesService {

    private final LoteNotificacionesTributariasRepository loteRepository;
    private final TipoNotificacionTributariaRepository tipoRepository;
    private final MedioNotificacionTributariaRepository medioRepository;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final DomicilioContribuyenteRepository domicilioRepository;
    private final ObligacionTributariaRepository obligacionRepository;
    private final NotificacionTributariaRepository notificacionRepository;
    private final PreparacionNotificacionService preparacionService;

    @Transactional
    public LoteNotificacionesTributarias crearLote(CrearLoteNotificacionesRequest request,
                                                   Usuario operador,
                                                   Oficina oficina) {
        TipoNotificacionTributaria tipo = tipoRepository.findById(request.getTipoNotificacionId())
                .orElseThrow(() -> new BusinessException("Tipo de notificación no válido: " + request.getTipoNotificacionId()));

        MedioNotificacionTributaria medio = medioRepository.findById(request.getMedioNotificacionId())
                .orElseThrow(() -> new BusinessException("Medio de notificación no válido: " + request.getMedioNotificacionId()));

        String codigoLote = generarCodigoLote();

        LoteNotificacionesTributarias lote = LoteNotificacionesTributarias.builder()
                .codigo(codigoLote)
                .tipoNotificacion(tipo)
                .fechaCreacion(OffsetDateTime.now())
                .estado(EstadoLoteNotificaciones.BORRADOR)
                .totalRegistros(0)
                .totalValidos(0)
                .totalObservados(0)
                .responsable(operador)
                .oficina(oficina)
                .descripcion(request.getDescripcion())
                .build();
        lote.setCreatedBy(operador != null ? operador.getUsername() : "SISTEMA");

        LoteNotificacionesTributarias guardado = loteRepository.save(lote);

        int validos = 0;
        int observados = 0;

        // Si se enviaron contribuyentes seleccionados
        List<UUID> contribIds = request.getContribuyentesIds();
        if (contribIds != null && !contribIds.isEmpty()) {
            for (UUID cId : contribIds) {
                try {
                    ContribuyenteTributario c = contribuyenteRepository.findById(cId).orElse(null);
                    if (c == null) {
                        observados++;
                        continue;
                    }

                    // Resolver domicilio válido
                    DomicilioContribuyente dom = domicilioRepository.findByContribuyenteIdAndEsFiscalTrue(c.getId())
                            .orElseGet(() -> {
                                List<DomicilioContribuyente> list = domicilioRepository.findByContribuyenteIdOrderByCreatedAtDesc(c.getId());
                                return list.isEmpty() ? null : list.get(0);
                            });
                    String direccion = (dom != null && dom.getDireccionCompleta() != null)
                            ? dom.getDireccionCompleta()
                            : "Domicilio fiscal de " + c.getCodigoContribuyente() + " (Quinua Centro)";

                    String nombreDest = c.getPersona() != null ? c.getPersona().getNombreCompleto() : c.getCodigoContribuyente();
                    String numDoc = c.getPersona() != null ? c.getPersona().getNumeroDocumento() : c.getCodigoContribuyente();

                    CrearNotificacionRequest notifReq = CrearNotificacionRequest.builder()
                            .tipoNotificacionId(tipo.getId())
                            .contribuyenteId(c.getId())
                            .destinatarioNombre(nombreDest)
                            .destinatarioNumeroDoc(numDoc)
                            .domicilioNotificacion(direccion)
                            .medioNotificacionId(medio.getId())
                            .observaciones("Notificación generada mediante lote masivo " + guardado.getCodigo())
                            .build();

                    NotificacionTributaria notif = preparacionService.prepararNotificacion(notifReq, operador, oficina);
                    notif.setLote(guardado);
                    notificacionRepository.save(notif);
                    validos++;
                } catch (Exception e) {
                    log.warn("Error al procesar registro en lote para contribuyente {}: {}", cId, e.getMessage());
                    observados++;
                }
            }
        }

        guardado.setTotalRegistros(validos + observados);
        guardado.setTotalValidos(validos);
        guardado.setTotalObservados(observados);
        return loteRepository.save(guardado);
    }

    @Transactional
    public LoteNotificacionesTributarias autorizarLote(UUID loteId, Usuario operador) {
        LoteNotificacionesTributarias lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new BusinessException("Lote no encontrado con ID: " + loteId));

        if (lote.getEstado() == EstadoLoteNotificaciones.AUTORIZADO ||
            lote.getEstado() == EstadoLoteNotificaciones.EN_GESTION ||
            lote.getEstado() == EstadoLoteNotificaciones.CERRADO) {
            throw new BusinessException("El lote ya se encuentra en estado: " + lote.getEstado());
        }

        List<NotificacionTributaria> notificaciones = notificacionRepository.findByLoteId(loteId);
        for (NotificacionTributaria n : notificaciones) {
            if (n.getEstadoAdministrativo() == EstadoAdministrativoNotificacion.BORRADOR ||
                n.getEstadoAdministrativo() == EstadoAdministrativoNotificacion.VALIDADA) {
                n.setEstadoAdministrativo(EstadoAdministrativoNotificacion.AUTORIZADA);
                notificacionRepository.save(n);
            }
        }

        lote.setEstado(EstadoLoteNotificaciones.AUTORIZADO);
        return loteRepository.save(lote);
    }

    @Transactional(readOnly = true)
    public Page<LoteNotificacionesTributarias> listarLotes(Pageable pageable) {
        return loteRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public LoteNotificacionesTributarias obtenerPorId(UUID id) {
        return loteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Lote no encontrado con ID: " + id));
    }

    private synchronized String generarCodigoLote() {
        int anio = LocalDate.now().getYear();
        long count = loteRepository.countTotalLotes() + 1;
        String codigo = String.format("LOTE-NOTIF-%d-%04d", anio, count);
        while (loteRepository.existsByCodigo(codigo)) {
            count++;
            codigo = String.format("LOTE-NOTIF-%d-%04d", anio, count);
        }
        return codigo;
    }
}

