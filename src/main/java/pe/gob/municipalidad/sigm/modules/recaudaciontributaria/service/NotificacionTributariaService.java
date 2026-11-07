package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionTributariaService {

    private final NotificacionTributariaRepository notificacionRepository;
    private final DetalleNotificacionObligacionRepository detalleObligacionRepository;
    private final CargoRecepcionTributariaRepository cargoRepository;
    private final IntentoNotificacionTributariaRepository intentoRepository;
    private final EvidenciaNotificacionTributariaRepository evidenciaRepository;
    private final ControlPlazoNotificacionRepository controlPlazoRepository;
    private final LoteNotificacionesTributariasRepository loteRepository;
    private final PdfNotificacionTributariaService pdfService;
    private final HistorialNotificacionService historialService;

    @Transactional(readOnly = true)
    public Page<NotificacionResponse> listar(Specification<NotificacionTributaria> spec, Pageable pageable) {
        return notificacionRepository.findAll(spec, pageable).map(this::convertirAResponse);
    }

    @Transactional(readOnly = true)
    public NotificacionTributaria obtenerEntidadPorId(UUID id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Notificación tributaria no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public NotificacionResponse obtenerPorId(UUID id) {
        NotificacionTributaria notif = obtenerEntidadPorId(id);
        return convertirAResponse(notif);
    }

    @Transactional(readOnly = true)
    public NotificacionResponse obtenerPorCodigo(String codigo) {
        NotificacionTributaria notif = notificacionRepository.findByCodigoNotificacion(codigo)
                .orElseThrow(() -> new BusinessException("Notificación tributaria no encontrada con código: " + codigo));
        return convertirAResponse(notif);
    }

    @Transactional
    public NotificacionResponse validarNotificacion(UUID id, Usuario usuario) {
        NotificacionTributaria notif = obtenerEntidadPorId(id);

        if (notif.getEstadoAdministrativo() != EstadoAdministrativoNotificacion.BORRADOR &&
            notif.getEstadoAdministrativo() != EstadoAdministrativoNotificacion.OBSERVADA) {
            throw new BusinessException("Solo se pueden validar notificaciones en borrador u observadas.");
        }

        notif.setEstadoAdministrativo(EstadoAdministrativoNotificacion.VALIDADA);
        NotificacionTributaria guardada = notificacionRepository.save(notif);

        historialService.registrarEvento(
                guardada,
                TipoEventoNotificacion.VALIDACION,
                "Notificación validada formalmente por operador",
                EstadoAdministrativoNotificacion.BORRADOR.name(),
                EstadoAdministrativoNotificacion.VALIDADA.name(),
                usuario,
                null
        );

        return convertirAResponse(guardada);
    }

    @Transactional
    public NotificacionResponse autorizarNotificacion(UUID id, String baseUrl, Usuario usuario) {
        NotificacionTributaria notif = obtenerEntidadPorId(id);

        if (notif.getEstadoAdministrativo() == EstadoAdministrativoNotificacion.ANULADA ||
            notif.getEstadoAdministrativo() == EstadoAdministrativoNotificacion.AUTORIZADA ||
            notif.getEstadoAdministrativo() == EstadoAdministrativoNotificacion.EN_GESTION ||
            notif.getEstadoAdministrativo() == EstadoAdministrativoNotificacion.CERRADA) {
            throw new BusinessException("No se puede autorizar una notificación en estado " + notif.getEstadoAdministrativo());
        }

        // Generar URL de verificación QR y hash inmutable
        String urlVerificacion = (baseUrl != null ? baseUrl : "https://quinua.gob.pe") +
                "/verificar/notificacion/" + notif.getCodigoNotificacion();
        notif.setCodigoQr(urlVerificacion);

        // Generar PDF y calcular Hash criptográfico
        PdfNotificacionTributariaService.DocumentoPdfResult pdfResult = pdfService.generarDocumentoNotificacion(notif, urlVerificacion);
        notif.setHashDocumento(pdfResult.getHashSha256());

        notif.setEstadoAdministrativo(EstadoAdministrativoNotificacion.AUTORIZADA);
        if (notif.getEstadoEntrega() == EstadoEntregaNotificacion.NO_PROGRAMADA && notif.getFechaProgramada() != null) {
            notif.setEstadoEntrega(EstadoEntregaNotificacion.PROGRAMADA);
        }

        NotificacionTributaria guardada = notificacionRepository.save(notif);

        historialService.registrarEvento(
                guardada,
                TipoEventoNotificacion.AUTORIZACION,
                "Notificación autorizada y cédula generada con hash SHA-256: " + pdfResult.getHashSha256(),
                null,
                EstadoAdministrativoNotificacion.AUTORIZADA.name(),
                usuario,
                null
        );

        return convertirAResponse(guardada);
    }

    @Transactional
    public byte[] descargarPdfNotificacion(UUID id, String baseUrl) {
        NotificacionTributaria notif = obtenerEntidadPorId(id);
        String urlVerificacion = notif.getCodigoQr();
        if (urlVerificacion == null || urlVerificacion.isBlank()) {
            urlVerificacion = (baseUrl != null ? baseUrl : "https://quinua.gob.pe") +
                    "/verificar/notificacion/" + notif.getCodigoNotificacion();
            notif.setCodigoQr(urlVerificacion);
        }
        PdfNotificacionTributariaService.DocumentoPdfResult res = pdfService.generarDocumentoNotificacion(notif, urlVerificacion);
        if (notif.getHashDocumento() == null) {
            notif.setHashDocumento(res.getHashSha256());
            notificacionRepository.save(notif);
        }
        return res.getPdfBytes();
    }

    @Transactional
    public NotificacionResponse anularNotificacion(UUID id, String motivo, Usuario usuario) {
        NotificacionTributaria notif = obtenerEntidadPorId(id);

        if (notif.getEstadoAdministrativo() == EstadoAdministrativoNotificacion.ANULADA) {
            throw new BusinessException("La notificación ya se encuentra anulada.");
        }
        if (notif.getEstadoAdministrativo() == EstadoAdministrativoNotificacion.CERRADA) {
            throw new BusinessException("No se puede anular un procedimiento de notificación ya cerrado.");
        }

        notif.setEstadoAdministrativo(EstadoAdministrativoNotificacion.ANULADA);
        notif.setMotivoAnulacion(motivo);
        NotificacionTributaria guardada = notificacionRepository.save(notif);

        historialService.registrarEvento(
                guardada,
                TipoEventoNotificacion.ANULACION,
                "Notificación anulada. Motivo: " + motivo,
                null,
                "ANULADA: " + motivo,
                usuario,
                null
        );

        return convertirAResponse(guardada);
    }

    @Transactional(readOnly = true)
    public VerificacionPublicaNotificacionResponse verificarPublicamente(String codigo) {
        Optional<NotificacionTributaria> opt = notificacionRepository.findByCodigoNotificacion(codigo);
        if (opt.isEmpty()) {
            return VerificacionPublicaNotificacionResponse.builder()
                    .existe(false)
                    .codigoNotificacion(codigo)
                    .mensajeVerificacion("El código consultado no corresponde a ninguna notificación emitida por la Municipalidad Distrital de Quinua.")
                    .esValido(false)
                    .build();
        }

        NotificacionTributaria notif = opt.get();

        // Minimización de datos personales: anonimizar nombre y enmascarar documento
        String nombreAnonimizado = anonimizarNombre(notif.getDestinatarioNombre());
        String docEnmascarado = enmascararDocumento(notif.getDestinatarioNumeroDoc());

        boolean valida = notif.getEstadoAdministrativo() != EstadoAdministrativoNotificacion.ANULADA &&
                         notif.getEstadoValidezLegal() != EstadoValidezLegalNotificacion.INVALIDADA;

        return VerificacionPublicaNotificacionResponse.builder()
                .existe(true)
                .codigoNotificacion(notif.getCodigoNotificacion())
                .tipoDocumento(notif.getTipoNotificacion().getNombre())
                .categoria(notif.getTipoNotificacion().getCategoria().getNombre())
                .destinatarioAnonimizado(nombreAnonimizado)
                .numeroDocEnmascarado(docEnmascarado)
                .fechaEmision(notif.getFechaCreacion())
                .estadoValidezLegal(notif.getEstadoValidezLegal().getEtiqueta())
                .estadoEntrega(notif.getEstadoEntrega().getEtiqueta())
                .entidadEmisora("Municipalidad Distrital de Quinua")
                .hashDocumento(notif.getHashDocumento() != null ? notif.getHashDocumento() : "PENDIENTE")
                .mensajeVerificacion(valida ? "Documento municipal auténtico registrado en el SIGM-QUINUA." : "Documento anulado o no válido.")
                .esValido(valida)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardNotificacionResponse obtenerDashboardMetrics() {
        long totalNotif = notificacionRepository.countTotalNotificaciones();
        long borrador = notificacionRepository.countByEstadoAdministrativo(EstadoAdministrativoNotificacion.BORRADOR);
        long pendVal = notificacionRepository.countByEstadoAdministrativo(EstadoAdministrativoNotificacion.PENDIENTE_VALIDACION);
        long autorizadas = notificacionRepository.countByEstadoAdministrativo(EstadoAdministrativoNotificacion.AUTORIZADA);
        long enGestion = notificacionRepository.countByEstadoAdministrativo(EstadoAdministrativoNotificacion.EN_GESTION);
        long anuladas = notificacionRepository.countByEstadoAdministrativo(EstadoAdministrativoNotificacion.ANULADA);

        long programadas = notificacionRepository.countByEstadoEntrega(EstadoEntregaNotificacion.PROGRAMADA);
        long enProceso = notificacionRepository.countByEstadoEntrega(EstadoEntregaNotificacion.EN_PROCESO);
        long entregadas = notificacionRepository.countByEstadoEntrega(EstadoEntregaNotificacion.ENTREGADA_CON_EVIDENCIA);
        long noEntregadas = notificacionRepository.countByEstadoEntrega(EstadoEntregaNotificacion.NO_ENTREGADA);

        long pendRevisionLegal = notificacionRepository.countByEstadoValidezLegal(EstadoValidezLegalNotificacion.PENDIENTE_VERIFICACION);
        long validadasLegal = notificacionRepository.countByEstadoValidezLegal(EstadoValidezLegalNotificacion.VALIDADA_POR_RESPONSABLE);
        long observadas = notificacionRepository.countByEstadoValidezLegal(EstadoValidezLegalNotificacion.OBSERVADA);

        long totalLotes = loteRepository.countTotalLotes();
        long lotesEnGestion = loteRepository.countByEstado(EstadoLoteNotificaciones.EN_GESTION);

        return DashboardNotificacionResponse.builder()
                .totalNotificaciones(totalNotif)
                .totalBorrador(borrador)
                .totalPendientesValidacion(pendVal)
                .totalAutorizadas(autorizadas)
                .totalEnGestion(enGestion)
                .totalProgramadas(programadas)
                .totalEnProceso(enProceso)
                .totalEntregadasConEvidencia(entregadas)
                .totalNoEntregadas(noEntregadas)
                .totalPendientesRevisionLegal(pendRevisionLegal)
                .totalValidadasLegalmente(validadasLegal)
                .totalObservadas(observadas)
                .totalAnuladas(anuladas)
                .totalLotes(totalLotes)
                .totalLotesEnGestion(lotesEnGestion)
                .build();
    }

    public NotificacionResponse convertirAResponse(NotificacionTributaria notif) {
        BigDecimal totalOblig = BigDecimal.ZERO;
        List<DetalleNotificacionObligacion> detalles = detalleObligacionRepository.findByNotificacionId(notif.getId());
        for (DetalleNotificacionObligacion d : detalles) {
            if (d.getMontoTotal() != null) {
                totalOblig = totalOblig.add(d.getMontoTotal());
            }
        }

        Optional<CargoRecepcionTributaria> cargoOpt = cargoRepository.findByNotificacionId(notif.getId());
        long totalIntentos = intentoRepository.countByNotificacionId(notif.getId());
        long totalEvidencias = evidenciaRepository.countByNotificacionId(notif.getId());
        Optional<ControlPlazoNotificacion> plazoOpt = controlPlazoRepository.findByNotificacionId(notif.getId());

        String contribNombre = notif.getContribuyente().getPersona() != null
                ? notif.getContribuyente().getPersona().getNombreCompleto()
                : notif.getContribuyente().getCodigoContribuyente();
        String contribDoc = notif.getContribuyente().getPersona() != null
                ? notif.getContribuyente().getPersona().getNumeroDocumento()
                : notif.getContribuyente().getCodigoContribuyente();

        return NotificacionResponse.builder()
                .id(notif.getId())
                .codigoNotificacion(notif.getCodigoNotificacion())
                .tipoNotificacionId(notif.getTipoNotificacion().getId())
                .tipoNotificacionCodigo(notif.getTipoNotificacion().getCodigo())
                .tipoNotificacionNombre(notif.getTipoNotificacion().getNombre())
                .categoria(notif.getTipoNotificacion().getCategoria())
                .requiereDocumento(notif.getTipoNotificacion().getRequiereDocumento())
                .requiereValidacionLegal(notif.getTipoNotificacion().getRequiereValidacionLegal())
                .requiereEvidencia(notif.getTipoNotificacion().getRequiereEvidencia())
                .contribuyenteId(notif.getContribuyente().getId())
                .contribuyenteNombre(contribNombre)
                .contribuyenteNumeroDoc(contribDoc)
                .destinatarioNombre(notif.getDestinatarioNombre())
                .destinatarioNumeroDoc(notif.getDestinatarioNumeroDoc())
                .representanteNombre(notif.getRepresentante() != null ? notif.getRepresentante().getNombresApellidos() : null)
                .domicilioNotificacion(notif.getDomicilioNotificacion())
                .referenciaUbicacion(notif.getReferenciaUbicacion())
                .medioNotificacionId(notif.getMedioNotificacion().getId())
                .medioNotificacionCodigo(notif.getMedioNotificacion().getCodigo())
                .medioNotificacionNombre(notif.getMedioNotificacion().getNombre())
                .esMedioFormal(notif.getMedioNotificacion().getEsMedioFormal())
                .documentoOrigenId(notif.getDocumentoOrigen() != null ? notif.getDocumentoOrigen().getId() : null)
                .documentoOrigenNumero(notif.getDocumentoOrigen() != null ? notif.getDocumentoOrigen().getNombreOriginal() : null)
                .documentoNotificacionId(notif.getDocumentoNotificacion() != null ? notif.getDocumentoNotificacion().getId() : null)
                .hashDocumento(notif.getHashDocumento())
                .codigoQr(notif.getCodigoQr())
                .obligacionId(notif.getObligacion() != null ? notif.getObligacion().getId() : null)
                .obligacionCodigo(notif.getObligacion() != null ? notif.getObligacion().getCodigoObligacion() : null)
                .convenioId(notif.getConvenio() != null ? notif.getConvenio().getId() : null)
                .convenioCodigo(notif.getConvenio() != null ? notif.getConvenio().getCodigoConvenio() : null)
                .seguimientoDeudaId(notif.getSeguimientoDeuda() != null ? notif.getSeguimientoDeuda().getId() : null)
                .loteId(notif.getLote() != null ? notif.getLote().getId() : null)
                .loteCodigo(notif.getLote() != null ? notif.getLote().getCodigo() : null)
                .fechaCreacion(notif.getFechaCreacion())
                .fechaProgramada(notif.getFechaProgramada())
                .estadoAdministrativo(notif.getEstadoAdministrativo())
                .estadoEntrega(notif.getEstadoEntrega())
                .estadoValidezLegal(notif.getEstadoValidezLegal())
                .oficinaResponsableId(notif.getOficinaResponsable() != null ? notif.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(notif.getOficinaResponsable() != null ? notif.getOficinaResponsable().getNombre() : null)
                .usuarioResponsableId(notif.getUsuarioResponsable() != null ? notif.getUsuarioResponsable().getId() : null)
                .usuarioResponsableNombre(notif.getUsuarioResponsable() != null ? notif.getUsuarioResponsable().getNombreCompleto() : null)
                .observaciones(notif.getObservaciones())
                .motivoAnulacion(notif.getMotivoAnulacion())
                .montoTotalObligaciones(totalOblig)
                .tieneCargo(cargoOpt.isPresent())
                .receptorCargo(cargoOpt.map(CargoRecepcionTributaria::getReceptorIdentificado).orElse(null))
                .fechaRecepcionCargo(cargoOpt.map(CargoRecepcionTributaria::getFechaRecepcionDeclarada).orElse(null))
                .totalIntentos((int) totalIntentos)
                .totalEvidencias((int) totalEvidencias)
                .plazoEstado(plazoOpt.map(p -> p.getEstadoPlazo().getEtiqueta()).orElse(null))
                .plazoVencimiento(plazoOpt.map(ControlPlazoNotificacion::getFechaVencimiento).orElse(null))
                .plazoDiasRestantes(plazoOpt.map(ControlPlazoNotificacion::getDiasRestantes).orElse(null))
                .build();
    }

    private String anonimizarNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) return "***";
        String[] partes = nombreCompleto.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (!p.isEmpty()) {
                sb.append(p.charAt(0)).append("*** ");
            }
        }
        return sb.toString().trim();
    }

    private String enmascararDocumento(String numDoc) {
        if (numDoc == null || numDoc.length() < 4) return "****";
        return numDoc.substring(0, 2) + "****" + numDoc.substring(numDoc.length() - 2);
    }
}

