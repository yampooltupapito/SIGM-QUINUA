package pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.administracion.entity.AuditoriaAdministrativa;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * DTO para elementos de la tabla de actividad reciente en el dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardActivityDto {

    private Long id;
    private String fecha;
    private String usuarioResponsable;
    private String accion;
    private String entidadAfectada;
    private Long entidadId;
    private String resultado;
    private String detalle;
    private String ipOrigen;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.of("America/Lima"));

    public static DashboardActivityDto fromEntity(AuditoriaAdministrativa entity) {
        if (entity == null) return null;
        return DashboardActivityDto.builder()
                .id(entity.getId())
                .fecha(entity.getCreatedAt() != null ? FORMATTER.format(entity.getCreatedAt()) : "")
                .usuarioResponsable(entity.getUsuarioResponsable())
                .accion(entity.getAccion())
                .entidadAfectada(entity.getEntidadAfectada())
                .entidadId(entity.getEntidadId())
                .resultado(entity.getResultado() != null ? entity.getResultado() : "EXITO")
                .detalle(entity.getDetalle())
                .ipOrigen(entity.getIpOrigen())
                .build();
    }
}
