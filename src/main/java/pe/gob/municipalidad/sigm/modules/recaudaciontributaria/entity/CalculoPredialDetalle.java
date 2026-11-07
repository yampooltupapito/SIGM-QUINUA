package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Desglose de valorización física y económica de cada predio involucrado
 * en la determinación del Impuesto Predial del contribuyente.
 */
@Entity
@Table(name = "tributaria_calculos_predial_predios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculoPredialDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculo_id", nullable = false)
    private CalculoImpuestoPredial calculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predio_id", nullable = false)
    private PredioTributario predio;

    @Column(name = "codigo_predio", nullable = false, length = 50)
    private String codigoPredio;

    @Column(name = "direccion_predio", nullable = false, length = 255)
    private String direccionPredio;

    @Column(name = "tipo_predio", nullable = false, length = 30)
    @Builder.Default
    private String tipoPredio = "URBANO";

    @Column(name = "clasificacion_predio", length = 50)
    @Builder.Default
    private String clasificacionPredio = "CASA_HABITACION";

    @Column(name = "porcentaje_participacion", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeParticipacion = new BigDecimal("100.00");

    @Column(name = "area_terreno", nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal areaTerreno = BigDecimal.ZERO;

    @Column(name = "unidad_medida_terreno", nullable = false, length = 10)
    @Builder.Default
    private String unidadMedidaTerreno = "M2";

    @Column(name = "arancel_unitario_m2", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal arancelUnitarioM2 = BigDecimal.ZERO;

    @Column(name = "valor_terreno", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal valorTerreno = BigDecimal.ZERO;

    @Column(name = "cantidad_construcciones", nullable = false)
    @Builder.Default
    private Integer cantidadConstrucciones = 0;

    @Column(name = "area_total_construida", nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal areaTotalConstruida = BigDecimal.ZERO;

    @Column(name = "valor_construcciones", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal valorConstrucciones = BigDecimal.ZERO;

    @Column(name = "valor_instalaciones", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal valorInstalaciones = BigDecimal.ZERO;

    @Column(name = "valor_autovaluo_total", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal valorAutovaluoTotal = BigDecimal.ZERO;

    @Column(name = "valor_autovaluo_imputable", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal valorAutovaluoImputable = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
