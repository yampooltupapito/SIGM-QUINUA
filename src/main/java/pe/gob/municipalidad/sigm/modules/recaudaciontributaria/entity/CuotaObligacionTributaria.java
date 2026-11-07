package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuotaTributaria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Cuota individual en la que se desglosa una obligación tributaria municipal.
 */
@Entity
@Table(name = "tributaria_obligaciones_cuotas",
       uniqueConstraints = @UniqueConstraint(name = "uq_cuota_obligacion_num", columnNames = {"obligacion_id", "numero_cuota"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuotaObligacionTributaria extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obligacion_id", nullable = false)
    private ObligacionTributaria obligacion;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @Column(name = "importe", nullable = false, precision = 14, scale = 2)
    private BigDecimal importe;

    @Column(name = "importe_pagado", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importePagado = BigDecimal.ZERO;

    @Column(name = "saldo", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldo;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoCuotaTributaria estado = EstadoCuotaTributaria.PENDIENTE;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
