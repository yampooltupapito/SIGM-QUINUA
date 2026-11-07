package pe.gob.municipalidad.sigm.modules.mercados.dto;

import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContratoDetailResponse {

    private UUID id;
    private String codigoContrato;
    private UUID asignacionId;
    private String codigoAsignacion;
    private UUID comercianteId;
    private String comercianteNombre;
    private String comercianteDocumento;
    private String comercianteTipo;
    private String comercianteTelefono;
    private String comercianteCorreo;
    private String comercianteDireccion;
    private UUID puestoId;
    private String puestoCodigo;
    private String puestoNumero;
    private String puestoSector;
    private String puestoPabellon;
    private String puestoTipo;
    private UUID mercadoId;
    private String mercadoNombre;
    private Long tipoContratoId;
    private String tipoContratoCodigo;
    private String tipoContratoNombre;
    private String tipoContratoBaseNormativa;
    private LocalDate fechaElaboracion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoContrato estado;
    private String estadoNombre;
    private Long diasRestantes;
    private Long documentoId;
    private Long expedienteId;
    private String expedienteNumero;
    private Long oficinaResponsableId;
    private String oficinaResponsableNombre;
    private String responsableElaboracion;
    private String responsableAprobacion;
    private OffsetDateTime fechaAprobacion;
    private OffsetDateTime fechaFormalizacion;
    private String codigoSeguro;
    private String hashDocumento;
    private String motivoObservacion;
    private String motivoSuspension;
    private String motivoTerminacion;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private List<ContratoHistorialResponse> historial = new ArrayList<>();

    public ContratoDetailResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigoContrato() { return codigoContrato; }
    public void setCodigoContrato(String codigoContrato) { this.codigoContrato = codigoContrato; }

    public UUID getAsignacionId() { return asignacionId; }
    public void setAsignacionId(UUID asignacionId) { this.asignacionId = asignacionId; }

    public String getCodigoAsignacion() { return codigoAsignacion; }
    public void setCodigoAsignacion(String codigoAsignacion) { this.codigoAsignacion = codigoAsignacion; }

    public UUID getComercianteId() { return comercianteId; }
    public void setComercianteId(UUID comercianteId) { this.comercianteId = comercianteId; }

    public String getComercianteNombre() { return comercianteNombre; }
    public void setComercianteNombre(String comercianteNombre) { this.comercianteNombre = comercianteNombre; }

    public String getComercianteDocumento() { return comercianteDocumento; }
    public void setComercianteDocumento(String comercianteDocumento) { this.comercianteDocumento = comercianteDocumento; }

    public String getComercianteTipo() { return comercianteTipo; }
    public void setComercianteTipo(String comercianteTipo) { this.comercianteTipo = comercianteTipo; }

    public String getComercianteTelefono() { return comercianteTelefono; }
    public void setComercianteTelefono(String comercianteTelefono) { this.comercianteTelefono = comercianteTelefono; }

    public String getComercianteCorreo() { return comercianteCorreo; }
    public void setComercianteCorreo(String comercianteCorreo) { this.comercianteCorreo = comercianteCorreo; }

    public String getComercianteDireccion() { return comercianteDireccion; }
    public void setComercianteDireccion(String comercianteDireccion) { this.comercianteDireccion = comercianteDireccion; }

    public UUID getPuestoId() { return puestoId; }
    public void setPuestoId(UUID puestoId) { this.puestoId = puestoId; }

    public String getPuestoCodigo() { return puestoCodigo; }
    public void setPuestoCodigo(String puestoCodigo) { this.puestoCodigo = puestoCodigo; }

    public String getPuestoNumero() { return puestoNumero; }
    public void setPuestoNumero(String puestoNumero) { this.puestoNumero = puestoNumero; }

    public String getPuestoSector() { return puestoSector; }
    public void setPuestoSector(String puestoSector) { this.puestoSector = puestoSector; }

    public String getPuestoPabellon() { return puestoPabellon; }
    public void setPuestoPabellon(String puestoPabellon) { this.puestoPabellon = puestoPabellon; }

    public String getPuestoTipo() { return puestoTipo; }
    public void setPuestoTipo(String puestoTipo) { this.puestoTipo = puestoTipo; }

    public UUID getMercadoId() { return mercadoId; }
    public void setMercadoId(UUID mercadoId) { this.mercadoId = mercadoId; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public Long getTipoContratoId() { return tipoContratoId; }
    public void setTipoContratoId(Long tipoContratoId) { this.tipoContratoId = tipoContratoId; }

    public String getTipoContratoCodigo() { return tipoContratoCodigo; }
    public void setTipoContratoCodigo(String tipoContratoCodigo) { this.tipoContratoCodigo = tipoContratoCodigo; }

    public String getTipoContratoNombre() { return tipoContratoNombre; }
    public void setTipoContratoNombre(String tipoContratoNombre) { this.tipoContratoNombre = tipoContratoNombre; }

    public String getTipoContratoBaseNormativa() { return tipoContratoBaseNormativa; }
    public void setTipoContratoBaseNormativa(String tipoContratoBaseNormativa) { this.tipoContratoBaseNormativa = tipoContratoBaseNormativa; }

    public LocalDate getFechaElaboracion() { return fechaElaboracion; }
    public void setFechaElaboracion(LocalDate fechaElaboracion) { this.fechaElaboracion = fechaElaboracion; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public EstadoContrato getEstado() { return estado; }
    public void setEstado(EstadoContrato estado) { this.estado = estado; }

    public String getEstadoNombre() { return estadoNombre; }
    public void setEstadoNombre(String estadoNombre) { this.estadoNombre = estadoNombre; }

    public Long getDiasRestantes() { return diasRestantes; }
    public void setDiasRestantes(Long diasRestantes) { this.diasRestantes = diasRestantes; }

    public Long getDocumentoId() { return documentoId; }
    public void setDocumentoId(Long documentoId) { this.documentoId = documentoId; }

    public Long getExpedienteId() { return expedienteId; }
    public void setExpedienteId(Long expedienteId) { this.expedienteId = expedienteId; }

    public String getExpedienteNumero() { return expedienteNumero; }
    public void setExpedienteNumero(String expedienteNumero) { this.expedienteNumero = expedienteNumero; }

    public Long getOficinaResponsableId() { return oficinaResponsableId; }
    public void setOficinaResponsableId(Long oficinaResponsableId) { this.oficinaResponsableId = oficinaResponsableId; }

    public String getOficinaResponsableNombre() { return oficinaResponsableNombre; }
    public void setOficinaResponsableNombre(String oficinaResponsableNombre) { this.oficinaResponsableNombre = oficinaResponsableNombre; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public List<ContratoHistorialResponse> getHistorial() { return historial; }
    public void setHistorial(List<ContratoHistorialResponse> historial) { this.historial = historial; }
}
