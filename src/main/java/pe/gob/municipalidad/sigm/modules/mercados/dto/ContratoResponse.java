package pe.gob.municipalidad.sigm.modules.mercados.dto;

import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ContratoResponse {

    private UUID id;
    private String codigoContrato;
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
    private String tipoContratoCodigo;
    private String tipoContratoNombre;
    private LocalDate fechaElaboracion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoContrato estado;
    private String estadoNombre;
    private Long diasRestantes;
    private String codigoSeguro;
    private String hashDocumento;
    private Long documentoId;
    private LocalDateTime createdAt;

    public ContratoResponse() {}

    public ContratoResponse(UUID id, String codigoContrato, UUID asignacionId, String codigoAsignacion, UUID comercianteId, String comercianteNombre, String comercianteDocumento, UUID puestoId, String puestoCodigo, String puestoNumero, UUID mercadoId, String mercadoNombre, String tipoContratoCodigo, String tipoContratoNombre, LocalDate fechaElaboracion, LocalDate fechaInicio, LocalDate fechaFin, EstadoContrato estado, String estadoNombre, Long diasRestantes, String codigoSeguro, String hashDocumento, Long documentoId, LocalDateTime createdAt) {
        this.id = id;
        this.codigoContrato = codigoContrato;
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
        this.tipoContratoCodigo = tipoContratoCodigo;
        this.tipoContratoNombre = tipoContratoNombre;
        this.fechaElaboracion = fechaElaboracion;
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

    public String getTipoContratoCodigo() { return tipoContratoCodigo; }
    public void setTipoContratoCodigo(String tipoContratoCodigo) { this.tipoContratoCodigo = tipoContratoCodigo; }

    public String getTipoContratoNombre() { return tipoContratoNombre; }
    public void setTipoContratoNombre(String tipoContratoNombre) { this.tipoContratoNombre = tipoContratoNombre; }

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
        private String codigoContrato;
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
        private String tipoContratoCodigo;
        private String tipoContratoNombre;
        private LocalDate fechaElaboracion;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private EstadoContrato estado;
        private String estadoNombre;
        private Long diasRestantes;
        private String codigoSeguro;
        private String hashDocumento;
        private Long documentoId;
        private LocalDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder codigoContrato(String codigoContrato) { this.codigoContrato = codigoContrato; return this; }
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
        public Builder tipoContratoCodigo(String tipoContratoCodigo) { this.tipoContratoCodigo = tipoContratoCodigo; return this; }
        public Builder tipoContratoNombre(String tipoContratoNombre) { this.tipoContratoNombre = tipoContratoNombre; return this; }
        public Builder fechaElaboracion(LocalDate fechaElaboracion) { this.fechaElaboracion = fechaElaboracion; return this; }
        public Builder fechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; return this; }
        public Builder fechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; return this; }
        public Builder estado(EstadoContrato estado) { this.estado = estado; return this; }
        public Builder estadoNombre(String estadoNombre) { this.estadoNombre = estadoNombre; return this; }
        public Builder diasRestantes(Long diasRestantes) { this.diasRestantes = diasRestantes; return this; }
        public Builder codigoSeguro(String codigoSeguro) { this.codigoSeguro = codigoSeguro; return this; }
        public Builder hashDocumento(String hashDocumento) { this.hashDocumento = hashDocumento; return this; }
        public Builder documentoId(Long documentoId) { this.documentoId = documentoId; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ContratoResponse build() {
            return new ContratoResponse(id, codigoContrato, asignacionId, codigoAsignacion, comercianteId, comercianteNombre, comercianteDocumento, puestoId, puestoCodigo, puestoNumero, mercadoId, mercadoNombre, tipoContratoCodigo, tipoContratoNombre, fechaElaboracion, fechaInicio, fechaFin, estado, estadoNombre, diasRestantes, codigoSeguro, hashDocumento, documentoId, createdAt);
        }
    }
}
