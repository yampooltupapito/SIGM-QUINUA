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

    // Alias de solo lectura usados por la plantilla predial/list.html (no se serializan en JSON)
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getEjercicioFiscal() {
        return periodoAnio;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getNumeroVersion() {
        return versionCalculo != null ? versionCalculo : 1;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getContribuyenteNumeroDocumento() {
        return contribuyenteDocumento;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public BigDecimal getBaseImponible() {
        return baseImponibleNeta != null ? baseImponibleNeta : BigDecimal.ZERO;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public BigDecimal getImporteCalculado() {
        return impuestoAnualDeterminado != null ? impuestoAnualDeterminado : BigDecimal.ZERO;
    }
}
