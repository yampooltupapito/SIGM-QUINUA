package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoRevisionRequisito;

import java.time.OffsetDateTime;

/**
 * Entidad que registra el estado de verificación de cada requisito documental
 * exigido para un expediente administrativo en trámite.
 */
@Entity
@Table(name = "mpv_revision_requisitos",
        uniqueConstraints = @UniqueConstraint(name = "uq_mpv_rev_req", columnNames = {"expediente_id", "requisito_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevisionRequisito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private Expediente expediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisito_id", nullable = false)
    private RequisitoDocumental requisito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_revision", nullable = false, length = 30)
    @Builder.Default
    private EstadoRevisionRequisito estadoRevision = EstadoRevisionRequisito.PENDIENTE_REVISION;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_revisor_id")
    private Usuario usuarioRevisor;

    @Column(name = "fecha_revision")
    private OffsetDateTime fechaRevision;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (estadoRevision == null) {
            estadoRevision = EstadoRevisionRequisito.PENDIENTE_REVISION;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
