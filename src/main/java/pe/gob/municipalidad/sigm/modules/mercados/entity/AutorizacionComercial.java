package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad que representa una autorización o permiso formal para el ejercicio de actividad comercial.
 */
@Entity
@Table(name = "mercados_autorizaciones")
public class AutorizacionComercial extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_id")
    private AsignacionPuesto asignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comerciante_id", nullable = false)
    private Comerciante comerciante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id")
    private PuestoComercial puesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_autorizacion_id")
    private TipoAutorizacion tipoAutorizacionEntity;

    @Column(name = "tipo_autorizacion", nullable = false, length = 50)
    private String tipoAutorizacion = "PADRON_MUNICIPAL";

    @Column(name = "codigo_autorizacion", unique = true, length = 50)
    private String codigoAutorizacion;

    @Column(name = "numero_autorizacion", unique = true, length = 50)
    private String numeroAutorizacion;

    @Column(name = "referencia_documento", length = 100)
    private String referenciaDocumento;

    @Column(name = "fecha_solicitud")
    private LocalDate fechaSolicitud = LocalDate.now();

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoAutorizacion estado = EstadoAutorizacion.BORRADOR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @Column(name = "expediente_id")
    private Long expedienteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_emisora_id")
    private Oficina oficinaEmisora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autoridad_competente_id")
    private Oficina autoridadCompetente;

    @Column(name = "responsable_evaluacion", length = 100)
    private String responsableEvaluacion;

    @Column(name = "responsable_aprobacion", length = 100)
    private String responsableAprobacion;

    @Column(name = "fecha_evaluacion")
    private OffsetDateTime fechaEvaluacion;

    @Column(name = "fecha_aprobacion")
    private OffsetDateTime fechaAprobacion;

    @Column(name = "codigo_seguro", unique = true, length = 100)
    private String codigoSeguro;

    @Column(name = "hash_documento", length = 64)
    private String hashDocumento;

    @Column(name = "motivo_observacion", columnDefinition = "TEXT")
    private String motivoObservacion;

    @Column(name = "motivo_suspension", columnDefinition = "TEXT")
    private String motivoSuspension;

    @Column(name = "motivo_revocacion", columnDefinition = "TEXT")
    private String motivoRevocacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public AutorizacionComercial() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public AsignacionPuesto getAsignacion() { return asignacion; }
    public void setAsignacion(AsignacionPuesto asignacion) { this.asignacion = asignacion; }

    public Comerciante getComerciante() { return comerciante; }
    public void setComerciante(Comerciante comerciante) { this.comerciante = comerciante; }

    public PuestoComercial getPuesto() { return puesto; }
    public void setPuesto(PuestoComercial puesto) { this.puesto = puesto; }

    public TipoAutorizacion getTipoAutorizacionEntity() { return tipoAutorizacionEntity; }
    public void setTipoAutorizacionEntity(TipoAutorizacion tipoAutorizacionEntity) { this.tipoAutorizacionEntity = tipoAutorizacionEntity; }

    public String getTipoAutorizacion() { return tipoAutorizacion; }
    public void setTipoAutorizacion(String tipoAutorizacion) { this.tipoAutorizacion = tipoAutorizacion; }

    public String getCodigoAutorizacion() { return codigoAutorizacion != null ? codigoAutorizacion : numeroAutorizacion; }
    public void setCodigoAutorizacion(String codigoAutorizacion) {
        this.codigoAutorizacion = codigoAutorizacion;
        if (this.numeroAutorizacion == null) {
            this.numeroAutorizacion = codigoAutorizacion;
        }
    }

    public String getNumeroAutorizacion() { return numeroAutorizacion != null ? numeroAutorizacion : codigoAutorizacion; }
    public void setNumeroAutorizacion(String numeroAutorizacion) {
        this.numeroAutorizacion = numeroAutorizacion;
        if (this.codigoAutorizacion == null) {
            this.codigoAutorizacion = numeroAutorizacion;
        }
    }

    public String getReferenciaDocumento() { return referenciaDocumento; }
    public void setReferenciaDocumento(String referenciaDocumento) { this.referenciaDocumento = referenciaDocumento; }

    public LocalDate getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDate fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public EstadoAutorizacion getEstado() { return estado; }
    public void setEstado(EstadoAutorizacion estado) { this.estado = estado; }

    public Documento getDocumento() { return documento; }
    public void setDocumento(Documento documento) { this.documento = documento; }

    public Long getExpedienteId() { return expedienteId; }
    public void setExpedienteId(Long expedienteId) { this.expedienteId = expedienteId; }

    public Oficina getOficinaEmisora() { return oficinaEmisora; }
    public void setOficinaEmisora(Oficina oficinaEmisora) { this.oficinaEmisora = oficinaEmisora; }

    public Oficina getAutoridadCompetente() { return autoridadCompetente; }
    public void setAutoridadCompetente(Oficina autoridadCompetente) { this.autoridadCompetente = autoridadCompetente; }

    public String getResponsableEvaluacion() { return responsableEvaluacion; }
    public void setResponsableEvaluacion(String responsableEvaluacion) { this.responsableEvaluacion = responsableEvaluacion; }

    public String getResponsableAprobacion() { return responsableAprobacion; }
    public void setResponsableAprobacion(String responsableAprobacion) { this.responsableAprobacion = responsableAprobacion; }

    public OffsetDateTime getFechaEvaluacion() { return fechaEvaluacion; }
    public void setFechaEvaluacion(OffsetDateTime fechaEvaluacion) { this.fechaEvaluacion = fechaEvaluacion; }

    public OffsetDateTime getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(OffsetDateTime fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }

    public String getCodigoSeguro() { return codigoSeguro; }
    public void setCodigoSeguro(String codigoSeguro) { this.codigoSeguro = codigoSeguro; }

    public String getHashDocumento() { return hashDocumento; }
    public void setHashDocumento(String hashDocumento) { this.hashDocumento = hashDocumento; }

    public String getMotivoObservacion() { return motivoObservacion; }
    public void setMotivoObservacion(String motivoObservacion) { this.motivoObservacion = motivoObservacion; }

    public String getMotivoSuspension() { return motivoSuspension; }
    public void setMotivoSuspension(String motivoSuspension) { this.motivoSuspension = motivoSuspension; }

    public String getMotivoRevocacion() { return motivoRevocacion; }
    public void setMotivoRevocacion(String motivoRevocacion) { this.motivoRevocacion = motivoRevocacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
