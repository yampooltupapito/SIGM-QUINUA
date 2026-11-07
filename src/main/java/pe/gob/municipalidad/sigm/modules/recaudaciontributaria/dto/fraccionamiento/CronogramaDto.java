package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCronogramaFraccionamiento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CronogramaDto {
    private UUID id;
    private Integer versionCronograma;
    private BigDecimal importeBase;
    private BigDecimal importeCuotaInicial;
    private BigDecimal importeFinanciado;
    private BigDecimal importeAccesorios;
    private BigDecimal importeTotal;
    private Integer numeroCuotas;
    private BigDecimal tasaInteresAplicada;
    private OffsetDateTime fechaGeneracion;
    private EstadoCronogramaFraccionamiento estado;
    private String estadoDescripcion;
    private String estadoBadgeClass;
    private List<CuotaDto> cuotas;
}
