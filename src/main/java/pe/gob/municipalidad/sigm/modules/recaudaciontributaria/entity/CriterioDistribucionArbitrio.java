package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MetodoDistribucionArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDatoCriterio;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tributaria_criterios_distribucion_arbitrio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriterioDistribucionArbitrio extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuracion_id", nullable = false)
    private ConfiguracionArbitrioMunicipal configuracion;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dato", nullable = false, length = 30)
    @Builder.Default
    private TipoDatoCriterio tipoDato = TipoDatoCriterio.NUMERICO;

    @Column(name = "unidad", nullable = false, length = 40)
    @Builder.Default
    private String unidad = "COEFICIENTE";

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_distribucion", nullable = false, length = 50)
    private MetodoDistribucionArbitrio metodoDistribucion;

    @Column(name = "ponderacion_porcentaje", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal ponderacionPorcentaje = new BigDecimal("100.00");

    @Column(name = "factor_minimo", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal factorMinimo = BigDecimal.ZERO;

    @Column(name = "factor_maximo", precision = 10, scale = 4)
    private BigDecimal factorMaximo;

    @Column(name = "fuente_normativa", length = 255)
    private String fuenteNormativa;

    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private String estado = "ACTIVO";
}
