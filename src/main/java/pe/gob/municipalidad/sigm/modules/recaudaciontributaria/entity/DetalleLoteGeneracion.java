package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ResultadoGeneracionItem;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Detalle individual de resultado por determinación procesada en un lote.
 */
@Entity
@Table(name = "tributaria_detalles_lote_generacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleLoteGeneracion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private LoteGeneracionObligaciones lote;

    @Column(name = "determinacion_id", nullable = false)
    private UUID determinacionId;

    @Column(name = "codigo_determinacion", length = 50)
    private String codigoDeterminacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private ContribuyenteTributario contribuyente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obligacion_id")
    private ObligacionTributaria obligacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 30)
    private ResultadoGeneracionItem resultado;

    @Column(name = "importe_generado", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importeGenerado = BigDecimal.ZERO;

    @Column(name = "mensaje_detalle", columnDefinition = "TEXT")
    private String mensajeDetalle;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
