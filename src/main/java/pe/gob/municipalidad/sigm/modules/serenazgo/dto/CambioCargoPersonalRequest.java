package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambioCargoPersonalRequest {

    @NotNull(message = "Debe indicar el nuevo cargo operativo")
    private UUID nuevoCargoId;

    @NotNull(message = "Debe indicar la fecha de inicio del nuevo cargo")
    private LocalDate fechaInicio;

    @NotBlank(message = "Debe indicar el motivo del cambio de cargo")
    private String motivo;

    private Long documentoSustentoId;
}
