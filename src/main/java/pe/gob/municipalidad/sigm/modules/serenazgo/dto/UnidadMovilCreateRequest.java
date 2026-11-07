package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoUnidadMovil;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadMovilCreateRequest {

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
    private LocalDate fechaIncorporacion;
    private String observaciones;
}
