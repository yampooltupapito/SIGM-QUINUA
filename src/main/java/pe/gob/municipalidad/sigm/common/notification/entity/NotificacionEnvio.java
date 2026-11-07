package pe.gob.municipalidad.sigm.common.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Entidad que modela la cola de envíos de correo bajo el patrón Outbox Transaccional.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"usuario", "cuerpoHtml", "cuerpoTexto"})
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "notificacion_envios")
public class NotificacionEnvio implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "destinatario_correo", nullable = false, length = 150)
    private String destinatarioCorreo;

    @Column(name = "destinatario_nombre", length = 150)
    private String destinatarioNombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "plantilla_codigo", length = 50)
    private String plantillaCodigo;

    @Column(name = "asunto", nullable = false, length = 250)
    private String asunto;

    @Column(name = "cuerpo_html", nullable = false, columnDefinition = "TEXT")
    private String cuerpoHtml;

    @Column(name = "cuerpo_texto", columnDefinition = "TEXT")
    private String cuerpoTexto;

    @Builder.Default
    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo = "INFORMACION";

    @Builder.Default
    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "PENDIENTE";

    @Builder.Default
    @Column(name = "intentos", nullable = false)
    private Integer intentos = 0;

    @Builder.Default
    @Column(name = "max_intentos", nullable = false)
    private Integer maxIntentos = 3;

    @Column(name = "proximo_intento")
    private OffsetDateTime proximoIntento;

    @Column(name = "fecha_envio")
    private OffsetDateTime fechaEnvio;

    @Column(name = "ultimo_error", columnDefinition = "TEXT")
    private String ultimoError;

    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    @Builder.Default
    @Column(name = "origen", length = 100)
    private String origen = "SISTEMA";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
