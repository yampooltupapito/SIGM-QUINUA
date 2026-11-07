package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoBeneficioPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoCalculoPredial;

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
public class CalculoPredialDetailResponse {

    private UUID id;
    private String codigoCalculo;
    private UUID contribuyenteId;
    private String contribuyenteCodigo;
    private String contribuyenteNombre;
    private String contribuyenteDocumento;
    private UUID periodoId;
    private Integer periodoAnio;
    private BigDecimal uitAplicada;
    private UUID configuracionId;
    private Integer configuracionVersion;
    private UUID normaId;
    private String normaReferencia;
    private TipoCalculoPredial tipoCalculo;
    private EstadoCalculoPredial estado;
    private String badgeClass;
    private OffsetDateTime fechaCalculo;
    private LocalDate fechaReferencia;
    private Integer cantidadPredios;
    private BigDecimal totalAutovaluoBruto;
    private BigDecimal totalDeduccionesBeneficios;
    private BigDecimal baseImponibleNeta;
    private BigDecimal impuestoCalculadoTramos;
    private BigDecimal impuestoMinimoLegal;
    private boolean aplicaImpuestoMinimo;
    private BigDecimal impuestoAnualDeterminado;
    private String moneda;
    private TipoBeneficioPredial beneficioAplicado;
    private String beneficioNombre;
    private String resolucionBeneficio;
    private String observaciones;
    private String resolucionAprobacion;
    private String motivoAnulacion;
    private String usuarioCalculo;
    private String usuarioAprobador;
    private OffsetDateTime fechaAprobacion;
    private String usuarioAnulador;
    private OffsetDateTime fechaAnulacion;
    private Integer versionCalculo;
    private UUID calculoPredecesorId;

    @Builder.Default
    private List<CalculoPredialDetalleResponse> predios = new ArrayList<>();

    @Builder.Default
    private List<CalculoPredialTramoResponse> tramos = new ArrayList<>();

    @Builder.Default
    private List<HistorialCalculoResponse> historial = new ArrayList<>();

    @Builder.Default
    private List<String> advertencias = new ArrayList<>();

    @Builder.Default
    private List<String> explicacionPasoAPaso = new ArrayList<>();
}
