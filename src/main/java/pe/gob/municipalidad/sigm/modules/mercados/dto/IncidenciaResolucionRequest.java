package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaResolucionRequest {

    @NotBlank(message = "El motivo de la resolución y las medidas adoptadas son obligatorias")
    @Size(min = 10, max = 2000, message = "El motivo debe tener entre 10 y 2000 caracteres")
    private String motivoResolucion;

    private UUID documentoId;
}
