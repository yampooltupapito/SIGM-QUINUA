package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Catálogo administrable de categorías comerciales para sectores y puestos municipales.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_categorias_comerciales")
public class CategoriaComercial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Builder.Default
    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "ACTIVO";

    @Builder.Default
    @Column(name = "orden_visual", nullable = false)
    private Integer ordenVisual = 0;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
