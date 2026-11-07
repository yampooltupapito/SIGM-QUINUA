package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeudaCiudadanoDto {
    private BigDecimal deudaTotal;
    private BigDecimal deudaVigente;
    private BigDecimal deudaVencida;
    private BigDecimal deudaExigibleCoactiva;
    private int totalObligacionesDeuda;
    private List<ObligacionCiudadanoDto> detalleObligaciones;
}

