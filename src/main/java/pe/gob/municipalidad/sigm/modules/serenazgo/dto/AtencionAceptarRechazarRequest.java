package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionAceptarRechazarRequest {

    @NotNull(message = "Debe indicar si acepta o rechaza la asignación")
    private Boolean aceptar;

    @Size(max = 1000, message = "El motivo de rechazo no puede superar los 1000 caracteres")
    private String motivoRechazo;

    public Boolean isAceptado() {
        return aceptar;
    }

    public void setAceptado(Boolean aceptado) {
        this.aceptar = aceptado;
    }

    public void setObservaciones(String o) {
        this.motivoRechazo = o;
    }
}
