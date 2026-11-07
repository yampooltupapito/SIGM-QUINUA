package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoBeneficioPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoCalculoPredial;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad principal que almacena el cálculo, simulación y liquidación oficial
 * del Impuesto Predial Municipal para un contribuyente y ejercicio fiscal.
 */
@Entity
@Table(name = "tributaria_calculos_predial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculoImpuestoPredial extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_calculo", nullable = false, unique = true, length = 50)
    private String codigoCalculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private ContribuyenteTributario contribuyente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periodo_id", nullable = false)
    private PeriodoTributario periodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuracion_id")
    private ConfiguracionConceptoTributario configuracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "norma_id")
    private NormaTributariaConfigurada norma;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_calculo", nullable = false, length = 30)
    @Builder.Default
    private TipoCalculoPredial tipoCalculo = TipoCalculoPredial.SIMULACION;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoCalculoPredial estado = EstadoCalculoPredial.SIMULACION;

    @Column(name = "fecha_calculo", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaCalculo = OffsetDateTime.now();

    @Column(name = "fecha_referencia", nullable = false)
    @Builder.Default
    private LocalDate fechaReferencia = LocalDate.now();

    @Column(name = "uit_aplicada", nullable = false, precision = 12, scale = 2)
    private BigDecimal uitAplicada;

    @Column(name = "cantidad_predios", nullable = false)
    @Builder.Default
    private Integer cantidadPredios = 1;

    @Column(name = "total_autovaluo_bruto", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalAutovaluoBruto = BigDecimal.ZERO;

    @Column(name = "total_deducciones_beneficios", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalDeduccionesBeneficios = BigDecimal.ZERO;

    @Column(name = "base_imponible_neta", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal baseImponibleNeta = BigDecimal.ZERO;

    @Column(name = "impuesto_calculado_tramos", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal impuestoCalculadoTramos = BigDecimal.ZERO;

    @Column(name = "impuesto_minimo_legal", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal impuestoMinimoLegal = BigDecimal.ZERO;

    @Column(name = "aplica_impuesto_minimo", nullable = false)
    @Builder.Default
    private boolean aplicaImpuestoMinimo = false;

    @Column(name = "impuesto_anual_determinado", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal impuestoAnualDeterminado = BigDecimal.ZERO;

    @Column(name = "moneda", nullable = false, length = 10)
    @Builder.Default
    private String moneda = "PEN";

    @Enumerated(EnumType.STRING)
    @Column(name = "beneficio_aplicado", length = 100)
    @Builder.Default
    private TipoBeneficioPredial beneficioAplicado = TipoBeneficioPredial.NINGUNO;

    @Column(name = "resolucion_beneficio", length = 150)
    private String resolucionBeneficio;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_calculo", columnDefinition = "jsonb")
    private String snapshotCalculo;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "resolucion_aprobacion", length = 150)
    private String resolucionAprobacion;

    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    @Column(name = "usuario_calculo", nullable = false, length = 100)
    private String usuarioCalculo;

    @Column(name = "usuario_aprobador", length = 100)
    private String usuarioAprobador;

    @Column(name = "fecha_aprobacion")
    private OffsetDateTime fechaAprobacion;

    @Column(name = "usuario_anulador", length = 100)
    private String usuarioAnulador;

    @Column(name = "fecha_anulacion")
    private OffsetDateTime fechaAnulacion;

    @Column(name = "version_calculo", nullable = false)
    @Builder.Default
    private Integer versionCalculo = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculo_predecesor_id")
    private CalculoImpuestoPredial calculoPredecesor;

    @Column(name = "oficina_id")
    private Long oficinaId;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToMany(mappedBy = "calculo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CalculoPredialDetalle> prediosDetalle = new ArrayList<>();

    @OneToMany(mappedBy = "calculo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroTramo ASC")
    @Builder.Default
    private List<CalculoPredialTramo> tramosDetalle = new ArrayList<>();

    @OneToMany(mappedBy = "calculo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaRegistro DESC")
    @Builder.Default
    private List<HistorialCalculoPredial> historial = new ArrayList<>();
}
