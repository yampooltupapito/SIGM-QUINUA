package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion.ValidarProcedimientoRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ValidacionProcedimientoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAdministrativoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoValidezLegalNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ResultadoValidacionProcedimental;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoEventoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.NotificacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ValidacionProcedimientoNotificacionRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidacionNotificacionService {

    private final ValidacionProcedimientoNotificacionRepository validacionRepository;
    private final NotificacionTributariaRepository notificacionRepository;
    private final HistorialNotificacionService historialService;
    private final PlazoNotificacionService plazoService;

    @Transactional
    public ValidacionProcedimientoNotificacion calificarProcedimiento(UUID notificacionId,
                                                                      ValidarProcedimientoRequest request,
                                                                      Usuario revisor) {
        NotificacionTributaria notif = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new BusinessException("Notificación no encontrada con ID: " + notificacionId));

        if (revisor == null) {
            throw new BusinessException("Se requiere un funcionario calificado como revisor responsable.");
        }

        // Si se pretende declarar CONFORME_VALIDA, se exige el cumplimiento de requisitos mínimos
        if (request.getResultado() == ResultadoValidacionProcedimental.CONFORME_VALIDA) {
            if (Boolean.FALSE.equals(request.getCumpleDestinatarioIdentificado()) ||
                Boolean.FALSE.equals(request.getCumpleDomicilioValido()) ||
                Boolean.FALSE.equals(request.getCumpleEvidenciaMinima())) {
                throw new BusinessException("No puede declararse formalmente válida si no cumple los requisitos indispensables (Destinatario, Domicilio y Evidencia).");
            }
        }

        ValidacionProcedimientoNotificacion val = ValidacionProcedimientoNotificacion.builder()
                .notificacion(notif)
                .revisor(revisor)
                .fechaValidacion(OffsetDateTime.now())
                .resultado(request.getResultado())
                .sustento(request.getSustento())
                .cumpleDestinatarioIdentificado(Boolean.TRUE.equals(request.getCumpleDestinatarioIdentificado()))
                .cumpleDomicilioValido(Boolean.TRUE.equals(request.getCumpleDomicilioValido()))
                .cumpleDocumentoAdjunto(Boolean.TRUE.equals(request.getCumpleDocumentoAdjunto()))
                .cumplePlazoProcedimental(Boolean.TRUE.equals(request.getCumplePlazoProcedimental()))
                .cumpleEvidenciaMinima(Boolean.TRUE.equals(request.getCumpleEvidenciaMinima()))
                .cumpleDatosReceptorYCargo(Boolean.TRUE.equals(request.getCumpleDatosReceptorYCargo()))
                .observaciones(request.getObservaciones())
                .build();
        val.setCreatedBy(revisor.getUsername());

        ValidacionProcedimientoNotificacion guardada = validacionRepository.save(val);

        // Actualizar estado de validez legal en NotificacionTributaria
        TipoEventoNotificacion tipoEvento;
        if (request.getResultado() == ResultadoValidacionProcedimental.CONFORME_VALIDA) {
            notif.setEstadoValidezLegal(EstadoValidezLegalNotificacion.VALIDADA_POR_RESPONSABLE);
            tipoEvento = TipoEventoNotificacion.VALIDACION_LEGAL;

            // Iniciar cómputo legal del plazo si corresponde
            if (notif.getTipoNotificacion().getCodigo().contains("ORDEN_PAGO")) {
                plazoService.registrarOActualizarPlazo(notif, "Plazo de Pago de Orden de Pago (3 días hábiles)", true, 3, LocalDate.now(), revisor);
            } else if (notif.getTipoNotificacion().getCodigo().contains("RESOLUCION")) {
                plazoService.registrarOActualizarPlazo(notif, "Plazo de Impugnación / Reclamación (20 días hábiles)", true, 20, LocalDate.now(), revisor);
            }
        } else if (request.getResultado() == ResultadoValidacionProcedimental.OBSERVADA_SUBSANABLE) {
            notif.setEstadoValidezLegal(EstadoValidezLegalNotificacion.OBSERVADA);
            tipoEvento = TipoEventoNotificacion.OBSERVACION_LEGAL;
        } else {
            notif.setEstadoValidezLegal(EstadoValidezLegalNotificacion.INVALIDADA);
            tipoEvento = TipoEventoNotificacion.INVALIDACION_LEGAL;
        }

        notificacionRepository.save(notif);

        historialService.registrarEvento(
                notif,
                tipoEvento,
                "Calificación legal procedimental: " + request.getResultado().getEtiqueta() +
                        ". Sustento: " + request.getSustento(),
                null,
                request.getResultado().name(),
                revisor,
                null
        );

        return guardada;
    }

    @Transactional(readOnly = true)
    public List<ValidacionProcedimientoNotificacion> listarPorNotificacion(UUID notificacionId) {
        return validacionRepository.findByNotificacionIdOrderByFechaValidacionDesc(notificacionId);
    }
}

