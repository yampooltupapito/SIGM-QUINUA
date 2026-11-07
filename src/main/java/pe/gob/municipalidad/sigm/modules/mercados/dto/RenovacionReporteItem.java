package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.time.LocalDate;
import java.util.UUID;

public class RenovacionReporteItem {
    private UUID id;
    private String codigoSolicitud;
    private String tipoSolicitud;
    private String mercadoNombre;
    private String puestoCodigo;
    private String comercianteActual;
    private String comerciantePropuesto;
    private LocalDate fechaPresentacion;
    private String estado;
    private String resultado;
    private String motivo;

    public RenovacionReporteItem() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigoSolicitud() { return codigoSolicitud; }
    public void setCodigoSolicitud(String codigoSolicitud) { this.codigoSolicitud = codigoSolicitud; }

    public String getTipoSolicitud() { return tipoSolicitud; }
    public void setTipoSolicitud(String tipoSolicitud) { this.tipoSolicitud = tipoSolicitud; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public String getPuestoCodigo() { return puestoCodigo; }
    public void setPuestoCodigo(String puestoCodigo) { this.puestoCodigo = puestoCodigo; }

    public String getComercianteActual() { return comercianteActual; }
    public void setComercianteActual(String comercianteActual) { this.comercianteActual = comercianteActual; }

    public String getComerciantePropuesto() { return comerciantePropuesto; }
    public void setComerciantePropuesto(String comerciantePropuesto) { this.comerciantePropuesto = comerciantePropuesto; }

    public LocalDate getFechaPresentacion() { return fechaPresentacion; }
    public void setFechaPresentacion(LocalDate fechaPresentacion) { this.fechaPresentacion = fechaPresentacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final RenovacionReporteItem item = new RenovacionReporteItem();

        public Builder id(UUID id) { item.id = id; return this; }
        public Builder codigoSolicitud(String codigoSolicitud) { item.codigoSolicitud = codigoSolicitud; return this; }
        public Builder tipoSolicitud(String tipoSolicitud) { item.tipoSolicitud = tipoSolicitud; return this; }
        public Builder mercadoNombre(String mercadoNombre) { item.mercadoNombre = mercadoNombre; return this; }
        public Builder puestoCodigo(String puestoCodigo) { item.puestoCodigo = puestoCodigo; return this; }
        public Builder comercianteActual(String comercianteActual) { item.comercianteActual = comercianteActual; return this; }
        public Builder comerciantePropuesto(String comerciantePropuesto) { item.comerciantePropuesto = comerciantePropuesto; return this; }
        public Builder fechaPresentacion(LocalDate fechaPresentacion) { item.fechaPresentacion = fechaPresentacion; return this; }
        public Builder estado(String estado) { item.estado = estado; return this; }
        public Builder resultado(String resultado) { item.resultado = resultado; return this; }
        public Builder motivo(String motivo) { item.motivo = motivo; return this; }

        public RenovacionReporteItem build() {
            return item;
        }
    }
}
