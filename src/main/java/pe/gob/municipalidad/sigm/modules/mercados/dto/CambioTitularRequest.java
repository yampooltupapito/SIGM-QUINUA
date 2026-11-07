package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambioTitularRequest {

    @NotNull(message = "El puesto comercial es obligatorio")
    private UUID puestoId;

    @NotNull(message = "La asignación actual es obligatoria")
    private UUID asignacionId;

    @NotNull(message = "El comerciante titular actual es obligatorio")
    private UUID comercianteActualId;

    @NotNull(message = "El comerciante titular propuesto es obligatorio")
    private UUID comerciantePropuestoId;

    private Long procedimientoId;

    @NotBlank(message = "El sustento o motivo del cambio de titular es obligatorio")
    private String motivo;

    private Long expedienteId;

    private Long oficinaResponsableId;

    private LocalDate fechaInicioSolicitada;
}
