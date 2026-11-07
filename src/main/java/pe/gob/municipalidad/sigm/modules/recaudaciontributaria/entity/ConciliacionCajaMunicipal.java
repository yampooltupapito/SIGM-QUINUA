package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConciliacionCaja;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Registro de Arqueo y Conciliación Financiera de una sesión de Caja Municipal.
 */
@Entity
@Table(name = "tributaria_conciliaciones_caja")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConciliacionCajaMunicipal extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_conciliacion", nullable = false, unique = true, length = 50)
    private String codigoConciliacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_caja_id", nullable = false)
    private SesionCajaMunicipal sesionCaja;

    @Column(name = "fecha_conciliacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaConciliacion = OffsetDateTime.now();

    @Column(name = "importe_esperado", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importeEsperado = BigDecimal.ZERO;

    @Column(name = "importe_verificado", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importeVerificado = BigDecimal.ZERO;

    @Column(name = "diferencia", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal diferencia = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoConciliacionCaja estado = EstadoConciliacionCaja.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "desglose_denominaciones_json", columnDefinition = "jsonb")
    private String desgloseDenominacionesJson;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
