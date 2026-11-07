package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ResultadoValidacionProcedimental;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidarProcedimientoRequest {

    @NotNull(message = "El resultado de la validación procedimental es obligatorio")
    private ResultadoValidacionProcedimental resultado;

    @NotBlank(message = "El sustento técnico/jurídico de la decisión es obligatorio")
    private String sustento;

    private Boolean cumpleDestinatarioIdentificado;
    private Boolean cumpleDomicilioValido;
    private Boolean cumpleDocumentoAdjunto;
    private Boolean cumplePlazoProcedimental;
    private Boolean cumpleEvidenciaMinima;
    private Boolean cumpleDatosReceptorYCargo;

    private String observaciones;
}

