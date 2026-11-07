package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoInteresTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.UnidadTemporalInteres;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Parámetro normativo formal de tasas de interés moratorio (TIM) o compensatorio.
 * Conforme al Art. 33 del TUO del Código Tributario peruano (D.S. 133-2013-EF).
 */
@Entity
@Table(name = "tributaria_parametros_interes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class ParametroInteresTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concepto_id")
    private ConceptoTributario concepto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_interes", nullable = false, length = 30)
    private TipoInteresTributario tipoInteres;

    @Column(name = "tasa", nullable = false, precision = 8, scale = 4)
    private BigDecimal tasa;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_temporal", nullable = false, length = 20)
    @Builder.Default
    private UnidadTemporalInteres unidadTemporal = UnidadTemporalInteres.MENSUAL;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDate fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private LocalDate fechaFinVigencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "norma_id")
    private NormaTributariaConfigurada norma;

    @Column(name = "base_legal", nullable = false)
    private String baseLegal;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    private Boolean estado = true;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
