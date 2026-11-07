package pe.gob.municipalidad.sigm.modules.notificaciones.model;

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
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.time.OffsetDateTime;

/**
 * Entidad de persistencia para notificaciones institucionales de usuarios.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "usuario")
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Builder.Default
    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo = "INFO";

    @Builder.Default
    @Column(name = "leida", nullable = false)
    private boolean leida = false;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion = OffsetDateTime.now();

    @Column(name = "fecha_lectura")
    private OffsetDateTime fechaLectura;

    @Column(name = "url_destino", length = 255)
    private String urlDestino;

    @Column(name = "referencia_id", length = 100)
    private String referenciaId;

    @Column(name = "fecha_expiracion")
    private OffsetDateTime fechaExpiracion;

    @Builder.Default
    @Column(name = "origen", length = 100)
    private String origen = "SISTEMA";
}
