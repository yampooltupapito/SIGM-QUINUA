package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoCalculoPredial;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculoPredialSummaryDto {

    private UUID id;
    private String codigoCalculo;
    private UUID contribuyenteId;
    private String contribuyenteNombre;
    private String contribuyenteDocumento;
    private Integer periodoAnio;
    private TipoCalculoPredial tipoCalculo;
    private EstadoCalculoPredial estado;
    private String badgeClass;
    private Integer cantidadPredios;
    private BigDecimal baseImponibleNeta;
    private BigDecimal impuestoAnualDeterminado;
    private String moneda;
    private OffsetDateTime fechaCalculo;
    private Integer versionCalculo;
}
