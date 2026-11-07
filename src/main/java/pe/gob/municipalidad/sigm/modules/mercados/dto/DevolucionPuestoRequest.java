package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevolucionPuestoRequest {

    @NotBlank(message = "El estado físico del puesto es obligatorio")
    private String estadoFisicoPuesto; // BUENO, REGULAR, MALO, REQUIERE_MANTENIMIENTO

    @Size(max = 2000, message = "Las observaciones no pueden exceder 2000 caracteres")
    private String observaciones;

    private Long documentoActaId;
}
