package pe.gob.municipalidad.sigm.modules.portal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;

import java.time.LocalDateTime;

/**
 * Entidad que almacena mensajes y consultas ciudadanas recibidas a través del portal público.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "portal_contactos")
public class PortalContacto extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "remitente_nombre", nullable = false, length = 150)
    private String remitenteNombre;

    @Column(name = "remitente_correo", nullable = false, length = 150)
    private String remitenteCorreo;

    @Column(name = "remitente_telefono", length = 20)
    private String remitenteTelefono;

    @Column(name = "asunto", nullable = false, length = 200)
    private String asunto;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Builder.Default
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "respuesta_interna", columnDefinition = "TEXT")
    private String respuestaInterna;

    @Column(name = "atendido_por", length = 100)
    private String atendidoPor;

    @Column(name = "atendido_at")
    private LocalDateTime atendidoAt;

    @Column(name = "ip_origen", length = 50)
    private String ipOrigen;
}
