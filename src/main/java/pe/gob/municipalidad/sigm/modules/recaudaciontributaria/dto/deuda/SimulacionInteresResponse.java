package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.deuda;

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
public class SimulacionInteresResponse {

    private UUID obligacionId;
    private String codigoObligacion;

    private BigDecimal montoCapital;
    private LocalDate fechaVencimiento;
    private LocalDate fechaCorte;
    private long diasMorosidad;

    private UUID parametroId;
    private String codigoParametro;
    private String nombreParametro;
    private TipoInteresTributario tipoInteres;
    private BigDecimal tasaNominal;
    private UnidadTemporalInteres unidadTemporal;
    private BigDecimal factorDiario;

    private BigDecimal montoInteresCalculado;
    private BigDecimal montoTotalSimulado;
    private String moneda;

    private String baseLegal;
    private String advertenciaLegal;
    private boolean esAptoParaCalculo;
    private String mensajeError;
}
