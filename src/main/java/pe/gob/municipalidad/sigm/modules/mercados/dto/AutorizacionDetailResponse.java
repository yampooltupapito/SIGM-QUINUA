package pe.gob.municipalidad.sigm.modules.mercados.dto;

import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AutorizacionDetailResponse {

    private UUID id;
    private String codigoAutorizacion;
    private String numeroAutorizacion;
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
    private Long tipoAutorizacionId;
    private String tipoAutorizacion;
    private String tipoAutorizacionNombre;
    private String tipoAutorizacionProcedimiento;
    private String tipoAutorizacionFuenteNormativa;
    private LocalDate fechaSolicitud;
    private LocalDate fechaEmision;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoAutorizacion estado;
    private String estadoNombre;
    private Long diasRestantes;
    private Long documentoId;
    private Long expedienteId;
    private String expedienteNumero;
    private Long oficinaEmisoraId;
    private String oficinaEmisoraNombre;
    private Long autoridadCompetenteId;
    private String autoridadCompetenteNombre;
    private String responsableEvaluacion;
    private String responsableAprobacion;
    private OffsetDateTime fechaEvaluacion;
    private OffsetDateTime fechaAprobacion;
    private String codigoSeguro;
    private String hashDocumento;
    private String motivoObservacion;
    private String motivoSuspension;
    private String motivoRevocacion;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private List<AutorizacionHistorialResponse> historial = new ArrayList<>();

    public AutorizacionDetailResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigoAutorizacion() { return codigoAutorizacion; }
    public void setCodigoAutorizacion(String codigoAutorizacion) { this.codigoAutorizacion = codigoAutorizacion; }

    public String getNumeroAutorizacion() { return numeroAutorizacion; }
    public void setNumeroAutorizacion(String numeroAutorizacion) { this.numeroAutorizacion = numeroAutorizacion; }

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

    public Long getTipoAutorizacionId() { return tipoAutorizacionId; }
    public void setTipoAutorizacionId(Long tipoAutorizacionId) { this.tipoAutorizacionId = tipoAutorizacionId; }

    public String getTipoAutorizacion() { return tipoAutorizacion; }
    public void setTipoAutorizacion(String tipoAutorizacion) { this.tipoAutorizacion = tipoAutorizacion; }

    public String getTipoAutorizacionNombre() { return tipoAutorizacionNombre; }
    public void setTipoAutorizacionNombre(String tipoAutorizacionNombre) { this.tipoAutorizacionNombre = tipoAutorizacionNombre; }

    public String getTipoAutorizacionProcedimiento() { return tipoAutorizacionProcedimiento; }
    public void setTipoAutorizacionProcedimiento(String tipoAutorizacionProcedimiento) { this.tipoAutorizacionProcedimiento = tipoAutorizacionProcedimiento; }

    public String getTipoAutorizacionFuenteNormativa() { return tipoAutorizacionFuenteNormativa; }
    public void setTipoAutorizacionFuenteNormativa(String tipoAutorizacionFuenteNormativa) { this.tipoAutorizacionFuenteNormativa = tipoAutorizacionFuenteNormativa; }

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

    public Long getOficinaEmisoraId() { return oficinaEmisoraId; }
    public void setOficinaEmisoraId(Long oficinaEmisoraId) { this.oficinaEmisoraId = oficinaEmisoraId; }

    public String getOficinaEmisoraNombre() { return oficinaEmisoraNombre; }
    public void setOficinaEmisoraNombre(String oficinaEmisoraNombre) { this.oficinaEmisoraNombre = oficinaEmisoraNombre; }

    public Long getAutoridadCompetenteId() { return autoridadCompetenteId; }
    public void setAutoridadCompetenteId(Long autoridadCompetenteId) { this.autoridadCompetenteId = autoridadCompetenteId; }

    public String getAutoridadCompetenteNombre() { return autoridadCompetenteNombre; }
    public void setAutoridadCompetenteNombre(String autoridadCompetenteNombre) { this.autoridadCompetenteNombre = autoridadCompetenteNombre; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public List<AutorizacionHistorialResponse> getHistorial() { return historial; }
    public void setHistorial(List<AutorizacionHistorialResponse> historial) { this.historial = historial; }
}
