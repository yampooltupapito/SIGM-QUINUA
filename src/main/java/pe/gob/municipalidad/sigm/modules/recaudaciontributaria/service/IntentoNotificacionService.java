package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion.RegistrarIntentoRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.IntentoNotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoEntregaNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ResultadoIntentoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoEventoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.IntentoNotificacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.NotificacionTributariaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntentoNotificacionService {

    private final IntentoNotificacionTributariaRepository intentoRepository;
    private final NotificacionTributariaRepository notificacionRepository;
    private final HistorialNotificacionService historialService;

    @Transactional
    public IntentoNotificacionTributaria registrarIntento(UUID notificacionId,
                                                          RegistrarIntentoRequest request,
                                                          Usuario notificador) {
        NotificacionTributaria notif = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new BusinessException("Notificación no encontrada con ID: " + notificacionId));

        OffsetDateTime fechaHora = request.getFechaHoraIntento() != null
                ? request.getFechaHoraIntento() : OffsetDateTime.now();

        IntentoNotificacionTributaria intento = IntentoNotificacionTributaria.builder()
                .notificacion(notif)
                .notificador(notificador)
                .fechaHoraIntento(fechaHora)
                .medioUtilizado(request.getMedioUtilizado())
                .resultado(request.getResultado())
                .ubicacionReferencia(request.getUbicacionReferencia())
                .observaciones(request.getObservaciones())
                .build();
        intento.setCreatedBy(notificador != null ? notificador.getUsername() : "SISTEMA");

        IntentoNotificacionTributaria guardado = intentoRepository.save(intento);

        // Actualizar estado de entrega según resultado fáctico
        if (request.getResultado() == ResultadoIntentoNotificacion.ENTREGA_EFECTIVA) {
            notif.setEstadoEntrega(EstadoEntregaNotificacion.ENTREGADA_CON_EVIDENCIA);
        } else if (request.getResultado() == ResultadoIntentoNotificacion.DESTINATARIO_NO_UBICADO ||
                   request.getResultado() == ResultadoIntentoNotificacion.RECEPCION_RECHAZADA) {
            notif.setEstadoEntrega(EstadoEntregaNotificacion.INTENTO_REGISTRADO);
        } else if (request.getResultado() == ResultadoIntentoNotificacion.DIRECCION_OBSERVADA) {
            notif.setEstadoEntrega(EstadoEntregaNotificacion.DEVUELTA);
        } else {
            notif.setEstadoEntrega(EstadoEntregaNotificacion.INTENTO_REGISTRADO);
        }
        notificacionRepository.save(notif);

        historialService.registrarEvento(
                notif,
                TipoEventoNotificacion.REGISTRO_INTENTO,
                "Intento registrado: " + request.getResultado().getEtiqueta() +
                        " por notificador " + (notificador != null ? notificador.getUsername() : "DESCONOCIDO") +
                        " (" + request.getObservaciones() + ")",
                null,
                request.getResultado().name(),
                notificador,
                null
        );

        return guardado;
    }

    @Transactional(readOnly = true)
    public List<IntentoNotificacionTributaria> listarPorNotificacion(UUID notificacionId) {
        return intentoRepository.findByNotificacionIdOrderByFechaHoraIntentoDesc(notificacionId);
    }
}

