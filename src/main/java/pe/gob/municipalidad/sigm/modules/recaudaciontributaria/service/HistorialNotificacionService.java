package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.HistorialNotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoEventoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.HistorialNotificacionTributariaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistorialNotificacionService {

    private final HistorialNotificacionTributariaRepository historialRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void registrarEvento(NotificacionTributaria notificacion,
                                TipoEventoNotificacion tipoEvento,
                                String descripcion,
                                String datosAnteriores,
                                String datosNuevos,
                                Usuario usuario,
                                String ipOrigen) {
        HistorialNotificacionTributaria evento = HistorialNotificacionTributaria.builder()
                .notificacion(notificacion)
                .tipoEvento(tipoEvento)
                .descripcion(descripcion)
                .datosAnteriores(datosAnteriores)
                .datosNuevos(datosNuevos)
                .usuario(usuario)
                .fechaEvento(OffsetDateTime.now())
                .ipOrigen(ipOrigen != null ? ipOrigen : "127.0.0.1")
                .build();
        historialRepository.save(evento);
        log.info("Evento auditado en Notificación {}: {} por {}", notificacion.getCodigoNotificacion(), tipoEvento,
                usuario != null ? usuario.getUsername() : "SISTEMA");
    }

    @Transactional(readOnly = true)
    public List<HistorialNotificacionTributaria> listarEventos(UUID notificacionId) {
        return historialRepository.findByNotificacionIdOrderByFechaEventoAsc(notificacionId);
    }
}

