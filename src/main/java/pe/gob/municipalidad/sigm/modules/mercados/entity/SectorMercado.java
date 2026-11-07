package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;

import java.util.UUID;

/**
 * Entidad que representa un sector dentro de un mercado municipal.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "mercado")
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(
        name = "mercados_sectores",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_mercados_sectores_mercado_codigo",
                columnNames = {"mercado_id", "codigo"}
        )
)
public class SectorMercado extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mercado_id", nullable = false)
    private MercadoMunicipal mercado;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "categoria_comercial", length = 100)
    private String categoriaComercial;

    @Builder.Default
    @Column(name = "tipo_sector", nullable = false, length = 50)
    private String tipoSector = "GENERAL";

    @Builder.Default
    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "ACTIVO";

    @Builder.Default
    @Column(name = "orden_visual", nullable = false)
    private Integer ordenVisual = 0;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
