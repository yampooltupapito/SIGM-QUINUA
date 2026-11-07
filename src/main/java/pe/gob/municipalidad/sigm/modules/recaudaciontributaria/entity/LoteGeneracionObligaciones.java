package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoLoteGeneracion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDeterminacionTributaria;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que registra la ejecución de un lote controlado de generación de obligaciones tributarias.
 */
@Entity
@Table(name = "tributaria_lotes_generacion_obligaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoteGeneracionObligaciones extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_lote", nullable = false, unique = true, length = 50)
    private String codigoLote;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_determinacion", nullable = false, length = 50)
    private TipoDeterminacionTributaria tipoDeterminacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periodo_id", nullable = false)
    private PeriodoTributario periodo;

    @Column(name = "total_evaluadas", nullable = false)
    @Builder.Default
    private Integer totalEvaluadas = 0;

    @Column(name = "total_elegibles", nullable = false)
    @Builder.Default
    private Integer totalElegibles = 0;

    @Column(name = "total_generadas", nullable = false)
    @Builder.Default
    private Integer totalGeneradas = 0;

    @Column(name = "total_duplicadas", nullable = false)
    @Builder.Default
    private Integer totalDuplicadas = 0;

    @Column(name = "total_errores", nullable = false)
    @Builder.Default
    private Integer totalErrores = 0;

    @Column(name = "importe_total_generado", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importeTotalGenerado = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoLoteGeneracion estado = EstadoLoteGeneracion.EN_PROCESO;

    @Column(name = "usuario_registro", length = 100)
    private String usuarioRegistro;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleLoteGeneracion> detalles = new ArrayList<>();
}
