package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuotaFraccionamiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad que representa cada cuota mensual o cuota inicial del cronograma.
 */
@Entity
@Table(name = "tributaria_cuotas_fraccionamiento",
       uniqueConstraints = @UniqueConstraint(name = "uq_trib_cuota_num", columnNames = {"cronograma_id", "numero_cuota"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuotaFraccionamientoTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cronograma_id", nullable = false)
    private CronogramaFraccionamientoTributario cronograma;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "importe_capital", nullable = false, precision = 14, scale = 2)
    private BigDecimal importeCapital;

    @Column(name = "importe_interes", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importeInteres = BigDecimal.ZERO;

    @Column(name = "importe_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal importeTotal;

    @Column(name = "importe_pagado", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importePagado = BigDecimal.ZERO;

    @Column(name = "saldo", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoCuotaFraccionamiento estado = EstadoCuotaFraccionamiento.PENDIENTE;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
