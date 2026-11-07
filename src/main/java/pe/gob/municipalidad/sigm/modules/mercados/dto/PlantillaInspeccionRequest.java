package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoInspeccionMercado;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaInspeccionRequest {

    @NotBlank(message = "El código de plantilla es obligatorio")
    @Size(max = 50)
    private String codigo;

    @NotBlank(message = "El nombre de la plantilla es obligatorio")
    @Size(max = 150)
    private String nombre;

    private String descripcion;

    @NotNull(message = "El tipo de inspección es obligatorio")
    private TipoInspeccionMercado tipoInspeccion;

    private Long oficinaResponsableId;

    private LocalDate fechaAprobacion;

    private String sustentoNormativo;

    private List<ItemDto> items;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDto {
        private String codigo;
        private String categoria;
        private String descripcion;
        private int orden;
        private boolean obligatorio;
        private String referenciaNormativa;
    }
}
