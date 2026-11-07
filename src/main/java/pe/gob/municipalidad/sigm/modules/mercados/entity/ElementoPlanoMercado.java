package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoElementoPlano;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad que representa un componente geométrico o visual en una versión del plano de mercado.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"versionPlano"})
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "mercados_planos_elementos")
public class ElementoPlanoMercado extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_plano_id", nullable = false)
    private VersionPlanoMercado versionPlano;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_elemento", nullable = false, length = 40)
    private TipoElementoPlano tipoElemento;

    @Column(name = "referencia_entidad_id")
    private UUID referenciaEntidadId;

    @Column(name = "codigo_visual", nullable = false, length = 60)
    private String codigoVisual;

    @Column(name = "etiqueta", length = 150)
    private String etiqueta;

    @Builder.Default
    @Column(name = "coordenada_x", nullable = false, precision = 10, scale = 2)
    private BigDecimal coordenadaX = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "coordenada_y", nullable = false, precision = 10, scale = 2)
    private BigDecimal coordenadaY = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "ancho", nullable = false, precision = 10, scale = 2)
    private BigDecimal ancho = new BigDecimal("60.00");

    @Builder.Default
    @Column(name = "alto", nullable = false, precision = 10, scale = 2)
    private BigDecimal alto = new BigDecimal("60.00");

    @Builder.Default
    @Column(name = "rotacion", nullable = false, precision = 6, scale = 2)
    private BigDecimal rotacion = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "orden_capa", nullable = false)
    private Integer ordenCapa = 0;

    @Column(name = "estilo_configurado", columnDefinition = "TEXT")
    private String estiloConfigurado;
}
