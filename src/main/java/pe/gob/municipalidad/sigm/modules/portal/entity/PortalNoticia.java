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
 * Entidad que representa noticias, comunicados y convocatorias institucionales publicadas en el portal.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "portal_noticias")
public class PortalNoticia extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "resumen", nullable = false, length = 500)
    private String resumen;

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Builder.Default
    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo = "NOTICIA";

    @Builder.Default
    @Column(name = "categoria", nullable = false, length = 100)
    private String categoria = "INSTITUCIONAL";

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Builder.Default
    @Column(name = "autor", nullable = false, length = 150)
    private String autor = "Municipalidad Distrital de Quinua";

    @Builder.Default
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "BORRADOR";

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Builder.Default
    @Column(name = "destacada", nullable = false)
    private Boolean destacada = false;

    @Builder.Default
    @Column(name = "visitas", nullable = false)
    private Integer visitas = 0;
}
