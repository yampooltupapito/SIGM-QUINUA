package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoRelacionContribuyentePredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoRelacionContribuyentePredio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tributaria_relaciones_contribuyente_predio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelacionContribuyentePredio extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private ContribuyenteTributario contribuyente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predio_id", nullable = false)
    private PredioTributario predio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_relacion", nullable = false, length = 40)
    private TipoRelacionContribuyentePredio tipoRelacion;

    @Column(name = "porcentaje_participacion", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeParticipacion = new BigDecimal("100.00");

    @Column(name = "fecha_inicio", nullable = false)
    @Builder.Default
    private LocalDate fechaInicio = LocalDate.now();

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "es_principal", nullable = false)
    @Builder.Default
    private Boolean esPrincipal = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoRelacionContribuyentePredio estado = EstadoRelacionContribuyentePredio.ACTIVO;

    @Column(name = "documento_sustento_id")
    private UUID documentoSustentoId;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
