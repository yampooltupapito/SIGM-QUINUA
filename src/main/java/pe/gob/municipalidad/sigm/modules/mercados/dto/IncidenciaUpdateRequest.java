package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PrioridadIncidencia;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaUpdateRequest {

    private UUID tipoIncidenciaId;

    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String descripcion;

    private OffsetDateTime fechaOcurrencia;
    private PrioridadIncidencia prioridad;
    private String observaciones;
}
