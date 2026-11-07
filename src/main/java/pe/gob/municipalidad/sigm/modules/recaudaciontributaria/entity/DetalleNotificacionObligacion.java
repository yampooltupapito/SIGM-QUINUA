package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Detalle de obligaciones tributarias individuales agrupadas en una notificación.
 */
@Entity
@Table(name = "tributaria_detalles_notificacion_obligacion")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class DetalleNotificacionObligacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacion_id", nullable = false)
    private NotificacionTributaria notificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obligacion_id", nullable = false)
    private ObligacionTributaria obligacion;

    @Column(name = "periodo", length = 30)
    private String periodo;

    @Column(name = "monto_insoluto", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal montoInsoluto = BigDecimal.ZERO;

    @Column(name = "monto_interes", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal montoInteres = BigDecimal.ZERO;

    @Column(name = "monto_total", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal montoTotal = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;
}

