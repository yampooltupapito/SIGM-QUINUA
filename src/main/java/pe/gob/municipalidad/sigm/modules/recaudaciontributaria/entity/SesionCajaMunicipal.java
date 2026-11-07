package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoSesionCaja;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Turno o sesión operativa de una Caja Municipal aperturada por un cajero autorizado.
 */
@Entity
@Table(name = "tributaria_sesiones_caja")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionCajaMunicipal extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_sesion", nullable = false, unique = true, length = 50)
    private String codigoSesion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id", nullable = false)
    private CajaMunicipal caja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cajero_id", nullable = false)
    private Usuario cajero;

    @Column(name = "fecha_apertura", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaApertura = OffsetDateTime.now();

    @Column(name = "fecha_cierre")
    private OffsetDateTime fechaCierre;

    @Column(name = "monto_apertura", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal montoApertura = BigDecimal.ZERO;

    @Column(name = "monto_cierre_declarado", precision = 14, scale = 2)
    private BigDecimal montoCierreDeclarado;

    @Column(name = "monto_cierre_calculado", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal montoCierreCalculado = BigDecimal.ZERO;

    @Column(name = "diferencia", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal diferencia = BigDecimal.ZERO;

    @Column(name = "total_recaudado_efectivo", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalRecaudadoEfectivo = BigDecimal.ZERO;

    @Column(name = "total_recaudado_otros_medios", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalRecaudadoOtrosMedios = BigDecimal.ZERO;

    @Column(name = "total_recaudado_general", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalRecaudadoGeneral = BigDecimal.ZERO;

    @Column(name = "total_operaciones", nullable = false)
    @Builder.Default
    private Integer totalOperaciones = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoSesionCaja estado = EstadoSesionCaja.ABIERTA;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "justificacion_diferencia", columnDefinition = "TEXT")
    private String justificacionDiferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private Usuario supervisor;

    @OneToMany(mappedBy = "sesionCaja")
    @OrderBy("fechaHoraPago DESC")
    @Builder.Default
    private List<PagoTributario> pagos = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
