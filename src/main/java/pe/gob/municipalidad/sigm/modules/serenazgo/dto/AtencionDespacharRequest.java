package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalComunicacionDespacho;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionDespacharRequest {

    private CanalComunicacionDespacho canalComunicacion;

    @Size(max = 2000, message = "Las instrucciones no pueden superar los 2000 caracteres")
    private String instruccionesDespacho;

    public void setInstrucciones(String i) {
        this.instruccionesDespacho = i;
    }
}
