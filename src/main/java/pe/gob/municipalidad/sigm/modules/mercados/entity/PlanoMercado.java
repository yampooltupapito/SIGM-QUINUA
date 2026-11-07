package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPlano;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa el plano general o maestro de un mercado municipal.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"mercado", "versionActual", "versiones"})
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "mercados_planos")
public class PlanoMercado extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mercado_id", nullable = false)
    private MercadoMunicipal mercado;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Builder.Default
    @Column(name = "ancho_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal anchoBase = new BigDecimal("1400.00");

    @Builder.Default
    @Column(name = "alto_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal altoBase = new BigDecimal("900.00");

    @Builder.Default
    @Column(name = "unidad_referencia", nullable = false, length = 30)
    private String unidadReferencia = "PIXEL";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoPlano estado = EstadoPlano.ACTIVO;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_actual_id")
    private VersionPlanoMercado versionActual;

    @Builder.Default
    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroVersion DESC")
    private List<VersionPlanoMercado> versiones = new ArrayList<>();

    @Version
    @Column(name = "version")
    private Long version;
}
