package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoPropuesta;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.ResultadoDecision;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoDecisionExpediente;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mpv_propuestas_decision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropuestaDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private Expediente expediente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_decision", nullable = false, length = 50)
    private TipoDecisionExpediente tipoDecision;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado_propuesto", nullable = false, length = 40)
    private ResultadoDecision resultadoPropuesto;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String fundamento;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_elaborador_id", nullable = false)
    private Usuario usuarioElaborador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id", nullable = false)
    private Oficina oficina;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoPropuesta estado = EstadoPropuesta.BORRADOR;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_envio_revision")
    private OffsetDateTime fechaEnvioRevision;

    @Column(name = "fecha_resolucion_revision")
    private OffsetDateTime fechaResolucionRevision;

    @OneToMany(mappedBy = "propuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RevisionPropuesta> revisiones = new ArrayList<>();

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (fechaCreacion == null) fechaCreacion = OffsetDateTime.now();
        if (estado == null) estado = EstadoPropuesta.BORRADOR;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
