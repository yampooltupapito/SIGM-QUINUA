package pe.gob.municipalidad.sigm.common.notification.mapper;

import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionEnvioDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionItemDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionPreferenciasDto;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.NotificacionProgramadaDto;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionEnvio;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionPreferencias;
import pe.gob.municipalidad.sigm.common.notification.entity.NotificacionProgramacion;
import pe.gob.municipalidad.sigm.modules.notificaciones.model.Notificacion;

/**
 * Componente Mapper para transformaciones de entidades y DTOs del sistema de notificaciones.
 */
@Component
public class NotificationMapper {

    public NotificacionItemDto toItemDto(Notificacion n) {
        return NotificacionItemDto.fromEntity(n);
    }

    public NotificacionPreferenciasDto toPreferenciasDto(NotificacionPreferencias p) {
        return NotificacionPreferenciasDto.fromEntity(p);
    }

    public NotificacionProgramadaDto toProgramadaDto(NotificacionProgramacion p) {
        return NotificacionProgramadaDto.fromEntity(p);
    }

    public NotificacionEnvioDto toEnvioDto(NotificacionEnvio e) {
        return NotificacionEnvioDto.fromEntity(e);
    }
}
