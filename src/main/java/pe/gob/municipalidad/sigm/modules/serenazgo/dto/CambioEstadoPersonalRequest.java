package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPersonalSerenazgo;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoPersonalRequest {

    @NotNull(message = "Debe indicar el nuevo estado")
    private EstadoPersonalSerenazgo nuevoEstado;

    @NotNull(message = "Debe indicar la fecha efectiva del cambio de estado")
    private LocalDate fechaEfectiva;

    @NotBlank(message = "Debe fundamentar el motivo del cambio de estado")
    private String motivo;

    private Long documentoSustentoId;
}
