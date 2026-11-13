package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoCalculoArbitrio;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArbitrioSummaryDto {
    private UUID id;
    private String codigoCalculo;
    private UUID contribuyenteId;
    private String contribuyenteNombre;
    private String contribuyenteDocumento;
    private UUID predioId;
    private String predioCodigo;
    private String predioDireccion;
    private Integer periodoAnio;
    private TipoCalculoArbitrio tipoCalculo;
    private EstadoCalculoArbitrio estado;
    private String badgeClass;
    private Integer totalServicios;
    private BigDecimal importeAnualTotal;
    private BigDecimal importePeriodoTotal;
    private String moneda;
    private OffsetDateTime fechaCalculo;
    private Integer versionCalculo;

    public BigDecimal getSubtotalCalculado() {
        return importeAnualTotal != null ? importeAnualTotal : BigDecimal.ZERO;
    }

    public BigDecimal getImporteBeneficio() {
        return BigDecimal.ZERO;
    }

    // Alias de solo lectura usados por la plantilla arbitrios/list.html (no se serializan en JSON)
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getAnioPeriodo() {
        return periodoAnio;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public BigDecimal getImporteCalculado() {
        return importeAnualTotal != null ? importeAnualTotal : BigDecimal.ZERO;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public BigDecimal getImportePeriodico() {
        return importePeriodoTotal != null ? importePeriodoTotal : BigDecimal.ZERO;
    }
}
