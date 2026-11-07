package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoSolicitudGestion;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad principal para la gestión de solicitudes de renovación, cambio de titular y transferencias autorizadas.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"mercado", "puesto", "asignacionOrigen", "contratoOrigen", "autorizacionOrigen",
        "comercianteActual", "comerciantePropuesto", "asignacionNueva", "contratoNuevo", "autorizacionNueva",
        "observaciones", "evaluaciones"})
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_solicitudes_gestion")
public class SolicitudGestionOcupacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo_solicitud", nullable = false, unique = true, length = 50)
    private String codigoSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_solicitud", nullable = false, length = 50)
    private TipoSolicitudGestion tipoSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimiento_id")
    private ProcedimientoGestionOcupacion procedimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mercado_id", nullable = false)
    private MercadoMunicipal mercado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id", nullable = false)
    private PuestoComercial puesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_origen_id", nullable = false)
    private AsignacionPuesto asignacionOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_origen_id")
    private ContratoOcupacion contratoOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autorizacion_origen_id")
    private AutorizacionComercial autorizacionOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comerciante_actual_id", nullable = false)
    private Comerciante comercianteActual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comerciante_propuesto_id")
    private Comerciante comerciantePropuesto;

    @Builder.Default
    @Column(name = "fecha_presentacion", nullable = false)
    private LocalDate fechaPresentacion = LocalDate.now();

    @Column(name = "fecha_inicio_solicitada")
    private LocalDate fechaInicioSolicitada;

    @Column(name = "fecha_fin_solicitada")
    private LocalDate fechaFinSolicitada;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    public String getMotivoSolicitud() {
        return motivo;
    }

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoSolicitudGestion estado = EstadoSolicitudGestion.BORRADOR;

    @Column(name = "expediente_id")
    private Long expedienteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @Column(name = "usuario_registro", length = 100)
    private String usuarioRegistro;

    @Column(name = "usuario_evaluacion", length = 100)
    private String usuarioEvaluacion;

    @Column(name = "fecha_evaluacion")
    private OffsetDateTime fechaEvaluacion;

    @Column(name = "informe_evaluacion", columnDefinition = "TEXT")
    private String informeEvaluacion;

    @Column(name = "usuario_decision", length = 100)
    private String usuarioDecision;

    @Column(name = "fecha_decision")
    private OffsetDateTime fechaDecision;

    @Column(name = "motivo_decision", columnDefinition = "TEXT")
    private String motivoDecision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_decision_id")
    private Documento documentoDecision;

    @Column(name = "usuario_formalizacion", length = 100)
    private String usuarioFormalizacion;

    @Column(name = "fecha_formalizacion")
    private OffsetDateTime fechaFormalizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_nueva_id")
    private AsignacionPuesto asignacionNueva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_nuevo_id")
    private ContratoOcupacion contratoNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autorizacion_nueva_id")
    private AutorizacionComercial autorizacionNueva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_formalizacion_id")
    private Documento documentoFormalizacion;

    @Column(name = "token_verificacion_qr", unique = true, length = 100)
    private String tokenVerificacionQr;

    @Builder.Default
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObservacionGestionOcupacion> observaciones = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvaluacionGestionOcupacion> evaluaciones = new ArrayList<>();

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
