package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalendarioVencimiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Calendario oficial de cuotas y fechas de vencimiento tributario municipal.
 */
@Entity
@Table(name = "tributaria_calendario_vencimientos", uniqueConstraints = {
        @UniqueConstraint(name = "uq_calendario_concepto_periodo_cuota", columnNames = {"concepto_id", "periodo_id", "numero_cuota"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarioTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concepto_id", nullable = false)
    private ConceptoTributario concepto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periodo_id", nullable = false)
    private PeriodoTributario periodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "norma_id")
    private NormaTributariaConfigurada norma;

    @Column(name = "numero_cuota", nullable = false)
    @Builder.Default
    private Integer numeroCuota = 1;

    @Column(name = "descripcion_cuota", nullable = false, length = 100)
    private String descripcionCuota;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "permite_descuento", nullable = false)
    @Builder.Default
    private Boolean permiteDescuento = false;

    @Column(name = "porcentaje_descuento", precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoCalendarioVencimiento estado = EstadoCalendarioVencimiento.PROGRAMADO;

    @Column(name = "resolucion_prorroga", length = 150)
    private String resolucionProrroga;
}
