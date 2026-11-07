package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoEmisionDocumento;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.ResultadoDecision;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoDecisionExpediente;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mpv_decisiones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionExpediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private Expediente expediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propuesta_id")
    private PropuestaDecision propuesta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_decision", nullable = false, length = 50)
    private TipoDecisionExpediente tipoDecision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ResultadoDecision resultado;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String fundamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autoridad_competente_id", nullable = false)
    private Usuario autoridadCompetente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_emisora_id", nullable = false)
    private Oficina oficinaEmisora;

    @Column(name = "cargo_autoridad", length = 150)
    private String cargoAutoridad;

    @Column(name = "fecha_decision", nullable = false)
    private OffsetDateTime fechaDecision;

    @Column(name = "numero_documento", length = 100)
    private String numeroDocumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_emision", nullable = false, length = 30)
    @Builder.Default
    private EstadoEmisionDocumento estadoEmision = EstadoEmisionDocumento.BORRADOR;

    @Column(name = "evidencia_formalizacion", columnDefinition = "TEXT")
    private String evidenciaFormalizacion;

    @Column(name = "fecha_emision")
    private OffsetDateTime fechaEmision;

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
        if (fechaDecision == null) fechaDecision = OffsetDateTime.now();
        if (estadoEmision == null) estadoEmision = EstadoEmisionDocumento.BORRADOR;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
