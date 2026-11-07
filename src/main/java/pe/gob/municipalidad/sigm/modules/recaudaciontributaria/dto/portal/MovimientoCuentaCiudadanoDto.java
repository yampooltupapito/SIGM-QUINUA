package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoCuentaCiudadanoDto {
    private UUID id;
    private Long numeroSecuencia;
    private OffsetDateTime fechaMovimiento;
    private String tipoMovimiento;
    private String origenOperacion;
    private String referenciaOperacion;
    private BigDecimal montoCargo;
    private BigDecimal montoAbono;
    private BigDecimal saldoPosterior;
    private String conceptoNombre;
    private String glosa;
}

