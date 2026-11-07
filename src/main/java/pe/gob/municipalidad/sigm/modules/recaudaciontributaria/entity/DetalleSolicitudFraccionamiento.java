package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoValidacionDetalleFraccionamiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad que vincula y valida cada obligación tributaria incorporada a una solicitud.
 */
@Entity
@Table(name = "tributaria_detalles_solicitud_fraccionamiento",
       uniqueConstraints = @UniqueConstraint(name = "uq_trib_det_sol_obl", columnNames = {"solicitud_id", "obligacion_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleSolicitudFraccionamiento extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudFraccionamientoTributario solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obligacion_id", nullable = false)
    private ObligacionTributaria obligacion;

    @Column(name = "saldo_evaluado", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoEvaluado;

    @Column(name = "fecha_corte", nullable = false)
    private LocalDate fechaCorte;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion", nullable = false, length = 30)
    @Builder.Default
    private EstadoValidacionDetalleFraccionamiento estadoValidacion = EstadoValidacionDetalleFraccionamiento.CONFORME;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
