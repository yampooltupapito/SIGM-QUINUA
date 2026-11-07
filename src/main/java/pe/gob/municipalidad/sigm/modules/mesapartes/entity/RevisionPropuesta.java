package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.AccionRevisionPropuesta;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mpv_revision_propuestas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevisionPropuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propuesta_id", nullable = false)
    private PropuestaDecision propuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_revisor_id", nullable = false)
    private Usuario usuarioRevisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id", nullable = false)
    private Oficina oficina;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccionRevisionPropuesta accion;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String observaciones;

    @Column(name = "fecha_revision", nullable = false)
    private OffsetDateTime fechaRevision;

    @PrePersist
    protected void onCreate() {
        if (fechaRevision == null) fechaRevision = OffsetDateTime.now();
    }
}
