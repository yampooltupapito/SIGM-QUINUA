package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ContratoReporteItem {
    private UUID id;
    private String codigoContrato;
    private String tipoInstrumento; // CONTRATO u AUTORIZACION
    private String mercadoNombre;
    private String puestoCodigo;
    private String comercianteNombre;
    private String documentoTitular;
    private String modalidad;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private long diasParaVencer;
    private boolean vencido;
    private boolean proximoAVencer;

    public ContratoReporteItem() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigoContrato() { return codigoContrato; }
    public void setCodigoContrato(String codigoContrato) { this.codigoContrato = codigoContrato; }

    public String getTipoInstrumento() { return tipoInstrumento; }
    public void setTipoInstrumento(String tipoInstrumento) { this.tipoInstrumento = tipoInstrumento; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public String getPuestoCodigo() { return puestoCodigo; }
    public void setPuestoCodigo(String puestoCodigo) { this.puestoCodigo = puestoCodigo; }

    public String getComercianteNombre() { return comercianteNombre; }
    public void setComercianteNombre(String comercianteNombre) { this.comercianteNombre = comercianteNombre; }

    public String getDocumentoTitular() { return documentoTitular; }
    public void setDocumentoTitular(String documentoTitular) { this.documentoTitular = documentoTitular; }

    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public long getDiasParaVencer() { return diasParaVencer; }
    public void setDiasParaVencer(long diasParaVencer) { this.diasParaVencer = diasParaVencer; }

    public boolean isVencido() { return vencido; }
    public void setVencido(boolean vencido) { this.vencido = vencido; }

    public boolean isProximoAVencer() { return proximoAVencer; }
    public void setProximoAVencer(boolean proximoAVencer) { this.proximoAVencer = proximoAVencer; }

    public String getCodigo() { return codigoContrato; }
    public String getNumeroDocumento() { return documentoTitular; }
    public long getDiasRestantes() { return diasParaVencer; }
    public BigDecimal getImporteMerced() { return BigDecimal.ZERO; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ContratoReporteItem item = new ContratoReporteItem();

        public Builder id(UUID id) { item.id = id; return this; }
        public Builder codigoContrato(String codigoContrato) { item.codigoContrato = codigoContrato; return this; }
        public Builder tipoInstrumento(String tipoInstrumento) { item.tipoInstrumento = tipoInstrumento; return this; }
        public Builder mercadoNombre(String mercadoNombre) { item.mercadoNombre = mercadoNombre; return this; }
        public Builder puestoCodigo(String puestoCodigo) { item.puestoCodigo = puestoCodigo; return this; }
        public Builder comercianteNombre(String comercianteNombre) { item.comercianteNombre = comercianteNombre; return this; }
        public Builder documentoTitular(String documentoTitular) { item.documentoTitular = documentoTitular; return this; }
        public Builder modalidad(String modalidad) { item.modalidad = modalidad; return this; }
        public Builder fechaInicio(LocalDate fechaInicio) { item.fechaInicio = fechaInicio; return this; }
        public Builder fechaFin(LocalDate fechaFin) { item.fechaFin = fechaFin; return this; }
        public Builder estado(String estado) { item.estado = estado; return this; }
        public Builder diasParaVencer(long diasParaVencer) { item.diasParaVencer = diasParaVencer; return this; }
        public Builder vencido(boolean vencido) { item.vencido = vencido; return this; }
        public Builder proximoAVencer(boolean proximoAVencer) { item.proximoAVencer = proximoAVencer; return this; }

        public ContratoReporteItem build() {
            return item;
        }
    }
}
