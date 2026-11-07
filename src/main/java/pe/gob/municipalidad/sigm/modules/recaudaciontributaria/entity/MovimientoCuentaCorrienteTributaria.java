package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoMovimientoCuenta;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.NaturalezaMovimiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoMovimientoCuentaCorriente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Libro diario contable de movimientos de cuenta corriente tributaria.
 * Inmutable: no se eliminan movimientos, se registran reversos y ajustes auditados.
 */
@Entity
@Table(name = "tributaria_movimientos_cuenta_corriente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoCuentaCorrienteTributaria extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_corriente_id", nullable = false)
    private CuentaCorrienteTributaria cuentaCorriente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obligacion_id")
    private ObligacionTributaria obligacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuota_id")
    private CuotaObligacionTributaria cuota;

    @Column(name = "numero_secuencia", nullable = false)
    private Long numeroSecuencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 40)
    private TipoMovimientoCuentaCorriente tipoMovimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "naturaleza", nullable = false, length = 20)
    private NaturalezaMovimiento naturaleza;

    @Column(name = "importe", nullable = false, precision = 14, scale = 2)
    private BigDecimal importe;

    @Column(name = "saldo_anterior", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_posterior", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoPosterior;

    @Column(name = "fecha_operacion", nullable = false)
    @Builder.Default
    private LocalDate fechaOperacion = LocalDate.now();

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaRegistro = OffsetDateTime.now();

    @Column(name = "referencia_origen", length = 100)
    private String referenciaOrigen;

    @Column(name = "documento_sustento", length = 150)
    private String documentoSustento;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoMovimientoCuenta estado = EstadoMovimientoCuenta.REGISTRADO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimiento_reverso_id")
    private MovimientoCuentaCorrienteTributaria movimientoReverso;

    @Column(name = "usuario_responsable", length = 100)
    private String usuarioResponsable;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
