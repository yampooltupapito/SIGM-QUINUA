package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoServicioArbitrio;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tributaria_calculos_arbitrio_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCalculoArbitrio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculo_id", nullable = false)
    private CalculoArbitrioMunicipal calculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private ServicioArbitrioMunicipal servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuracion_id")
    private ConfiguracionArbitrioMunicipal configuracion;

    @Column(name = "codigo_servicio", nullable = false, length = 50)
    private String codigoServicio;

    @Column(name = "nombre_servicio", nullable = false, length = 150)
    private String nombreServicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", nullable = false, length = 40)
    private TipoServicioArbitrio tipoServicio;

    @Column(name = "criterio_aplicado", nullable = false, length = 150)
    private String criterioAplicado;

    @Column(name = "parametro_base_valor", nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal parametroBaseValor = BigDecimal.ZERO;

    @Column(name = "unidad_base", nullable = false, length = 40)
    @Builder.Default
    private String unidadBase = "COEFICIENTE";

    @Column(name = "tasa_unitario_mensual", nullable = false, precision = 14, scale = 4)
    @Builder.Default
    private BigDecimal tasaUnitarioMensual = BigDecimal.ZERO;

    @Column(name = "costo_mensual_servicio", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal costoMensualServicio = BigDecimal.ZERO;

    @Column(name = "costo_anual_servicio", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal costoAnualServicio = BigDecimal.ZERO;

    @Column(name = "descuento_aplicado", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal descuentoAplicado = BigDecimal.ZERO;

    @Column(name = "importe_final_mensual", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importeFinalMensual = BigDecimal.ZERO;

    @Column(name = "importe_final_anual", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importeFinalAnual = BigDecimal.ZERO;

    @Column(name = "explicacion_detalle", columnDefinition = "TEXT")
    private String explicacionDetalle;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
