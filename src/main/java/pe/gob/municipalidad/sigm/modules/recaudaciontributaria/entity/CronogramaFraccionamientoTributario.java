package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCronogramaFraccionamiento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa el Cronograma de amortización y cuotas de un fraccionamiento.
 */
@Entity
@Table(name = "tributaria_cronogramas_fraccionamiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CronogramaFraccionamientoTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudFraccionamientoTributario solicitud;

    @Column(name = "version_cronograma", nullable = false)
    @Builder.Default
    private Integer versionCronograma = 1;

    @Column(name = "importe_base", nullable = false, precision = 14, scale = 2)
    private BigDecimal importeBase;

    @Column(name = "importe_cuota_inicial", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importeCuotaInicial = BigDecimal.ZERO;

    @Column(name = "importe_financiado", nullable = false, precision = 14, scale = 2)
    private BigDecimal importeFinanciado;

    @Column(name = "importe_accesorios", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importeAccesorios = BigDecimal.ZERO;

    @Column(name = "importe_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal importeTotal;

    @Column(name = "numero_cuotas", nullable = false)
    private Integer numeroCuotas;

    @Column(name = "tasa_interes_aplicada", nullable = false, precision = 8, scale = 4)
    @Builder.Default
    private BigDecimal tasaInteresAplicada = BigDecimal.ZERO;

    @Column(name = "fecha_generacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaGeneracion = OffsetDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoCronogramaFraccionamiento estado = EstadoCronogramaFraccionamiento.SIMULADO;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToMany(mappedBy = "cronograma")
    @OrderBy("numeroCuota ASC")
    @Builder.Default
    private List<CuotaFraccionamientoTributario> cuotas = new ArrayList<>();
}
