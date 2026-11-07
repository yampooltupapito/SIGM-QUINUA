package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionIniciarRequest {

    @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres")
    private String observaciones;

    public void setDescripcionSituacionInicial(String d) {
        this.observaciones = d;
    }
}
