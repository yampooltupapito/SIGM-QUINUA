package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaSeguimientoConsultaRequest {

    @NotBlank(message = "El código de seguimiento es obligatorio (ej. ALT-2026-00001)")
    private String codigo;

    private String pinConsulta;
}
