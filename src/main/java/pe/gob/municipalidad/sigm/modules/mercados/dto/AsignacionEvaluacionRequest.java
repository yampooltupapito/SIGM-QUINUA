package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionEvaluacionRequest {

    @NotBlank(message = "El resultado de la evaluación es obligatorio")
    private String resultado; // CONFORME, OBSERVADO, DESFAVORABLE

    @NotBlank(message = "El motivo o sustento técnico es obligatorio")
    @Size(max = 2000, message = "El motivo no puede exceder 2000 caracteres")
    private String motivo;

    private Long documentoSustentoId;

    private String observaciones;
}
