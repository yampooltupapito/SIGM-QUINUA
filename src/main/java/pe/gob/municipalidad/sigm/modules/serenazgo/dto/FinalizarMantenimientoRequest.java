package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalizarMantenimientoRequest {

    private LocalDate fechaRealizada;

    @NotBlank(message = "El informe de resultado técnico o conformidad es obligatorio")
    private String resultadoTecnico;

    private BigDecimal costoFinal;
}
