package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.PeriodicidadArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoBeneficioArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoCalculoArbitrio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArbitrioDetailResponse {
    private UUID id;
    private String codigoCalculo;
    private UUID contribuyenteId;
    private String contribuyenteCodigo;
    private String contribuyenteNombre;
    private String contribuyenteDocumento;
    private UUID predioId;
    private String predioCodigo;
    private String predioDireccion;
    private String tipoPredio;
    private String clasificacionPredio;
    private BigDecimal areaTerreno;
    private BigDecimal areaConstruida;
    private BigDecimal porcentajeParticipacion;
    private Integer periodoAnio;
    private UUID periodoId;
    private TipoCalculoArbitrio tipoCalculo;
    private EstadoCalculoArbitrio estado;
    private String badgeClass;
    private OffsetDateTime fechaCalculo;
    private LocalDate fechaReferencia;
    private String subperiodo;
    private Integer totalServicios;
    private BigDecimal importeAnualTotal;
    private BigDecimal importePeriodoTotal;
    private String moneda;
    private TipoBeneficioArbitrio beneficioAplicado;
    private BigDecimal porcentajeDescuentoBeneficio;
    private BigDecimal montoDescuentoBeneficio;
    private String resolucionBeneficio;
    private String observaciones;
    private String resolucionAprobacion;
    private String motivoAnulacion;
    private String usuarioCalculo;
    private String usuarioAprobador;
    private OffsetDateTime fechaAprobacion;
    private Integer versionCalculo;
    private UUID calculoPredecesorId;

    @Builder.Default
    private List<DetalleCalculoArbitrioResponse> detalles = new ArrayList<>();

    @Builder.Default
    private List<HistorialArbitrioResponse> historial = new ArrayList<>();

    @Builder.Default
    private List<String> advertencias = new ArrayList<>();

    @Builder.Default
    private List<String> explicaciones = new ArrayList<>();

    public BigDecimal getSubtotalCalculado() {
        if (importeAnualTotal == null) return BigDecimal.ZERO;
        return importeAnualTotal.add(montoDescuentoBeneficio != null ? montoDescuentoBeneficio : BigDecimal.ZERO);
    }

    public BigDecimal getImporteBeneficio() {
        return montoDescuentoBeneficio != null ? montoDescuentoBeneficio : BigDecimal.ZERO;
    }

    public BigDecimal getImporteCalculado() {
        return importeAnualTotal != null ? importeAnualTotal : BigDecimal.ZERO;
    }

    public BigDecimal getImportePeriodico() {
        return importePeriodoTotal != null ? importePeriodoTotal : BigDecimal.ZERO;
    }

    public BigDecimal getPorcentajeCondomino() {
        return porcentajeParticipacion != null ? porcentajeParticipacion : new BigDecimal("100.00");
    }

    public TipoBeneficioArbitrio getTipoBeneficio() {
        return beneficioAplicado != null ? beneficioAplicado : TipoBeneficioArbitrio.NINGUNO;
    }

    public PeriodicidadArbitrio getPeriodicidad() {
        return PeriodicidadArbitrio.TRIMESTRAL;
    }

    public String getPredioClasificacion() {
        return clasificacionPredio != null ? clasificacionPredio : "-";
    }

    public BigDecimal getPredioAreaTerrenoM2() {
        return areaTerreno != null ? areaTerreno : BigDecimal.ZERO;
    }

    public BigDecimal getPredioFrenteLinealMetros() {
        if (areaTerreno != null && areaTerreno.compareTo(BigDecimal.ZERO) > 0) {
            return BigDecimal.valueOf(Math.sqrt(areaTerreno.doubleValue()) * 0.60).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return new BigDecimal("6.00");
    }
}
