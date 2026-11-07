package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearLoteNotificacionesRequest {

    @NotNull(message = "El tipo de notificación es obligatorio")
    private UUID tipoNotificacionId;

    @NotNull(message = "El medio de notificación es obligatorio")
    private UUID medioNotificacionId;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    private List<UUID> contribuyentesIds;
    private List<UUID> obligacionesIds;
}

