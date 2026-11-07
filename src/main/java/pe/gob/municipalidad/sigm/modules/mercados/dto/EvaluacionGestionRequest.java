package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.SentidoEvaluacionGestion;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionGestionRequest {

    @NotNull(message = "El sentido de la evaluación es obligatorio")
    private SentidoEvaluacionGestion sentidoEvaluacion;

    @NotBlank(message = "El informe técnico o sustento de evaluación es obligatorio")
    private String informeTecnico;

    private String observaciones;

    private Long documentoSustentoId;
}
