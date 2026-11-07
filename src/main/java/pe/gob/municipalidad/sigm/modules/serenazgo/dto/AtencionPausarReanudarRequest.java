package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionPausarReanudarRequest {

    @Size(max = 1000, message = "El motivo no puede superar los 1000 caracteres")
    private String motivo;

    @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres")
    private String observaciones;
}
