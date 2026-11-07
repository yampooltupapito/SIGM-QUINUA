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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObligacionDetailResponse {
    private UUID id;
    private String codigoObligacion;
    private UUID contribuyenteId;
    private String contribuyenteCodigo;
    private String contribuyenteNombre;
    private String contribuyenteDocumento;
    private String contribuyenteDireccion;
    private UUID conceptoId;
    private String conceptoCodigo;
    private String conceptoNombre;
    private UUID predioId;
    private String predioCodigo;
    private String predioDireccion;
    private String predioClasificacion;
    private UUID periodoId;
    private Integer periodoAnio;
    private TipoDeterminacionTributaria tipoDeterminacion;
    private UUID determinacionOrigenId;
    private Integer numeroVersionDeterminacion;
    private Integer cuotaNumero;
    private Integer totalCuotas;
    private BigDecimal importeOriginal;
    private BigDecimal importeVigente;
    private BigDecimal importeDeterminado;
    private BigDecimal importeAjustes;
    private BigDecimal importePagado;
    private BigDecimal saldo;
    private String moneda;
    private LocalDate fechaDeterminacion;
    private LocalDate fechaVencimiento;
    private OffsetDateTime fechaRegistro;
    private OffsetDateTime fechaValidacion;
    private String usuarioValidador;
    private OffsetDateTime fechaAnulacion;
    private String usuarioAnulador;
    private EstadoObligacionTributaria estado;
    private EstadoFinancieroObligacion estadoFinanciero;
    private EstadoExigibilidadObligacion estadoExigibilidad;
    private String versionCalculo;
    private String normaNombre;
    private String snapshotDeterminacion;
    private String observaciones;
    private String motivoAjuste;
    private String motivoAnulacion;
    private String resolucionSustento;

    @Builder.Default
    private List<CuotaResponse> cuotas = new ArrayList<>();

    @Builder.Default
    private List<MovimientoResponse> movimientos = new ArrayList<>();

    @Builder.Default
    private List<HistorialObligacionResponse> historial = new ArrayList<>();

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
