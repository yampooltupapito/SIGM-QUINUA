package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSeguimientoSupervision;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadAdministrativaSupervision;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoRegistroSupervision;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoSeguimientoSupervision;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_supervision_seguimientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeguimientoSupervisionSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_registro_origen", nullable = false, length = 40)
    private TipoRegistroSupervision tipoRegistroOrigen;

    @Column(name = "registro_origen_id", nullable = false)
    private UUID registroOrigenId;

    @Column(name = "registro_origen_codigo", length = 50)
    private String registroOrigenCodigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @Column(name = "oficina_responsable_nombre", length = 150)
    private String oficinaResponsableNombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id", nullable = false)
    private Usuario supervisor;

    @Column(name = "supervisor_nombre", nullable = false, length = 200)
    private String supervisorNombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_asignado_id")
    private Usuario responsableAsignado;

    @Column(name = "responsable_asignado_nombre", length = 200)
    private String responsableAsignadoNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_seguimiento", nullable = false, length = 40)
    private TipoSeguimientoSupervision tipoSeguimiento;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoSeguimientoSupervision estado = EstadoSeguimientoSupervision.ABIERTO;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad_administrativa", nullable = false, length = 20)
    @Builder.Default
    private PrioridadAdministrativaSupervision prioridadAdministrativa = PrioridadAdministrativaSupervision.MEDIA;

    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaCreacion = OffsetDateTime.now();

    @Column(name = "fecha_compromiso")
    private OffsetDateTime fechaCompromiso;

    @Column(name = "fecha_cierre")
    private OffsetDateTime fechaCierre;

    @Column(name = "resultado", columnDefinition = "TEXT")
    private String resultado;

    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    @Column(name = "oficina_escalamiento", length = 150)
    private String oficinaEscalamiento;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToMany(mappedBy = "seguimiento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ObservacionSupervisionSerenazgo> observaciones = new ArrayList<>();

    @OneToMany(mappedBy = "seguimiento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<HistorialSupervisionSerenazgo> historial = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
