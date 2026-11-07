package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoParametroFraccionamiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad que representa un parámetro normativo configurable de un programa de fraccionamiento.
 */
@Entity
@Table(name = "tributaria_parametros_fraccionamiento",
       uniqueConstraints = @UniqueConstraint(name = "uq_trib_param_frac", columnNames = {"programa_id", "codigo", "fecha_inicio_vigencia"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametroFraccionamientoTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", nullable = false)
    private ProgramaFraccionamientoTributario programa;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_parametro", nullable = false, length = 50)
    private TipoParametroFraccionamiento tipoParametro;

    @Column(name = "valor_numerico", precision = 12, scale = 4)
    private BigDecimal valorNumerico;

    @Column(name = "valor_texto", length = 255)
    private String valorTexto;

    @Column(name = "unidad", nullable = false, length = 30)
    private String unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "norma_id")
    private NormaTributariaConfigurada norma;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDate fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private LocalDate fechaFinVigencia;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    private Boolean estado = true;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
