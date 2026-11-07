package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCierreExpediente;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mpv_cierres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CierreExpediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private Expediente expediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id")
    private DecisionExpediente decision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cierre_id", nullable = false)
    private Usuario usuarioCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_cierre_id", nullable = false)
    private Oficina oficinaCierre;

    @Column(name = "motivo_cierre", columnDefinition = "TEXT", nullable = false)
    private String motivoCierre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cierre", nullable = false, length = 40)
    private TipoCierreExpediente tipoCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_cierre_id")
    private Documento documentoCierre;

    @Column(name = "fecha_cierre", nullable = false)
    private OffsetDateTime fechaCierre;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (fechaCierre == null) fechaCierre = OffsetDateTime.now();
    }
}
