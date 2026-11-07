package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.NotificacionCiudadanoDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.SeccionPortalTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.NotificacionTributariaRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Consulta de comunicaciones y notificaciones para el ciudadano.
 * REGLA FUNDAMENTAL: La visualización ciudadana en el portal NO atribuye automáticamente
 * validez legal formal a una notificación, preservando las garantías procesales de ley.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultaNotificacionesCiudadanoService {

    private final PortalTributarioContextService contextService;
    private final NotificacionTributariaRepository notificacionRepository;
    private final PdfNotificacionTributariaService pdfNotificacionTributariaService;

    @Transactional(readOnly = true)
    public List<NotificacionCiudadanoDto> listarMisNotificaciones() {
        Optional<ContribuyenteTributario> contribOpt = contextService.obtenerContribuyenteActivo();
        if (contribOpt.isEmpty()) {
            return Collections.emptyList();
        }

        List<NotificacionTributaria> list = notificacionRepository
                .findByContribuyenteIdOrderByFechaCreacionDesc(contribOpt.get().getId());

        return list.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificacionCiudadanoDto obtenerDetalleNotificacion(UUID notificacionId) {
        // Valida acceso anti-IDOR
        NotificacionTributaria notif = contextService.validarAccesoNotificacion(notificacionId);

        // Registrar auditoría de consulta personal
        contextService.registrarAcceso(SeccionPortalTributario.NOTIFICACIONES);
        log.info("Ciudadano consultó la comunicación tributaria '{}'. Esta lectura técnica no altera la validez legal.",
                notif.getCodigoNotificacion());

        return convertirADto(notif);
    }

    @Transactional(readOnly = true)
    public byte[] descargarCedulaPdf(UUID notificacionId) {
        // Valida acceso anti-IDOR
        NotificacionTributaria notif = contextService.validarAccesoNotificacion(notificacionId);
        String urlVerif = "/verificar/tributaria/" + (notif.getCodigoQr() != null ? notif.getCodigoQr() : notif.getCodigoNotificacion());
        return pdfNotificacionTributariaService.generarDocumentoNotificacion(notif, urlVerif).getPdfBytes();
    }

    private NotificacionCiudadanoDto convertirADto(NotificacionTributaria n) {
        return NotificacionCiudadanoDto.builder()
                .id(n.getId())
                .codigoNotificacion(n.getCodigoNotificacion())
                .tipoNombre(n.getTipoNotificacion() != null ? n.getTipoNotificacion().getNombre() : "Comunicación")
                .categoria(n.getTipoNotificacion() != null ? n.getTipoNotificacion().getCategoria().getNombre() : "INFORMATIVA")
                .asunto(n.getObservaciones())
                .medioEntrega(n.getMedioNotificacion() != null ? n.getMedioNotificacion().getNombre() : "PORTAL")
                .fechaEmision(n.getFechaCreacion())
                .estadoEntrega(n.getEstadoEntrega().getEtiqueta())
                .estadoValidezLegal(n.getEstadoValidezLegal().getEtiqueta())
                .hashDocumento(n.getHashDocumento())
                .formal(n.getTipoNotificacion() != null && Boolean.TRUE.equals(n.getTipoNotificacion().getRequiereDocumento()))
                .tieneDocumentoAdjunto(n.getDocumentoNotificacion() != null || n.getHashDocumento() != null)
                .observaciones(n.getObservaciones())
                .build();
    }
}

