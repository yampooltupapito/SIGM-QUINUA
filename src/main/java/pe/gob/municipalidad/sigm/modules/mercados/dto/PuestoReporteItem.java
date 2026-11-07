package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PuestoReporteItem {
    private UUID id;
    private String codigo;
    private String numero;
    private String mercadoNombre;
    private String sectorNombre;
    private String pabellonNombre;
    private String tipoPuesto;
    private BigDecimal areaM2;
    private String estadoFisico;
    private String estadoOcupacion;
    private String titularActual;
    private String giroActual;

    public PuestoReporteItem() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public String getSectorNombre() { return sectorNombre; }
    public void setSectorNombre(String sectorNombre) { this.sectorNombre = sectorNombre; }

    public String getPabellonNombre() { return pabellonNombre; }
    public void setPabellonNombre(String pabellonNombre) { this.pabellonNombre = pabellonNombre; }

    public String getTipoPuesto() { return tipoPuesto; }
    public void setTipoPuesto(String tipoPuesto) { this.tipoPuesto = tipoPuesto; }

    public BigDecimal getAreaM2() { return areaM2; }
    public void setAreaM2(BigDecimal areaM2) { this.areaM2 = areaM2; }

    public String getEstadoFisico() { return estadoFisico; }
    public void setEstadoFisico(String estadoFisico) { this.estadoFisico = estadoFisico; }

    public String getEstadoOcupacion() { return estadoOcupacion; }
    public void setEstadoOcupacion(String estadoOcupacion) { this.estadoOcupacion = estadoOcupacion; }

    public String getTitularActual() { return titularActual; }
    public void setTitularActual(String titularActual) { this.titularActual = titularActual; }

    public String getGiroActual() { return giroActual; }
    public void setGiroActual(String giroActual) { this.giroActual = giroActual; }

    public BigDecimal getArea() { return areaM2; }
    public String getEstado() { return estadoOcupacion; }
    public String getOcupanteActual() { return titularActual; }
    public Integer getNivel() { return 1; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PuestoReporteItem item = new PuestoReporteItem();

        public Builder id(UUID id) { item.id = id; return this; }
        public Builder codigo(String codigo) { item.codigo = codigo; return this; }
        public Builder numero(String numero) { item.numero = numero; return this; }
        public Builder mercadoNombre(String mercadoNombre) { item.mercadoNombre = mercadoNombre; return this; }
        public Builder sectorNombre(String sectorNombre) { item.sectorNombre = sectorNombre; return this; }
        public Builder pabellonNombre(String pabellonNombre) { item.pabellonNombre = pabellonNombre; return this; }
        public Builder tipoPuesto(String tipoPuesto) { item.tipoPuesto = tipoPuesto; return this; }
        public Builder areaM2(BigDecimal areaM2) { item.areaM2 = areaM2; return this; }
        public Builder estadoFisico(String estadoFisico) { item.estadoFisico = estadoFisico; return this; }
        public Builder estadoOcupacion(String estadoOcupacion) { item.estadoOcupacion = estadoOcupacion; return this; }
        public Builder titularActual(String titularActual) { item.titularActual = titularActual; return this; }
        public Builder giroActual(String giroActual) { item.giroActual = giroActual; return this; }

        public PuestoReporteItem build() {
            return item;
        }
    }
}
