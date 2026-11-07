package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoAsignacion;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad que vincula a un comerciante con un puesto comercial en un periodo específico.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"puesto", "comerciante", "documentoSustento"})
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "mercados_asignaciones")
public class AsignacionPuesto extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_asignacion", unique = true, length = 30)
    private String codigoAsignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id", nullable = false)
    private PuestoComercial puesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comerciante_id", nullable = false)
    private Comerciante comerciante;

    @Builder.Default
    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud = LocalDate.now();

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoAsignacion estado = EstadoAsignacion.BORRADOR;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_asignacion", nullable = false, length = 50)
    private TipoAsignacion tipoAsignacion = TipoAsignacion.REGULAR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_sustento_id")
    private Documento documentoSustento;

    @Column(name = "expediente_id")
    private Long expedienteId;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "usuario_registro", length = 100)
    private String usuarioRegistro;

    @Column(name = "usuario_evaluacion", length = 100)
    private String usuarioEvaluacion;

    @Column(name = "fecha_evaluacion")
    private OffsetDateTime fechaEvaluacion;

    @Column(name = "observaciones_evaluacion", columnDefinition = "TEXT")
    private String observacionesEvaluacion;

    @Column(name = "usuario_aprobacion", length = 100)
    private String usuarioAprobacion;

    @Column(name = "fecha_aprobacion")
    private OffsetDateTime fechaAprobacion;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "motivo_suspension", columnDefinition = "TEXT")
    private String motivoSuspension;

    @Column(name = "motivo_finalizacion", columnDefinition = "TEXT")
    private String motivoFinalizacion;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
