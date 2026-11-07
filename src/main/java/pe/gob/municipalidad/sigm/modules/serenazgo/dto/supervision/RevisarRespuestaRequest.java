package pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevisarRespuestaRequest {

    @NotNull(message = "Debe indicar si la subsanación es conforme o no")
    private Boolean conforme;

    @NotBlank(message = "El pronunciamiento u observaciones del supervisor son obligatorios")
    @Size(min = 10, max = 2000, message = "El detalle debe contener entre 10 y 2000 caracteres")
    private String observaciones;
}
