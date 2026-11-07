package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionCancelarRequest {

    @NotBlank(message = "El motivo de la cancelación es obligatorio")
    @Size(max = 1000, message = "El motivo no puede superar los 1000 caracteres")
    private String motivoCancelacion;

    public String getMotivo() {
        return motivoCancelacion;
    }

    public void setMotivo(String m) {
        this.motivoCancelacion = m;
    }
}
