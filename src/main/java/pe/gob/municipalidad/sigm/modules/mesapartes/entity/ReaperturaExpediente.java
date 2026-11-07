package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mpv_reaperturas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReaperturaExpediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private Expediente expediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_reapertura_id", nullable = false)
    private Usuario usuarioReapertura;

    @Column(name = "motivo_legal", columnDefinition = "TEXT", nullable = false)
    private String motivoLegal;

    @Column(name = "acto_reapertura_referencia", length = 150)
    private String actoReaperturaReferencia;

    @Column(name = "fecha_reapertura", nullable = false)
    private OffsetDateTime fechaReapertura;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (fechaReapertura == null) fechaReapertura = OffsetDateTime.now();
    }
}
