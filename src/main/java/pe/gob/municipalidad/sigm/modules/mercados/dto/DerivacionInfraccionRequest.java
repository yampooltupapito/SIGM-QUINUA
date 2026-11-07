package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DerivacionInfraccionRequest {
    private String expedienteId;

    @NotBlank(message = "El motivo de la derivación es obligatorio")
    private String motivo;
}
