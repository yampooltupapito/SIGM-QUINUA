package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class RecaudacionReporteItem {
    private UUID id;
    private String codigoPago;
    private OffsetDateTime fechaPago;
    private String mercadoNombre;
    private String puestoCodigo;
    private String comercianteNombre;
    private String conceptoNombre;
    private BigDecimal montoRecibido;
    private BigDecimal montoAplicado;
    private String medioPago;
    private String referenciaOperacion;
    private String estado;
    private String sesionCajaCodigo;
    private String usuarioCajero;

    public RecaudacionReporteItem() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigoPago() { return codigoPago; }
    public void setCodigoPago(String codigoPago) { this.codigoPago = codigoPago; }

    public OffsetDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(OffsetDateTime fechaPago) { this.fechaPago = fechaPago; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public String getPuestoCodigo() { return puestoCodigo; }
    public void setPuestoCodigo(String puestoCodigo) { this.puestoCodigo = puestoCodigo; }

    public String getComercianteNombre() { return comercianteNombre; }
    public void setComercianteNombre(String comercianteNombre) { this.comercianteNombre = comercianteNombre; }

    public String getConceptoNombre() { return conceptoNombre; }
    public void setConceptoNombre(String conceptoNombre) { this.conceptoNombre = conceptoNombre; }

    public BigDecimal getMontoRecibido() { return montoRecibido; }
    public void setMontoRecibido(BigDecimal montoRecibido) { this.montoRecibido = montoRecibido; }

    public BigDecimal getMontoAplicado() { return montoAplicado; }
    public void setMontoAplicado(BigDecimal montoAplicado) { this.montoAplicado = montoAplicado; }

    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }

    public String getReferenciaOperacion() { return referenciaOperacion; }
    public void setReferenciaOperacion(String referenciaOperacion) { this.referenciaOperacion = referenciaOperacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getSesionCajaCodigo() { return sesionCajaCodigo; }
    public void setSesionCajaCodigo(String sesionCajaCodigo) { this.sesionCajaCodigo = sesionCajaCodigo; }

    public String getUsuarioCajero() { return usuarioCajero; }
    public void setUsuarioCajero(String usuarioCajero) { this.usuarioCajero = usuarioCajero; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final RecaudacionReporteItem item = new RecaudacionReporteItem();

        public Builder id(UUID id) { item.id = id; return this; }
        public Builder codigoPago(String codigoPago) { item.codigoPago = codigoPago; return this; }
        public Builder fechaPago(OffsetDateTime fechaPago) { item.fechaPago = fechaPago; return this; }
        public Builder mercadoNombre(String mercadoNombre) { item.mercadoNombre = mercadoNombre; return this; }
        public Builder puestoCodigo(String puestoCodigo) { item.puestoCodigo = puestoCodigo; return this; }
        public Builder comercianteNombre(String comercianteNombre) { item.comercianteNombre = comercianteNombre; return this; }
        public Builder conceptoNombre(String conceptoNombre) { item.conceptoNombre = conceptoNombre; return this; }
        public Builder montoRecibido(BigDecimal montoRecibido) { item.montoRecibido = montoRecibido; return this; }
        public Builder montoAplicado(BigDecimal montoAplicado) { item.montoAplicado = montoAplicado; return this; }
        public Builder medioPago(String medioPago) { item.medioPago = medioPago; return this; }
        public Builder referenciaOperacion(String referenciaOperacion) { item.referenciaOperacion = referenciaOperacion; return this; }
        public Builder estado(String estado) { item.estado = estado; return this; }
        public Builder sesionCajaCodigo(String sesionCajaCodigo) { item.sesionCajaCodigo = sesionCajaCodigo; return this; }
        public Builder usuarioCajero(String usuarioCajero) { item.usuarioCajero = usuarioCajero; return this; }

        public RecaudacionReporteItem build() {
            return item;
        }
    }
}
