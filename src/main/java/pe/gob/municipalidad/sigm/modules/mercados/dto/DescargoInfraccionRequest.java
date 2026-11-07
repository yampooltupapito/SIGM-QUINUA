package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescargoInfraccionRequest {

    private OffsetDateTime fechaDescargo;

    @NotBlank(message = "La referencia del documento o memorial de descargo es obligatoria")
    private String documentoDescargoRef;

    @NotBlank(message = "El resumen del descargo y medios probatorios presentados es obligatorio")
    @Size(min = 10, max = 3000, message = "El resumen debe tener entre 10 y 3000 caracteres")
    private String resumenDescargo;
}
