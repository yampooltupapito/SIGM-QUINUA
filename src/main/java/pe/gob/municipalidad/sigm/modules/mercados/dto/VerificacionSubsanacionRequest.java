package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificacionSubsanacionRequest {

    @NotNull(message = "Debe indicar si la subsanación es conforme o insuficiente")
    private Boolean aprobada; // true = APROBADA_CONFORME, false = RECHAZADA_INSUFICIENTE

    @NotBlank(message = "Las observaciones técnicas de verificación son obligatorias")
    private String observaciones;
}
