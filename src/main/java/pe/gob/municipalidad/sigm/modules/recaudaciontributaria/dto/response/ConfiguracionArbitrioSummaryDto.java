package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConfiguracionArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MetodoDistribucionArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoServicioArbitrio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionArbitrioSummaryDto {
    private UUID id;
    private String codigoConfiguracion;
    private UUID servicioId;
    private String servicioNombre;
    private String servicioCodigo;
    private TipoServicioArbitrio tipoServicio;
    private Integer periodoAnio;
    private String normaNumero;
    private Integer versionConfiguracion;
    private LocalDate fechaInicioVigencia;
    private LocalDate fechaFinVigencia;
    private EstadoConfiguracionArbitrio estado;
    private String badgeClass;
    private BigDecimal costoTotalAnual;
    private BigDecimal costoTotalMensual;
    private MetodoDistribucionArbitrio metodologiaDistribucion;
    private Integer cantidadPrediosAfectos;
}
