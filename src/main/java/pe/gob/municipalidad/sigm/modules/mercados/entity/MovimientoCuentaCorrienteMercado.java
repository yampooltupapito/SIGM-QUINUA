package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoMovimientoCuentaCorriente;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "mercados_cuenta_corriente_movimientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoCuentaCorrienteMercado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comerciante_id", nullable = false)
    private Comerciante comerciante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id")
    private PuestoComercial puesto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private TipoMovimientoCuentaCorriente tipoMovimiento;

    @Column(name = "concepto", nullable = false, length = 150)
    private String concepto;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "saldo_resultante", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoResultante;

    @Column(name = "referencia_id")
    private UUID referenciaId;

    @Column(name = "tipo_referencia", length = 50)
    private String tipoReferencia;

    @Column(name = "fecha_movimiento", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaMovimiento = OffsetDateTime.now();

    @Column(name = "usuario", nullable = false, length = 100)
    private String usuario;
}
