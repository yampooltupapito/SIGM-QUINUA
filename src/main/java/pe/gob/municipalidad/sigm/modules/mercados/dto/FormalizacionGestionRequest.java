package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormalizacionGestionRequest {

    private LocalDate fechaEfectiva;

    private LocalDate nuevaFechaFin;

    @NotBlank(message = "El sustento o resolución de formalización es obligatorio")
    private String motivo;

    private Long documentoFormalizacionId;
}
