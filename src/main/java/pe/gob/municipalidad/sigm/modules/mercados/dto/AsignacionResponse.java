package pe.gob.municipalidad.sigm.modules.mercados.dto;

import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoAsignacion;

import java.time.LocalDate;
import java.util.UUID;

public class AsignacionResponse {

    private UUID id;
    private String codigoAsignacion;
    private UUID puestoId;
    private String puestoCodigo;
    private String puestoNumero;
    private String mercadoNombre;
    private String sectorNombre;
    private String pabellonNombre;
    private UUID comercianteId;
    private String comercianteCodigo;
    private String comercianteTipo;
    private String titularNombre;
    private String documentoEnmascarado;
    private LocalDate fechaSolicitud;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoAsignacion estado;
    private String estadoDescripcion;
    private String badgeClase;
    private TipoAsignacion tipoAsignacion;
    private String motivo;
    private String usuarioResponsable;
    private Long documentoSustentoId;
    private Long expedienteId;
    private String observaciones;

    public AsignacionResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean esVigente() {
        return estado == EstadoAsignacion.VIGENTE;
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
        return estado != null && estado.permiteDevolucion();
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

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

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

    public String getDocumentoEnmascarado() { return documentoEnmascarado; }
    public void setDocumentoEnmascarado(String documentoEnmascarado) { this.documentoEnmascarado = documentoEnmascarado; }

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

    public String getUsuarioResponsable() { return usuarioResponsable; }
    public void setUsuarioResponsable(String usuarioResponsable) { this.usuarioResponsable = usuarioResponsable; }

    public Long getDocumentoSustentoId() { return documentoSustentoId; }
    public void setDocumentoSustentoId(Long documentoSustentoId) { this.documentoSustentoId = documentoSustentoId; }

    public Long getExpedienteId() { return expedienteId; }
    public void setExpedienteId(Long expedienteId) { this.expedienteId = expedienteId; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public static class Builder {
        private final AsignacionResponse target = new AsignacionResponse();

        public Builder id(UUID id) { target.id = id; return this; }
        public Builder codigoAsignacion(String c) { target.codigoAsignacion = c; return this; }
        public Builder puestoId(UUID p) { target.puestoId = p; return this; }
        public Builder puestoCodigo(String p) { target.puestoCodigo = p; return this; }
        public Builder puestoNumero(String p) { target.puestoNumero = p; return this; }
        public Builder mercadoNombre(String m) { target.mercadoNombre = m; return this; }
        public Builder sectorNombre(String s) { target.sectorNombre = s; return this; }
        public Builder pabellonNombre(String p) { target.pabellonNombre = p; return this; }
        public Builder comercianteId(UUID c) { target.comercianteId = c; return this; }
        public Builder comercianteCodigo(String c) { target.comercianteCodigo = c; return this; }
        public Builder comercianteTipo(String c) { target.comercianteTipo = c; return this; }
        public Builder titularNombre(String t) { target.titularNombre = t; return this; }
        public Builder documentoEnmascarado(String d) { target.documentoEnmascarado = d; return this; }
        public Builder fechaSolicitud(LocalDate f) { target.fechaSolicitud = f; return this; }
        public Builder fechaInicio(LocalDate f) { target.fechaInicio = f; return this; }
        public Builder fechaFin(LocalDate f) { target.fechaFin = f; return this; }
        public Builder estado(EstadoAsignacion e) { target.estado = e; return this; }
        public Builder estadoDescripcion(String d) { target.estadoDescripcion = d; return this; }
        public Builder badgeClase(String b) { target.badgeClase = b; return this; }
        public Builder tipoAsignacion(TipoAsignacion t) { target.tipoAsignacion = t; return this; }
        public Builder motivo(String m) { target.motivo = m; return this; }
        public Builder usuarioResponsable(String u) { target.usuarioResponsable = u; return this; }
        public Builder documentoSustentoId(Long d) { target.documentoSustentoId = d; return this; }
        public Builder expedienteId(Long e) { target.expedienteId = e; return this; }
        public Builder observaciones(String o) { target.observaciones = o; return this; }

        public AsignacionResponse build() {
            return target;
        }
    }
}
