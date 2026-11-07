package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoBeneficioArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoCalculoArbitrio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tributaria_calculos_arbitrio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculoArbitrioMunicipal extends AuditableEntity {

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
    @JoinColumn(name = "predio_id", nullable = false)
    private PredioTributario predio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periodo_id", nullable = false)
    private PeriodoTributario periodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuracion_id")
    private ConfiguracionArbitrioMunicipal configuracion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_calculo", nullable = false, length = 35)
    @Builder.Default
    private TipoCalculoArbitrio tipoCalculo = TipoCalculoArbitrio.SIMULACION;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoCalculoArbitrio estado = EstadoCalculoArbitrio.SIMULACION;

    @Column(name = "fecha_calculo", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaCalculo = OffsetDateTime.now();

    @Column(name = "fecha_referencia", nullable = false)
    @Builder.Default
    private LocalDate fechaReferencia = LocalDate.now();

    @Column(name = "subperiodo", nullable = false, length = 40)
    @Builder.Default
    private String subperiodo = "ANUAL";

    @Column(name = "total_servicios", nullable = false)
    @Builder.Default
    private Integer totalServicios = 1;

    @Column(name = "importe_anual_total", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importeAnualTotal = BigDecimal.ZERO;

    @Column(name = "importe_periodo_total", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importePeriodoTotal = BigDecimal.ZERO;

    @Column(name = "moneda", nullable = false, length = 10)
    @Builder.Default
    private String moneda = "PEN";

    @Enumerated(EnumType.STRING)
    @Column(name = "beneficio_aplicado", length = 100)
    @Builder.Default
    private TipoBeneficioArbitrio beneficioAplicado = TipoBeneficioArbitrio.NINGUNO;

    @Column(name = "porcentaje_descuento_beneficio", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeDescuentoBeneficio = BigDecimal.ZERO;

    @Column(name = "monto_descuento_beneficio", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal montoDescuentoBeneficio = BigDecimal.ZERO;

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
    private CalculoArbitrioMunicipal calculoPredecesor;

    @Column(name = "oficina_id")
    private Long oficinaId;

    @OneToMany(mappedBy = "calculo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleCalculoArbitrio> detalles = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
