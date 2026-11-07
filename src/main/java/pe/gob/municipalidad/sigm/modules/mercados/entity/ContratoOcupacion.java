package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad que representa un contrato de ocupación o uso de puesto comercial.
 */
@Entity
@Table(name = "mercados_contratos")
public class ContratoOcupacion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_contrato", nullable = false, unique = true, length = 50)
    private String codigoContrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_id", nullable = false)
    private AsignacionPuesto asignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comerciante_id", nullable = false)
    private Comerciante comerciante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id", nullable = false)
    private PuestoComercial puesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_contrato_id")
    private TipoContrato tipoContrato;

    @Column(name = "tipo_contrato_codigo", nullable = false, length = 50)
    private String tipoContratoCodigo = "USO_ESPACIO_COMERCIAL";

    @Column(name = "fecha_elaboracion", nullable = false)
    private LocalDate fechaElaboracion = LocalDate.now();

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoContrato estado = EstadoContrato.BORRADOR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @Column(name = "expediente_id")
    private Long expedienteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @Column(name = "responsable_elaboracion", length = 100)
    private String responsableElaboracion;

    @Column(name = "responsable_aprobacion", length = 100)
    private String responsableAprobacion;

    @Column(name = "fecha_aprobacion")
    private OffsetDateTime fechaAprobacion;

    @Column(name = "fecha_formalizacion")
    private OffsetDateTime fechaFormalizacion;

    @Column(name = "codigo_seguro", unique = true, length = 100)
    private String codigoSeguro;

    @Column(name = "hash_documento", length = 64)
    private String hashDocumento;

    @Column(name = "motivo_observacion", columnDefinition = "TEXT")
    private String motivoObservacion;

    @Column(name = "motivo_suspension", columnDefinition = "TEXT")
    private String motivoSuspension;

    @Column(name = "motivo_terminacion", columnDefinition = "TEXT")
    private String motivoTerminacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public ContratoOcupacion() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigoContrato() { return codigoContrato; }
    public void setCodigoContrato(String codigoContrato) { this.codigoContrato = codigoContrato; }

    public AsignacionPuesto getAsignacion() { return asignacion; }
    public void setAsignacion(AsignacionPuesto asignacion) { this.asignacion = asignacion; }

    public Comerciante getComerciante() { return comerciante; }
    public void setComerciante(Comerciante comerciante) { this.comerciante = comerciante; }

    public PuestoComercial getPuesto() { return puesto; }
    public void setPuesto(PuestoComercial puesto) { this.puesto = puesto; }

    public TipoContrato getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(TipoContrato tipoContrato) { this.tipoContrato = tipoContrato; }

    public String getTipoContratoCodigo() { return tipoContratoCodigo; }
    public void setTipoContratoCodigo(String tipoContratoCodigo) { this.tipoContratoCodigo = tipoContratoCodigo; }

    public LocalDate getFechaElaboracion() { return fechaElaboracion; }
    public void setFechaElaboracion(LocalDate fechaElaboracion) { this.fechaElaboracion = fechaElaboracion; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public EstadoContrato getEstado() { return estado; }
    public void setEstado(EstadoContrato estado) { this.estado = estado; }

    public Documento getDocumento() { return documento; }
    public void setDocumento(Documento documento) { this.documento = documento; }

    public Long getExpedienteId() { return expedienteId; }
    public void setExpedienteId(Long expedienteId) { this.expedienteId = expedienteId; }

    public Oficina getOficinaResponsable() { return oficinaResponsable; }
    public void setOficinaResponsable(Oficina oficinaResponsable) { this.oficinaResponsable = oficinaResponsable; }

    public String getResponsableElaboracion() { return responsableElaboracion; }
    public void setResponsableElaboracion(String responsableElaboracion) { this.responsableElaboracion = responsableElaboracion; }

    public String getResponsableAprobacion() { return responsableAprobacion; }
    public void setResponsableAprobacion(String responsableAprobacion) { this.responsableAprobacion = responsableAprobacion; }

    public OffsetDateTime getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(OffsetDateTime fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }

    public OffsetDateTime getFechaFormalizacion() { return fechaFormalizacion; }
    public void setFechaFormalizacion(OffsetDateTime fechaFormalizacion) { this.fechaFormalizacion = fechaFormalizacion; }

    public String getCodigoSeguro() { return codigoSeguro; }
    public void setCodigoSeguro(String codigoSeguro) { this.codigoSeguro = codigoSeguro; }

    public String getHashDocumento() { return hashDocumento; }
    public void setHashDocumento(String hashDocumento) { this.hashDocumento = hashDocumento; }

    public String getMotivoObservacion() { return motivoObservacion; }
    public void setMotivoObservacion(String motivoObservacion) { this.motivoObservacion = motivoObservacion; }

    public String getMotivoSuspension() { return motivoSuspension; }
    public void setMotivoSuspension(String motivoSuspension) { this.motivoSuspension = motivoSuspension; }

    public String getMotivoTerminacion() { return motivoTerminacion; }
    public void setMotivoTerminacion(String motivoTerminacion) { this.motivoTerminacion = motivoTerminacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
