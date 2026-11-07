package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoNotificacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.MedioNotificacion;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mpv_notificaciones_administrativas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionAdministrativa {

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
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @Column(name = "destinatario_nombre", nullable = false)
    private String destinatarioNombre;

    @Column(name = "destinatario_documento", length = 30)
    private String destinatarioDocumento;

    @Column(name = "destinatario_direccion", columnDefinition = "TEXT")
    private String destinatarioDireccion;

    @Column(name = "destinatario_email", length = 150)
    private String destinatarioEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_notificacion", nullable = false, length = 40)
    private MedioNotificacion medioNotificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_notificacion", nullable = false, length = 30)
    @Builder.Default
    private EstadoNotificacion estadoNotificacion = EstadoNotificacion.PENDIENTE;

    @Column(name = "fecha_intento")
    private OffsetDateTime fechaIntento;

    @Column(name = "fecha_practica_valida")
    private OffsetDateTime fechaPracticaValida;

    @Column(name = "numero_acuse_recibo", length = 100)
    private String numeroAcuseRecibo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidencia_documental_id")
    private Documento evidenciaDocumental;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsable_id", nullable = false)
    private Usuario usuarioResponsable;

    @Column(name = "es_legalmente_valida", nullable = false)
    @Builder.Default
    private Boolean esLegalmenteValida = false;

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
        if (estadoNotificacion == null) estadoNotificacion = EstadoNotificacion.PENDIENTE;
        if (esLegalmenteValida == null) esLegalmenteValida = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
