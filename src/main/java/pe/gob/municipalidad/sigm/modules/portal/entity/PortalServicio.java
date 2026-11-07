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

/**
 * Entidad que representa los servicios municipales visibles en el catálogo ciudadano.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "portal_servicios")
public class PortalServicio extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "icono", nullable = false, length = 50)
    private String icono;

    @Builder.Default
    @Column(name = "orden", nullable = false)
    private Integer orden = 1;

    @Builder.Default
    @Column(name = "estado_disponibilidad", nullable = false, length = 30)
    private String estadoDisponibilidad = "PROXIMAMENTE";

    @Column(name = "url_acceso", length = 255)
    private String urlAcceso;

    @Builder.Default
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVO";
}
