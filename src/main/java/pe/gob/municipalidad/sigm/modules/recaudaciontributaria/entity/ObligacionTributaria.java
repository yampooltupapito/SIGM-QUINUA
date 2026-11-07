package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
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

/**
 * Entidad principal que representa una Obligación Tributaria Municipal individual
 * generada a partir de una determinación técnica aprobada (Impuesto Predial, Arbitrios, etc.).
 */
@Entity
@Table(name = "tributaria_obligaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObligacionTributaria extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_obligacion", nullable = false, unique = true, length = 50)
    private String codigoObligacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private ContribuyenteTributario contribuyente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concepto_id", nullable = false)
    private ConceptoTributario concepto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periodo_id", nullable = false)
    private PeriodoTributario periodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predio_id")
    private PredioTributario predio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_determinacion", nullable = false, length = 50)
    @Builder.Default
    private TipoDeterminacionTributaria tipoDeterminacion = TipoDeterminacionTributaria.IMPUESTO_PREDIAL;

    @Column(name = "determinacion_origen_id")
    private UUID determinacionOrigenId;

    @Column(name = "numero_version_determinacion", nullable = false)
    @Builder.Default
    private Integer numeroVersionDeterminacion = 1;

    @Column(name = "cuota_numero", nullable = false)
    @Builder.Default
    private Integer cuotaNumero = 1;

    @Column(name = "total_cuotas", nullable = false)
    @Builder.Default
    private Integer totalCuotas = 1;

    @Column(name = "importe_original", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importeOriginal = BigDecimal.ZERO;

    @Column(name = "importe_vigente", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importeVigente = BigDecimal.ZERO;

    @Column(name = "importe_determinado", nullable = false, precision = 14, scale = 2)
    private BigDecimal importeDeterminado;

    @Column(name = "importe_ajustes", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importeAjustes = BigDecimal.ZERO;

    @Column(name = "importe_pagado", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importePagado = BigDecimal.ZERO;

    @Column(name = "saldo", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldo;

    @Column(name = "moneda", nullable = false, length = 10)
    @Builder.Default
    private String moneda = "PEN";

    @Column(name = "fecha_determinacion", nullable = false)
    @Builder.Default
    private LocalDate fechaDeterminacion = LocalDate.now();

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaRegistro = OffsetDateTime.now();

    @Column(name = "fecha_validacion")
    private OffsetDateTime fechaValidacion;

    @Column(name = "usuario_validador", length = 100)
    private String usuarioValidador;

    @Column(name = "fecha_anulacion")
    private OffsetDateTime fechaAnulacion;

    @Column(name = "usuario_anulador", length = 100)
    private String usuarioAnulador;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoObligacionTributaria estado = EstadoObligacionTributaria.REGISTRADA;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_financiero", nullable = false, length = 40)
    @Builder.Default
    private EstadoFinancieroObligacion estadoFinanciero = EstadoFinancieroObligacion.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_exigibilidad", nullable = false, length = 40)
    @Builder.Default
    private EstadoExigibilidadObligacion estadoExigibilidad = EstadoExigibilidadObligacion.NO_EXIGIBLE;

    @Column(name = "version_calculo", length = 50)
    private String versionCalculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "norma_aplicable_id")
    private NormaTributariaConfigurada normaAplicable;

    @Column(name = "oficina_responsable_id")
    private Long oficinaResponsableId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_determinacion", columnDefinition = "jsonb")
    private String snapshotDeterminacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "motivo_ajuste", columnDefinition = "TEXT")
    private String motivoAjuste;

    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    @Column(name = "resolucion_sustento", length = 150)
    private String resolucionSustento;

    @OneToMany(mappedBy = "obligacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroCuota ASC")
    @Builder.Default
    private List<CuotaObligacionTributaria> cuotas = new ArrayList<>();

    @OneToMany(mappedBy = "obligacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaRegistro DESC")
    @Builder.Default
    private List<HistorialObligacionTributaria> historial = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
