package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEquipamientoSerenazgo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipamientoCreateRequest {

    @NotNull(message = "El tipo de equipamiento es obligatorio")
    private TipoEquipamientoSerenazgo tipoEquipo;

    @NotBlank(message = "La descripción del equipamiento es obligatoria")
    private String descripcion;

    private String marca;
    private String modelo;
    private String numeroSerie;
    private String codigoPatrimonial;
    private Long oficinaResponsableId;

    @Builder.Default
    private Boolean esControladoPorCantidad = false;

    @Builder.Default
    private Integer stockTotal = 1;

    private String observaciones;
}
