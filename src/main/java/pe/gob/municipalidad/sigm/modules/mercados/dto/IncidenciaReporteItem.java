package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class IncidenciaReporteItem {
    private UUID id;
    private String codigoIncidencia;
    private String mercadoNombre;
    private String puestoCodigo;
    private String tipoIncidencia;
    private String origen;
    private String prioridad;
    private String estado;
    private OffsetDateTime fechaReporte;
    private String responsableNombre;
    private Double tiempoAtencionHoras;
    private String descripcion;

    public IncidenciaReporteItem() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigoIncidencia() { return codigoIncidencia; }
    public void setCodigoIncidencia(String codigoIncidencia) { this.codigoIncidencia = codigoIncidencia; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public String getPuestoCodigo() { return puestoCodigo; }
    public void setPuestoCodigo(String puestoCodigo) { this.puestoCodigo = puestoCodigo; }

    public String getTipoIncidencia() { return tipoIncidencia; }
    public void setTipoIncidencia(String tipoIncidencia) { this.tipoIncidencia = tipoIncidencia; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public OffsetDateTime getFechaReporte() { return fechaReporte; }
    public void setFechaReporte(OffsetDateTime fechaReporte) { this.fechaReporte = fechaReporte; }

    public String getResponsableNombre() { return responsableNombre; }
    public void setResponsableNombre(String responsableNombre) { this.responsableNombre = responsableNombre; }

    public Double getTiempoAtencionHoras() { return tiempoAtencionHoras; }
    public void setTiempoAtencionHoras(Double tiempoAtencionHoras) { this.tiempoAtencionHoras = tiempoAtencionHoras; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final IncidenciaReporteItem item = new IncidenciaReporteItem();

        public Builder id(UUID id) { item.id = id; return this; }
        public Builder codigoIncidencia(String codigoIncidencia) { item.codigoIncidencia = codigoIncidencia; return this; }
        public Builder mercadoNombre(String mercadoNombre) { item.mercadoNombre = mercadoNombre; return this; }
        public Builder puestoCodigo(String puestoCodigo) { item.puestoCodigo = puestoCodigo; return this; }
        public Builder tipoIncidencia(String tipoIncidencia) { item.tipoIncidencia = tipoIncidencia; return this; }
        public Builder origen(String origen) { item.origen = origen; return this; }
        public Builder prioridad(String prioridad) { item.prioridad = prioridad; return this; }
        public Builder estado(String estado) { item.estado = estado; return this; }
        public Builder fechaReporte(OffsetDateTime fechaReporte) { item.fechaReporte = fechaReporte; return this; }
        public Builder responsableNombre(String responsableNombre) { item.responsableNombre = responsableNombre; return this; }
        public Builder tiempoAtencionHoras(Double tiempoAtencionHoras) { item.tiempoAtencionHoras = tiempoAtencionHoras; return this; }
        public Builder descripcion(String descripcion) { item.descripcion = descripcion; return this; }

        public IncidenciaReporteItem build() {
            return item;
        }
    }
}
