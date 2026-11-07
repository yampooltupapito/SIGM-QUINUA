package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Desglose del cálculo de alícuotas y base imponible por cada tramo progresivo
 * acumulativo conforme a la escala del Art. 13 de la Ley de Tributación Municipal.
 */
@Entity
@Table(name = "tributaria_calculos_predial_tramos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculoPredialTramo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculo_id", nullable = false)
    private CalculoImpuestoPredial calculo;

    @Column(name = "numero_tramo", nullable = false)
    private Integer numeroTramo;

    @Column(name = "descripcion_tramo", nullable = false, length = 100)
    private String descripcionTramo;

    @Column(name = "limite_inferior_uit", nullable = false, precision = 8, scale = 2)
    private BigDecimal limiteInferiorUit;

    @Column(name = "limite_superior_uit", precision = 8, scale = 2)
    private BigDecimal limiteSuperiorUit;

    @Column(name = "limite_inferior_monto", nullable = false, precision = 14, scale = 2)
    private BigDecimal limiteInferiorMonto;

    @Column(name = "limite_superior_monto", precision = 14, scale = 2)
    private BigDecimal limiteSuperiorMonto;

    @Column(name = "alicuota_porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal alicuotaPorcentaje;

    @Column(name = "base_imponible_tramo", nullable = false, precision = 14, scale = 2)
    private BigDecimal baseImponibleTramo;

    @Column(name = "impuesto_tramo", nullable = false, precision = 14, scale = 2)
    private BigDecimal impuestoTramo;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
