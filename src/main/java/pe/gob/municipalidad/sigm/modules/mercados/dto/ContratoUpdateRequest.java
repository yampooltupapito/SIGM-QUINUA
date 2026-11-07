package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoUpdateRequest {

    private Long tipoContratoId;

    private String tipoContratoCodigo;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private Long expedienteId;

    private Long oficinaResponsableId;

    private String observaciones;
}
