package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoActuacionIncidencia;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaActuacionRequest {

    @NotNull(message = "El tipo de actuación es obligatorio")
    private TipoActuacionIncidencia tipoActuacion;

    @NotBlank(message = "La descripción de la actuación es obligatoria")
    @Size(min = 5, max = 2000, message = "La descripción debe tener entre 5 y 2000 caracteres")
    private String descripcion;

    private UUID documentoId;
    private EstadoIncidencia nuevoEstado;
}
