package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ResultadoIntentoNotificacion;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarIntentoRequest {

    private OffsetDateTime fechaHoraIntento;

    @NotBlank(message = "El medio utilizado es obligatorio")
    @Size(max = 60, message = "El medio no puede exceder 60 caracteres")
    private String medioUtilizado;

    @NotNull(message = "El resultado fáctico de la actuación es obligatorio")
    private ResultadoIntentoNotificacion resultado;

    @Size(max = 255, message = "La referencia de ubicación no puede exceder 255 caracteres")
    private String ubicacionReferencia;

    private String observaciones;
}

