package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoSolicitudFraccionamiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa la Solicitud formal de Facilidades de Pago / Fraccionamiento.
 */
@Entity
@Table(name = "tributaria_solicitudes_fraccionamiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudFraccionamientoTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_solicitud", nullable = false, unique = true, length = 50)
    private String codigoSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private ContribuyenteTributario contribuyente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", nullable = false)
    private ProgramaFraccionamientoTributario programa;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoSolicitudFraccionamiento estado = EstadoSolicitudFraccionamiento.PRESENTADA;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "numero_cuotas_propuestas", nullable = false)
    private Integer numeroCuotasPropuestas;

    @Column(name = "monto_total_deuda", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal montoTotalDeuda = BigDecimal.ZERO;

    @Column(name = "monto_cuota_inicial", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal montoCuotaInicial = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id")
    private Oficina oficina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluador_id")
    private Usuario evaluador;

    @Column(name = "fecha_evaluacion")
    private OffsetDateTime fechaEvaluacion;

    @Column(name = "observacion_evaluacion", columnDefinition = "TEXT")
    private String observacionEvaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_sustento_id")
    private Documento documentoSustento;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleSolicitudFraccionamiento> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "solicitud")
    @Builder.Default
    private List<CronogramaFraccionamientoTributario> cronogramas = new ArrayList<>();
}
