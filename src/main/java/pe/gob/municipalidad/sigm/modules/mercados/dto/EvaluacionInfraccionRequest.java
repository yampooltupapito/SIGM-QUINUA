package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoEvaluacionInfraccion;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionInfraccionRequest {

    @NotNull(message = "El nuevo estado de evaluación es obligatorio")
    private EstadoEvaluacionInfraccion nuevoEstado; // DESCARTADA, DERIVADA_A_PROCEDIMIENTO, PENDIENTE_INFORMACION, EN_EVALUACION

    @NotBlank(message = "El informe de evaluación preliminar es obligatorio")
    @Size(min = 10, max = 3000, message = "El informe debe tener entre 10 y 3000 caracteres")
    private String informeEvaluacion;

    private String motivoDescarte;
    private UUID expedienteId;
    private String expedienteNumero;
}
