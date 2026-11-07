package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoParametroFraccionamiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParametroFraccionamientoDto {
    private UUID id;
    private String codigo;
    private TipoParametroFraccionamiento tipoParametro;
    private String tipoParametroDescripcion;
    private BigDecimal valorNumerico;
    private String valorTexto;
    private String unidad;
    private LocalDate fechaInicioVigencia;
    private LocalDate fechaFinVigencia;
    private Boolean estado;
}
