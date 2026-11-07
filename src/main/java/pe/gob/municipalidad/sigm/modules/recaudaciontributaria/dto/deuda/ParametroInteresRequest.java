package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoInteresTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.UnidadTemporalInteres;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametroInteresRequest {

    @NotBlank(message = "El código de parámetro es obligatorio")
    private String codigo;

    @NotBlank(message = "El nombre del parámetro es obligatorio")
    private String nombre;

    private UUID conceptoId;

    @NotNull(message = "El tipo de interés es obligatorio")
    private TipoInteresTributario tipoInteres;

    @NotNull(message = "La tasa es obligatoria")
    @DecimalMin(value = "0.0001", message = "La tasa debe ser mayor a cero")
    private BigDecimal tasa;

    @NotNull(message = "La unidad temporal es obligatoria")
    private UnidadTemporalInteres unidadTemporal;

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate fechaInicioVigencia;

    private LocalDate fechaFinVigencia;

    private UUID normaId;

    @NotBlank(message = "La base legal es obligatoria")
    private String baseLegal;

    private Boolean estado;
}
