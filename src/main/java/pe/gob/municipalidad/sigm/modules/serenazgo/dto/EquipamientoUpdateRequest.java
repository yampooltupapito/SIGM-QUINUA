package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipamientoUpdateRequest {

    @NotBlank(message = "La descripción del equipamiento es obligatoria")
    private String descripcion;

    private String marca;
    private String modelo;
    private String numeroSerie;
    private String codigoPatrimonial;
    private Long oficinaResponsableId;
    private Integer stockTotal;
    private String observaciones;
}
