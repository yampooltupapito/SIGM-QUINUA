package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicarPlanoRequest {

    @Size(max = 500, message = "La observación o justificación no debe exceder 500 caracteres")
    private String observaciones;
}
