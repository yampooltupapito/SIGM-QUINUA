package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad que representa un puesto o local comercial dentro de un mercado municipal.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"mercado", "sector", "pabellon", "zona", "tipoPuesto"})
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(
        name = "mercados_puestos",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_mercados_puestos_mercado_codigo",
                columnNames = {"mercado_id", "codigo"}
        )
)
public class PuestoComercial extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mercado_id", nullable = false)
    private MercadoMunicipal mercado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private SectorMercado sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pabellon_id")
    private PabellonMercado pabellon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id")
    private ZonaComercial zona;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "numero", nullable = false, length = 30)
    private String numero;

    @Column(name = "nombre_referencial", length = 150)
    private String nombreReferencial;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "area_metros_cuadrados", precision = 8, scale = 2)
    private BigDecimal areaMetrosCuadrados;

    @Column(name = "ancho_metros", precision = 8, scale = 2)
    private BigDecimal anchoMetros;

    @Column(name = "largo_metros", precision = 8, scale = 2)
    private BigDecimal largoMetros;

    @Builder.Default
    @Column(name = "nivel", nullable = false)
    private Integer nivel = 1;

    @Column(name = "ubicacion_referencia", length = 255)
    private String ubicacionReferencia;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_fisico", nullable = false, length = 30)
    private EstadoFisicoPuesto estadoFisico = EstadoFisicoPuesto.OPERATIVO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoPuesto estado = EstadoPuesto.DISPONIBLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_puesto_id")
    private TipoPuestoComercial tipoPuesto;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public boolean isDisponible() {
        return this.estado == EstadoPuesto.DISPONIBLE && this.estadoFisico == EstadoFisicoPuesto.OPERATIVO;
    }
}
