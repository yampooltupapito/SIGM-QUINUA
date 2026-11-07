package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoElementoPlano;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElementoPlanoRequest {

    private UUID id;

    @NotNull(message = "El tipo de elemento es obligatorio")
    private TipoElementoPlano tipoElemento;

    private UUID referenciaEntidadId;

    @NotBlank(message = "El código visual es obligatorio")
    @Size(max = 60, message = "El código visual no debe exceder 60 caracteres")
    private String codigoVisual;

    @Size(max = 150, message = "La etiqueta no debe exceder 150 caracteres")
    private String etiqueta;

    @NotNull(message = "La coordenada X es obligatoria")
    private BigDecimal coordenadaX;

    @NotNull(message = "La coordenada Y es obligatoria")
    private BigDecimal coordenadaY;

    @NotNull(message = "El ancho es obligatorio")
    @DecimalMin(value = "5.0", message = "El ancho debe ser al menos de 5 unidades")
    private BigDecimal ancho;

    @NotNull(message = "El alto es obligatorio")
    @DecimalMin(value = "5.0", message = "El alto debe ser al menos de 5 unidades")
    private BigDecimal alto;

    @Builder.Default
    private BigDecimal rotacion = BigDecimal.ZERO;

    @Builder.Default
    private Integer ordenCapa = 0;

    private String estiloConfigurado;
}
