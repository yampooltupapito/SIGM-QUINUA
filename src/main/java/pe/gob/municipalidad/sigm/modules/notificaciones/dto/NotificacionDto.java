package pe.gob.municipalidad.sigm.modules.notificaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.notificaciones.model.Notificacion;

import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDto {

    private Long id;
    private String titulo;
    private String mensaje;
    private String tipo;
    private boolean leida;
    private String fecha;
    private String urlDestino;
    private String origen;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static NotificacionDto fromEntity(Notificacion entity) {
        if (entity == null) return null;
        return NotificacionDto.builder()
                .id(entity.getId())
                .titulo(entity.getTitulo())
                .mensaje(entity.getMensaje())
                .tipo(entity.getTipo())
                .leida(entity.isLeida())
                .fecha(entity.getFechaCreacion() != null ? entity.getFechaCreacion().format(FORMATTER) : "")
                .urlDestino(entity.getUrlDestino() != null ? entity.getUrlDestino() : "#")
                .origen(entity.getOrigen())
                .build();
    }
}
