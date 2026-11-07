package pe.gob.municipalidad.sigm.modules.administracion.perfil.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;

/**
 * Entidad que almacena las preferencias individuales de interfaz y notificaciones
 * para cada usuario de SIGM-QUINUA.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "usuario")
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "usuario_preferencias")
public class UsuarioPreferencia extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Builder.Default
    @Column(name = "tema", nullable = false, length = 20)
    private String tema = "OSCURO"; // OSCURO, CLARO, SISTEMA

    @Builder.Default
    @Column(name = "idioma", nullable = false, length = 10)
    private String idioma = "es";

    @Builder.Default
    @Column(name = "notif_email", nullable = false)
    private Boolean notifEmail = true;

    @Builder.Default
    @Column(name = "notif_sistema", nullable = false)
    private Boolean notifSistema = true;

    @Builder.Default
    @Column(name = "page_size", nullable = false)
    private Integer pageSize = 10;

    @Builder.Default
    @Column(name = "sidebar_colapsado", nullable = false)
    private Boolean sidebarColapsado = false;
}
