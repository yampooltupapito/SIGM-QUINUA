package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDatoParametro;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.UnidadMedidaParametro;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametroCreateRequest {

    @NotBlank(message = "El código del parámetro es obligatorio")
    @Size(max = 80, message = "El código no debe superar 80 caracteres")
    private String codigo;

    @NotBlank(message = "La denominación del parámetro es obligatoria")
    @Size(max = 150, message = "La denominación no debe superar 150 caracteres")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El tipo de dato es obligatorio")
    @Builder.Default
    private TipoDatoParametro tipoDato = TipoDatoParametro.DECIMAL;

    private BigDecimal valorDecimal;

    private String valorTexto;

    private Boolean valorBooleano;

    private LocalDate valorFecha;

    @NotNull(message = "La unidad de medida es obligatoria")
    @Builder.Default
    private UnidadMedidaParametro unidad = UnidadMedidaParametro.PEN;

    private String origenNormativo;

    @Builder.Default
    private Boolean esObligatorio = true;

    @Builder.Default
    private Integer ordenVisual = 1;
}
