package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoCreateRequest {

    @NotNull(message = "La asignación es obligatoria")
    private UUID asignacionId;

    private Long tipoContratoId;

    @Builder.Default
    private String tipoContratoCodigo = "USO_ESPACIO_COMERCIAL";

    @Builder.Default
    private LocalDate fechaElaboracion = LocalDate.now();

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private Long expedienteId;

    private Long oficinaResponsableId;

    private String observaciones;
}
