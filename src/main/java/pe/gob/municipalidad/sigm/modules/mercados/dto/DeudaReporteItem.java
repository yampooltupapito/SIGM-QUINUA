package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class DeudaReporteItem {
    private UUID obligacionId;
    private String codigoObligacion;
    private String comercianteNombre;
    private String documentoComerciante;
    private String mercadoNombre;
    private String puestoCodigo;
    private String conceptoNombre;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private BigDecimal montoOriginal;
    private BigDecimal saldoPendiente;
    private String estado;
    private long diasMora;
    private String rangoAntiguedad; // NO_VENCIDA, DE_1_A_30_DIAS, DE_31_A_60_DIAS, MAS_DE_60_DIAS

    public DeudaReporteItem() {
    }

    public UUID getObligacionId() { return obligacionId; }
    public void setObligacionId(UUID obligacionId) { this.obligacionId = obligacionId; }

    public String getCodigoObligacion() { return codigoObligacion; }
    public void setCodigoObligacion(String codigoObligacion) { this.codigoObligacion = codigoObligacion; }

    public String getComercianteNombre() { return comercianteNombre; }
    public void setComercianteNombre(String comercianteNombre) { this.comercianteNombre = comercianteNombre; }

    public String getDocumentoComerciante() { return documentoComerciante; }
    public void setDocumentoComerciante(String documentoComerciante) { this.documentoComerciante = documentoComerciante; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public String getPuestoCodigo() { return puestoCodigo; }
    public void setPuestoCodigo(String puestoCodigo) { this.puestoCodigo = puestoCodigo; }

    public String getConceptoNombre() { return conceptoNombre; }
    public void setConceptoNombre(String conceptoNombre) { this.conceptoNombre = conceptoNombre; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public BigDecimal getMontoOriginal() { return montoOriginal; }
    public void setMontoOriginal(BigDecimal montoOriginal) { this.montoOriginal = montoOriginal; }

    public BigDecimal getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(BigDecimal saldoPendiente) { this.saldoPendiente = saldoPendiente; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public long getDiasMora() { return diasMora; }
    public void setDiasMora(long diasMora) { this.diasMora = diasMora; }

    public String getRangoAntiguedad() { return rangoAntiguedad; }
    public void setRangoAntiguedad(String rangoAntiguedad) { this.rangoAntiguedad = rangoAntiguedad; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final DeudaReporteItem item = new DeudaReporteItem();

        public Builder obligacionId(UUID obligacionId) { item.obligacionId = obligacionId; return this; }
        public Builder codigoObligacion(String codigoObligacion) { item.codigoObligacion = codigoObligacion; return this; }
        public Builder comercianteNombre(String comercianteNombre) { item.comercianteNombre = comercianteNombre; return this; }
        public Builder documentoComerciante(String documentoComerciante) { item.documentoComerciante = documentoComerciante; return this; }
        public Builder mercadoNombre(String mercadoNombre) { item.mercadoNombre = mercadoNombre; return this; }
        public Builder puestoCodigo(String puestoCodigo) { item.puestoCodigo = puestoCodigo; return this; }
        public Builder conceptoNombre(String conceptoNombre) { item.conceptoNombre = conceptoNombre; return this; }
        public Builder fechaEmision(LocalDate fechaEmision) { item.fechaEmision = fechaEmision; return this; }
        public Builder fechaVencimiento(LocalDate fechaVencimiento) { item.fechaVencimiento = fechaVencimiento; return this; }
        public Builder montoOriginal(BigDecimal montoOriginal) { item.montoOriginal = montoOriginal; return this; }
        public Builder saldoPendiente(BigDecimal saldoPendiente) { item.saldoPendiente = saldoPendiente; return this; }
        public Builder estado(String estado) { item.estado = estado; return this; }
        public Builder diasMora(long diasMora) { item.diasMora = diasMora; return this; }
        public Builder rangoAntiguedad(String rangoAntiguedad) { item.rangoAntiguedad = rangoAntiguedad; return this; }

        public DeudaReporteItem build() {
            return item;
        }
    }
}
