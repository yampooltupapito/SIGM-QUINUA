package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuentaCorriente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cuenta Corriente Tributaria única por contribuyente institucional.
 * Centraliza la contabilidad de cargos, abonos y saldo total exigible.
 */
@Entity
@Table(name = "tributaria_cuentas_corrientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaCorrienteTributaria extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false, unique = true)
    private ContribuyenteTributario contribuyente;

    @Column(name = "codigo_cuenta", nullable = false, unique = true, length = 50)
    private String codigoCuenta;

    @Column(name = "moneda", nullable = false, length = 10)
    @Builder.Default
    private String moneda = "PEN";

    @Column(name = "saldo_actual", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal saldoActual = BigDecimal.ZERO;

    @Column(name = "total_cargos", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalCargos = BigDecimal.ZERO;

    @Column(name = "total_abonos", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalAbonos = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoCuentaCorriente estado = EstadoCuentaCorriente.ACTIVA;

    @Column(name = "fecha_apertura", nullable = false)
    @Builder.Default
    private LocalDate fechaApertura = LocalDate.now();

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "cuentaCorriente", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroSecuencia ASC")
    @Builder.Default
    private List<MovimientoCuentaCorrienteTributaria> movimientos = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
