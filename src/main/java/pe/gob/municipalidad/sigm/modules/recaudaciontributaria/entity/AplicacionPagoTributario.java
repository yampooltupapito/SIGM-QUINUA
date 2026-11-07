package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAplicacionPago;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Imputación o aplicación financiera de una fracción o totalidad de un pago a una obligación/cuota tributaria.
 */
@Entity
@Table(name = "tributaria_aplicaciones_pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AplicacionPagoTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false)
    private PagoTributario pago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obligacion_id", nullable = false)
    private ObligacionTributaria obligacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuota_id")
    private CuotaObligacionTributaria cuota;

    @Column(name = "importe_aplicado", nullable = false, precision = 14, scale = 2)
    private BigDecimal importeAplicado;

    @Column(name = "fecha_hora_aplicacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaHoraAplicacion = OffsetDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_aplicacion_id")
    private Usuario usuarioAplicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimiento_cuenta_id")
    private MovimientoCuentaCorrienteTributaria movimientoCuenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoAplicacionPago estado = EstadoAplicacionPago.ACTIVA;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
