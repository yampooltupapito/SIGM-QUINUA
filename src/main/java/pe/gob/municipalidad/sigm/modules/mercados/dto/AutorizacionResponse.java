package pe.gob.municipalidad.sigm.modules.mercados.dto;

import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class AutorizacionResponse {

    private UUID id;
    private String codigoAutorizacion;
    private String numeroAutorizacion;
    private UUID asignacionId;
    private String codigoAsignacion;
    private UUID comercianteId;
    private String comercianteNombre;
    private String comercianteDocumento;
    private UUID puestoId;
    private String puestoCodigo;
    private String puestoNumero;
    private UUID mercadoId;
    private String mercadoNombre;
    private String tipoAutorizacion;
    private String tipoAutorizacionNombre;
    private LocalDate fechaSolicitud;
    private LocalDate fechaEmision;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoAutorizacion estado;
    private String estadoNombre;
    private Long diasRestantes;
    private String codigoSeguro;
    private String hashDocumento;
    private Long documentoId;
    private LocalDateTime createdAt;

    public AutorizacionResponse() {}

    public AutorizacionResponse(UUID id, String codigoAutorizacion, String numeroAutorizacion, UUID asignacionId, String codigoAsignacion, UUID comercianteId, String comercianteNombre, String comercianteDocumento, UUID puestoId, String puestoCodigo, String puestoNumero, UUID mercadoId, String mercadoNombre, String tipoAutorizacion, String tipoAutorizacionNombre, LocalDate fechaSolicitud, LocalDate fechaEmision, LocalDate fechaInicio, LocalDate fechaFin, EstadoAutorizacion estado, String estadoNombre, Long diasRestantes, String codigoSeguro, String hashDocumento, Long documentoId, LocalDateTime createdAt) {
        this.id = id;
        this.codigoAutorizacion = codigoAutorizacion;
        this.numeroAutorizacion = numeroAutorizacion;
        this.asignacionId = asignacionId;
        this.codigoAsignacion = codigoAsignacion;
        this.comercianteId = comercianteId;
        this.comercianteNombre = comercianteNombre;
        this.comercianteDocumento = comercianteDocumento;
        this.puestoId = puestoId;
        this.puestoCodigo = puestoCodigo;
        this.puestoNumero = puestoNumero;
        this.mercadoId = mercadoId;
        this.mercadoNombre = mercadoNombre;
        this.tipoAutorizacion = tipoAutorizacion;
        this.tipoAutorizacionNombre = tipoAutorizacionNombre;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaEmision = fechaEmision;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.estadoNombre = estadoNombre;
        this.diasRestantes = diasRestantes;
        this.codigoSeguro = codigoSeguro;
        this.hashDocumento = hashDocumento;
        this.documentoId = documentoId;
        this.createdAt = createdAt;
    }

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

    public UUID getPuestoId() { return puestoId; }
    public void setPuestoId(UUID puestoId) { this.puestoId = puestoId; }

    public String getPuestoCodigo() { return puestoCodigo; }
    public void setPuestoCodigo(String puestoCodigo) { this.puestoCodigo = puestoCodigo; }

    public String getPuestoNumero() { return puestoNumero; }
    public void setPuestoNumero(String puestoNumero) { this.puestoNumero = puestoNumero; }

    public UUID getMercadoId() { return mercadoId; }
    public void setMercadoId(UUID mercadoId) { this.mercadoId = mercadoId; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public String getTipoAutorizacion() { return tipoAutorizacion; }
    public void setTipoAutorizacion(String tipoAutorizacion) { this.tipoAutorizacion = tipoAutorizacion; }

    public String getTipoAutorizacionNombre() { return tipoAutorizacionNombre; }
    public void setTipoAutorizacionNombre(String tipoAutorizacionNombre) { this.tipoAutorizacionNombre = tipoAutorizacionNombre; }

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

    public String getCodigoSeguro() { return codigoSeguro; }
    public void setCodigoSeguro(String codigoSeguro) { this.codigoSeguro = codigoSeguro; }

    public String getHashDocumento() { return hashDocumento; }
    public void setHashDocumento(String hashDocumento) { this.hashDocumento = hashDocumento; }

    public Long getDocumentoId() { return documentoId; }
    public void setDocumentoId(Long documentoId) { this.documentoId = documentoId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String codigoAutorizacion;
        private String numeroAutorizacion;
        private UUID asignacionId;
        private String codigoAsignacion;
        private UUID comercianteId;
        private String comercianteNombre;
        private String comercianteDocumento;
        private UUID puestoId;
        private String puestoCodigo;
        private String puestoNumero;
        private UUID mercadoId;
        private String mercadoNombre;
        private String tipoAutorizacion;
        private String tipoAutorizacionNombre;
        private LocalDate fechaSolicitud;
        private LocalDate fechaEmision;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private EstadoAutorizacion estado;
        private String estadoNombre;
        private Long diasRestantes;
        private String codigoSeguro;
        private String hashDocumento;
        private Long documentoId;
        private LocalDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder codigoAutorizacion(String codigoAutorizacion) { this.codigoAutorizacion = codigoAutorizacion; return this; }
        public Builder numeroAutorizacion(String numeroAutorizacion) { this.numeroAutorizacion = numeroAutorizacion; return this; }
        public Builder asignacionId(UUID asignacionId) { this.asignacionId = asignacionId; return this; }
        public Builder codigoAsignacion(String codigoAsignacion) { this.codigoAsignacion = codigoAsignacion; return this; }
        public Builder comercianteId(UUID comercianteId) { this.comercianteId = comercianteId; return this; }
        public Builder comercianteNombre(String comercianteNombre) { this.comercianteNombre = comercianteNombre; return this; }
        public Builder comercianteDocumento(String comercianteDocumento) { this.comercianteDocumento = comercianteDocumento; return this; }
        public Builder puestoId(UUID puestoId) { this.puestoId = puestoId; return this; }
        public Builder puestoCodigo(String puestoCodigo) { this.puestoCodigo = puestoCodigo; return this; }
        public Builder puestoNumero(String puestoNumero) { this.puestoNumero = puestoNumero; return this; }
        public Builder mercadoId(UUID mercadoId) { this.mercadoId = mercadoId; return this; }
        public Builder mercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; return this; }
        public Builder tipoAutorizacion(String tipoAutorizacion) { this.tipoAutorizacion = tipoAutorizacion; return this; }
        public Builder tipoAutorizacionNombre(String tipoAutorizacionNombre) { this.tipoAutorizacionNombre = tipoAutorizacionNombre; return this; }
        public Builder fechaSolicitud(LocalDate fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; return this; }
        public Builder fechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; return this; }
        public Builder fechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; return this; }
        public Builder fechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; return this; }
        public Builder estado(EstadoAutorizacion estado) { this.estado = estado; return this; }
        public Builder estadoNombre(String estadoNombre) { this.estadoNombre = estadoNombre; return this; }
        public Builder diasRestantes(Long diasRestantes) { this.diasRestantes = diasRestantes; return this; }
        public Builder codigoSeguro(String codigoSeguro) { this.codigoSeguro = codigoSeguro; return this; }
        public Builder hashDocumento(String hashDocumento) { this.hashDocumento = hashDocumento; return this; }
        public Builder documentoId(Long documentoId) { this.documentoId = documentoId; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public AutorizacionResponse build() {
            return new AutorizacionResponse(id, codigoAutorizacion, numeroAutorizacion, asignacionId, codigoAsignacion, comercianteId, comercianteNombre, comercianteDocumento, puestoId, puestoCodigo, puestoNumero, mercadoId, mercadoNombre, tipoAutorizacion, tipoAutorizacionNombre, fechaSolicitud, fechaEmision, fechaInicio, fechaFin, estado, estadoNombre, diasRestantes, codigoSeguro, hashDocumento, documentoId, createdAt);
        }
    }
}
