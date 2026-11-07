package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionArchivarRequest {

    @NotBlank(message = "Debe consignar el motivo de archivo institucional")
    @Size(min = 5, max = 2000)
    private String motivo;
}
