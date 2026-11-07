package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.SentidoDecisionInfraccion;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionInfraccionRequest {

    @NotBlank(message = "El número o referencia de la resolución formal es obligatorio")
    private String resolucionDecisionRef;

    @NotNull(message = "La fecha de emisión del acto administrativo es obligatoria")
    private LocalDate fechaDecision;

    @NotNull(message = "El sentido de la decisión administrativa es obligatorio")
    private SentidoDecisionInfraccion sentidoDecision;
}
