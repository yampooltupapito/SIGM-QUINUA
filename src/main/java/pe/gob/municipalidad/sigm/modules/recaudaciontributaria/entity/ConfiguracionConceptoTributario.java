package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConfiguracionConcepto;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.SectorPatrullaje;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Configuración paramétrica y normativa versionada de un concepto tributario para un período fiscal.
 */
@Entity
@Table(name = "tributaria_configuraciones_concepto", uniqueConstraints = {
        @UniqueConstraint(name = "uq_concepto_periodo_version", columnNames = {"concepto_id", "periodo_id", "numero_version"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionConceptoTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concepto_id", nullable = false)
    private ConceptoTributario concepto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periodo_id", nullable = false)
    private PeriodoTributario periodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "norma_id")
    private NormaTributariaConfigurada norma;

    @Column(name = "numero_version", nullable = false)
    @Builder.Default
    private Integer numeroVersion = 1;

    @Column(name = "version_anterior_id")
    private UUID versionAnteriorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoConfiguracionConcepto estado = EstadoConfiguracionConcepto.BORRADOR;

    @Column(name = "descripcion_motivo", columnDefinition = "TEXT")
    private String descripcionMotivo;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDate fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private LocalDate fechaFinVigencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private SectorPatrullaje sector;

    @Column(name = "zona", length = 50)
    private String zona;

    @Column(name = "oficina_responsable_id")
    private Long oficinaResponsableId;

    @Column(name = "usuario_creador", length = 100)
    private String usuarioCreador;

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

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToMany(mappedBy = "configuracion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordenVisual ASC")
    @Builder.Default
    private List<ParametroTributario> parametros = new ArrayList<>();

    @OneToMany(mappedBy = "configuracion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordenAplicacion ASC")
    @Builder.Default
    private List<ReglaTributariaConfigurada> reglas = new ArrayList<>();

    @OneToMany(mappedBy = "configuracion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaRegistro DESC")
    @Builder.Default
    private List<HistorialConfiguracionTributaria> historial = new ArrayList<>();

    public boolean esEditable() {
        return this.estado == EstadoConfiguracionConcepto.BORRADOR;
    }

    public boolean isEditable() {
        return esEditable();
    }

    public boolean esAprobadoOActivo() {
        return this.estado == EstadoConfiguracionConcepto.APROBADO || this.estado == EstadoConfiguracionConcepto.ACTIVO;
    }
}
