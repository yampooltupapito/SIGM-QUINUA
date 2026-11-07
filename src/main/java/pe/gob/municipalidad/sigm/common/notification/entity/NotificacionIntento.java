package pe.gob.municipalidad.sigm.common.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Entidad que registra cada intento de despacho de correo electrónico hacia un proveedor SMTP/API.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "envio")
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "notificacion_intentos")
public class NotificacionIntento implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envio_id", nullable = false)
    private NotificacionEnvio envio;

    @Column(name = "numero_intento", nullable = false)
    private Integer numeroIntento;

    @Column(name = "proveedor", nullable = false, length = 50)
    private String proveedor;

    @Column(name = "exitoso", nullable = false)
    private Boolean exitoso;

    @Column(name = "mensaje_respuesta", columnDefinition = "TEXT")
    private String mensajeRespuesta;

    @Builder.Default
    @Column(name = "fecha_intento", nullable = false)
    private OffsetDateTime fechaIntento = OffsetDateTime.now();
}
