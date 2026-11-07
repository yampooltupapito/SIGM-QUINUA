package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoZona;

import java.util.UUID;

/**
 * Entidad que representa una zona física o funcional dentro de un mercado municipal
 * (zonas comerciales asignables, áreas comunes, circulación, almacenamiento, etc.).
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
        name = "mercados_zonas",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_mercados_zonas_mercado_codigo",
                columnNames = {"mercado_id", "codigo"}
        )
)
public class ZonaComercial extends AuditableEntity {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_zona", nullable = false, length = 50)
    private TipoZona tipoZona;

    @Builder.Default
    @Column(name = "es_asignable", nullable = false)
    private Boolean esAsignable = true;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoMercado estado = EstadoMercado.ACTIVO;

    @Builder.Default
    @Column(name = "orden_visual", nullable = false)
    private Integer ordenVisual = 0;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
