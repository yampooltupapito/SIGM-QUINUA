package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoUnidadMovil;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadMovilUpdateRequest {

    @NotNull(message = "El tipo de unidad móvil es obligatorio")
    private TipoUnidadMovil tipoUnidad;

    private String placa;
    private String marca;
    private String modelo;
    private Integer anio;
    private String color;
    private String numeroMotor;
    private String numeroChasis;
    private String codigoPatrimonial;
    private Long oficinaResponsableId;
    private BigDecimal kilometrajeActual;
    private String observaciones;
}
