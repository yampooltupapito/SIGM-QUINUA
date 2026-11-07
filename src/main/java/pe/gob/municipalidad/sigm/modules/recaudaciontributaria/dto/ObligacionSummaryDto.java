package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoExigibilidadObligacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoFinancieroObligacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDeterminacionTributaria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObligacionSummaryDto {
    private UUID id;
    private String codigoObligacion;
    private UUID contribuyenteId;
    private String contribuyenteCodigo;
    private String contribuyenteNombre;
    private String contribuyenteDocumento;
    private String conceptoCodigo;
    private String conceptoNombre;
    private String predioCodigo;
    private String predioDireccion;
    private Integer periodoAnio;
    private TipoDeterminacionTributaria tipoDeterminacion;
    private Integer cuotaNumero;
    private Integer totalCuotas;
    private BigDecimal importeDeterminado;
    private BigDecimal importePagado;
    private BigDecimal saldo;
    private String moneda;
    private LocalDate fechaVencimiento;
    private OffsetDateTime fechaRegistro;
    private EstadoObligacionTributaria estado;
    private EstadoFinancieroObligacion estadoFinanciero;
    private EstadoExigibilidadObligacion estadoExigibilidad;

    public String getBadgeClass() {
        return estado != null ? estado.getBadgeClass() : "";
    }

    public String getFinancieroBadgeClass() {
        return estadoFinanciero != null ? estadoFinanciero.getBadgeClass() : "";
    }

    public String getExigibilidadBadgeClass() {
        return estadoExigibilidad != null ? estadoExigibilidad.getBadgeClass() : "";
    }
}
