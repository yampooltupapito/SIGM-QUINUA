package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConfiguracionArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MetodoDistribucionArbitrio;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.SectorPatrullaje;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tributaria_configuraciones_arbitrio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionArbitrioMunicipal extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_configuracion", nullable = false, unique = true, length = 60)
    private String codigoConfiguracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private ServicioArbitrioMunicipal servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periodo_id", nullable = false)
    private PeriodoTributario periodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "norma_id")
    private NormaTributariaConfigurada norma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private SectorPatrullaje sector;

    @Column(name = "zona", length = 60)
    private String zona;

    @Column(name = "version_configuracion", nullable = false)
    @Builder.Default
    private Integer versionConfiguracion = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuracion_predecesora_id")
    private ConfiguracionArbitrioMunicipal configuracionPredecesora;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDate fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private LocalDate fechaFinVigencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoConfiguracionArbitrio estado = EstadoConfiguracionArbitrio.BORRADOR;

    @Column(name = "costo_total_anual", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal costoTotalAnual = BigDecimal.ZERO;

    @Column(name = "costo_total_mensual", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal costoTotalMensual = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodologia_distribucion", nullable = false, length = 50)
    @Builder.Default
    private MetodoDistribucionArbitrio metodologiaDistribucion = MetodoDistribucionArbitrio.USO_PREDIO_ACTIVIDAD;

    @Column(name = "cantidad_predios_afectos", nullable = false)
    @Builder.Default
    private Integer cantidadPrediosAfectos = 0;

    @Column(name = "descripcion_justificacion", columnDefinition = "TEXT")
    private String descripcionJustificacion;

    @Column(name = "usuario_aprobador", length = 100)
    private String usuarioAprobador;

    @Column(name = "fecha_aprobacion")
    private OffsetDateTime fechaAprobacion;

    @Column(name = "resolucion_aprobacion", length = 150)
    private String resolucionAprobacion;

    @Column(name = "usuario_activador", length = 100)
    private String usuarioActivador;

    @Column(name = "fecha_activacion")
    private OffsetDateTime fechaActivacion;

    @Column(name = "motivo_desactivacion", columnDefinition = "TEXT")
    private String motivoDesactivacion;

    @OneToMany(mappedBy = "configuracion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CostoServicioArbitrio> costos = new ArrayList<>();

    @OneToMany(mappedBy = "configuracion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CriterioDistribucionArbitrio> criterios = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
