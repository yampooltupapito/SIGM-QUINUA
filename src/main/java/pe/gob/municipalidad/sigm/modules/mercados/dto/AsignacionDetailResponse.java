package pe.gob.municipalidad.sigm.modules.mercados.dto;

import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoAsignacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class AsignacionDetailResponse {

    private UUID id;
    private String codigoAsignacion;

    // Puesto y Mercado
    private UUID puestoId;
    private String puestoCodigo;
    private String puestoNumero;
    private String puestoEstado;
    private BigDecimal puestoArea;
    private String tipoPuestoNombre;
    private UUID mercadoId;
    private String mercadoNombre;
    private String mercadoCodigo;
    private String sectorNombre;
    private String pabellonNombre;

    // Comerciante
    private UUID comercianteId;
    private String comercianteCodigo;
    private String comercianteTipo;
    private String titularNombre;
    private String titularDocumento;
    private String comercianteTelefono;
    private String comercianteCorreo;
    private String comercianteDireccion;
    private String comercianteEstado;

    // Fechas y Estado
    private LocalDate fechaSolicitud;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoAsignacion estado;
    private String estadoDescripcion;
    private String badgeClase;
    private TipoAsignacion tipoAsignacion;

    // Motivos y Gestión
    private String motivo;
    private String observaciones;
    private String usuarioRegistro;
    private String usuarioEvaluacion;
    private OffsetDateTime fechaEvaluacion;
    private String observacionesEvaluacion;
    private String usuarioAprobacion;
    private OffsetDateTime fechaAprobacion;
    private String motivoRechazo;
    private String motivoSuspension;
    private String motivoFinalizacion;

    // Documentos y Expedientes
    private Long documentoSustentoId;
    private String documentoSustentoNombre;
    private Long expedienteId;

    // Trazabilidad y Evaluaciones
    private List<AsignacionEvaluacionResponse> evaluaciones;
    private List<HistorialOcupacionResponse> historial;
    private DevolucionPuestoResponse devolucion;

    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AsignacionDetailResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean esVigente() {
        return estado == EstadoAsignacion.VIGENTE;
    }

    public boolean permitePresentar() {
        return estado == EstadoAsignacion.BORRADOR;
    }

    public boolean permiteEvaluar() {
        return estado != null && estado.permiteEvaluacion();
    }

    public boolean permiteAprobar() {
        return estado != null && estado.permiteAprobacion();
    }

    public boolean permiteActivar() {
        return estado != null && estado.permiteActivacion();
    }

    public boolean permiteSuspender() {
        return estado != null && estado.permiteSuspension();
    }

    public boolean permiteReanudar() {
        return estado != null && estado.permiteReanudacion();
    }

    public boolean permiteFinalizar() {
        return estado != null && estado.permiteFinalizacion();
    }

    public boolean permiteDevolucion() {
        return estado != null && estado.permiteDevolucion() && devolucion == null;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigoAsignacion() { return codigoAsignacion; }
    public void setCodigoAsignacion(String codigoAsignacion) { this.codigoAsignacion = codigoAsignacion; }

    public UUID getPuestoId() { return puestoId; }
    public void setPuestoId(UUID puestoId) { this.puestoId = puestoId; }

    public String getPuestoCodigo() { return puestoCodigo; }
    public void setPuestoCodigo(String puestoCodigo) { this.puestoCodigo = puestoCodigo; }

    public String getPuestoNumero() { return puestoNumero; }
    public void setPuestoNumero(String puestoNumero) { this.puestoNumero = puestoNumero; }

    public String getPuestoEstado() { return puestoEstado; }
    public void setPuestoEstado(String puestoEstado) { this.puestoEstado = puestoEstado; }

    public BigDecimal getPuestoArea() { return puestoArea; }
    public void setPuestoArea(BigDecimal puestoArea) { this.puestoArea = puestoArea; }

    public String getTipoPuestoNombre() { return tipoPuestoNombre; }
    public void setTipoPuestoNombre(String tipoPuestoNombre) { this.tipoPuestoNombre = tipoPuestoNombre; }

    public UUID getMercadoId() { return mercadoId; }
    public void setMercadoId(UUID mercadoId) { this.mercadoId = mercadoId; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public String getMercadoCodigo() { return mercadoCodigo; }
    public void setMercadoCodigo(String mercadoCodigo) { this.mercadoCodigo = mercadoCodigo; }

    public String getSectorNombre() { return sectorNombre; }
    public void setSectorNombre(String sectorNombre) { this.sectorNombre = sectorNombre; }

    public String getPabellonNombre() { return pabellonNombre; }
    public void setPabellonNombre(String pabellonNombre) { this.pabellonNombre = pabellonNombre; }

    public UUID getComercianteId() { return comercianteId; }
    public void setComercianteId(UUID comercianteId) { this.comercianteId = comercianteId; }

    public String getComercianteCodigo() { return comercianteCodigo; }
    public void setComercianteCodigo(String comercianteCodigo) { this.comercianteCodigo = comercianteCodigo; }

    public String getComercianteTipo() { return comercianteTipo; }
    public void setComercianteTipo(String comercianteTipo) { this.comercianteTipo = comercianteTipo; }

    public String getTitularNombre() { return titularNombre; }
    public void setTitularNombre(String titularNombre) { this.titularNombre = titularNombre; }

    public String getTitularDocumento() { return titularDocumento; }
    public void setTitularDocumento(String titularDocumento) { this.titularDocumento = titularDocumento; }

    public String getComercianteTelefono() { return comercianteTelefono; }
    public void setComercianteTelefono(String comercianteTelefono) { this.comercianteTelefono = comercianteTelefono; }

    public String getComercianteCorreo() { return comercianteCorreo; }
    public void setComercianteCorreo(String comercianteCorreo) { this.comercianteCorreo = comercianteCorreo; }

    public String getComercianteDireccion() { return comercianteDireccion; }
    public void setComercianteDireccion(String comercianteDireccion) { this.comercianteDireccion = comercianteDireccion; }

    public String getComercianteEstado() { return comercianteEstado; }
    public void setComercianteEstado(String comercianteEstado) { this.comercianteEstado = comercianteEstado; }

    public LocalDate getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDate fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public EstadoAsignacion getEstado() { return estado; }
    public void setEstado(EstadoAsignacion estado) { this.estado = estado; }

    public String getEstadoDescripcion() { return estadoDescripcion; }
    public void setEstadoDescripcion(String estadoDescripcion) { this.estadoDescripcion = estadoDescripcion; }

    public String getBadgeClase() { return badgeClase; }
    public void setBadgeClase(String badgeClase) { this.badgeClase = badgeClase; }

    public TipoAsignacion getTipoAsignacion() { return tipoAsignacion; }
    public void setTipoAsignacion(TipoAsignacion tipoAsignacion) { this.tipoAsignacion = tipoAsignacion; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getUsuarioRegistro() { return usuarioRegistro; }
    public void setUsuarioRegistro(String usuarioRegistro) { this.usuarioRegistro = usuarioRegistro; }

    public String getUsuarioEvaluacion() { return usuarioEvaluacion; }
    public void setUsuarioEvaluacion(String usuarioEvaluacion) { this.usuarioEvaluacion = usuarioEvaluacion; }

    public OffsetDateTime getFechaEvaluacion() { return fechaEvaluacion; }
    public void setFechaEvaluacion(OffsetDateTime fechaEvaluacion) { this.fechaEvaluacion = fechaEvaluacion; }

    public String getObservacionesEvaluacion() { return observacionesEvaluacion; }
    public void setObservacionesEvaluacion(String observacionesEvaluacion) { this.observacionesEvaluacion = observacionesEvaluacion; }

    public String getUsuarioAprobacion() { return usuarioAprobacion; }
    public void setUsuarioAprobacion(String usuarioAprobacion) { this.usuarioAprobacion = usuarioAprobacion; }

    public OffsetDateTime getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(OffsetDateTime fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public String getMotivoSuspension() { return motivoSuspension; }
    public void setMotivoSuspension(String motivoSuspension) { this.motivoSuspension = motivoSuspension; }

    public String getMotivoFinalizacion() { return motivoFinalizacion; }
    public void setMotivoFinalizacion(String motivoFinalizacion) { this.motivoFinalizacion = motivoFinalizacion; }

    public Long getDocumentoSustentoId() { return documentoSustentoId; }
    public void setDocumentoSustentoId(Long documentoSustentoId) { this.documentoSustentoId = documentoSustentoId; }

    public String getDocumentoSustentoNombre() { return documentoSustentoNombre; }
    public void setDocumentoSustentoNombre(String documentoSustentoNombre) { this.documentoSustentoNombre = documentoSustentoNombre; }

    public Long getExpedienteId() { return expedienteId; }
    public void setExpedienteId(Long expedienteId) { this.expedienteId = expedienteId; }

    public List<AsignacionEvaluacionResponse> getEvaluaciones() { return evaluaciones; }
    public void setEvaluaciones(List<AsignacionEvaluacionResponse> evaluaciones) { this.evaluaciones = evaluaciones; }

    public List<HistorialOcupacionResponse> getHistorial() { return historial; }
    public void setHistorial(List<HistorialOcupacionResponse> historial) { this.historial = historial; }

    public DevolucionPuestoResponse getDevolucion() { return devolucion; }
    public void setDevolucion(DevolucionPuestoResponse devolucion) { this.devolucion = devolucion; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class Builder {
        private final AsignacionDetailResponse target = new AsignacionDetailResponse();

        public Builder id(UUID id) { target.id = id; return this; }
        public Builder codigoAsignacion(String c) { target.codigoAsignacion = c; return this; }
        public Builder puestoId(UUID p) { target.puestoId = p; return this; }
        public Builder puestoCodigo(String p) { target.puestoCodigo = p; return this; }
        public Builder puestoNumero(String p) { target.puestoNumero = p; return this; }
        public Builder puestoEstado(String p) { target.puestoEstado = p; return this; }
        public Builder puestoArea(BigDecimal a) { target.puestoArea = a; return this; }
        public Builder tipoPuestoNombre(String t) { target.tipoPuestoNombre = t; return this; }
        public Builder mercadoId(UUID m) { target.mercadoId = m; return this; }
        public Builder mercadoNombre(String m) { target.mercadoNombre = m; return this; }
        public Builder mercadoCodigo(String m) { target.mercadoCodigo = m; return this; }
        public Builder sectorNombre(String s) { target.sectorNombre = s; return this; }
        public Builder pabellonNombre(String p) { target.pabellonNombre = p; return this; }
        public Builder comercianteId(UUID c) { target.comercianteId = c; return this; }
        public Builder comercianteCodigo(String c) { target.comercianteCodigo = c; return this; }
        public Builder comercianteTipo(String c) { target.comercianteTipo = c; return this; }
        public Builder titularNombre(String t) { target.titularNombre = t; return this; }
        public Builder titularDocumento(String t) { target.titularDocumento = t; return this; }
        public Builder comercianteTelefono(String t) { target.comercianteTelefono = t; return this; }
        public Builder comercianteCorreo(String c) { target.comercianteCorreo = c; return this; }
        public Builder comercianteDireccion(String d) { target.comercianteDireccion = d; return this; }
        public Builder comercianteEstado(String e) { target.comercianteEstado = e; return this; }
        public Builder fechaSolicitud(LocalDate f) { target.fechaSolicitud = f; return this; }
        public Builder fechaInicio(LocalDate f) { target.fechaInicio = f; return this; }
        public Builder fechaFin(LocalDate f) { target.fechaFin = f; return this; }
        public Builder estado(EstadoAsignacion e) { target.estado = e; return this; }
        public Builder estadoDescripcion(String d) { target.estadoDescripcion = d; return this; }
        public Builder badgeClase(String b) { target.badgeClase = b; return this; }
        public Builder tipoAsignacion(TipoAsignacion t) { target.tipoAsignacion = t; return this; }
        public Builder motivo(String m) { target.motivo = m; return this; }
        public Builder observaciones(String o) { target.observaciones = o; return this; }
        public Builder usuarioRegistro(String u) { target.usuarioRegistro = u; return this; }
        public Builder usuarioEvaluacion(String u) { target.usuarioEvaluacion = u; return this; }
        public Builder fechaEvaluacion(OffsetDateTime f) { target.fechaEvaluacion = f; return this; }
        public Builder observacionesEvaluacion(String o) { target.observacionesEvaluacion = o; return this; }
        public Builder usuarioAprobacion(String u) { target.usuarioAprobacion = u; return this; }
        public Builder fechaAprobacion(OffsetDateTime f) { target.fechaAprobacion = f; return this; }
        public Builder motivoRechazo(String m) { target.motivoRechazo = m; return this; }
        public Builder motivoSuspension(String m) { target.motivoSuspension = m; return this; }
        public Builder motivoFinalizacion(String m) { target.motivoFinalizacion = m; return this; }
        public Builder documentoSustentoId(Long d) { target.documentoSustentoId = d; return this; }
        public Builder documentoSustentoNombre(String d) { target.documentoSustentoNombre = d; return this; }
        public Builder expedienteId(Long e) { target.expedienteId = e; return this; }
        public Builder evaluaciones(List<AsignacionEvaluacionResponse> e) { target.evaluaciones = e; return this; }
        public Builder historial(List<HistorialOcupacionResponse> h) { target.historial = h; return this; }
        public Builder devolucion(DevolucionPuestoResponse d) { target.devolucion = d; return this; }
        public Builder version(Long v) { target.version = v; return this; }
        public Builder createdAt(LocalDateTime c) { target.createdAt = c; return this; }
        public Builder updatedAt(LocalDateTime u) { target.updatedAt = u; return this; }

        public AsignacionDetailResponse build() {
            return target;
        }
    }
}
