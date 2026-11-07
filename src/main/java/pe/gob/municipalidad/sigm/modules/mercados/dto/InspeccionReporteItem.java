package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.time.LocalDate;
import java.util.UUID;

public class InspeccionReporteItem {
    private UUID id;
    private String codigoInspeccion;
    private String mercadoNombre;
    private String puestoCodigo;
    private String titularNombre;
    private String tipoInspeccion;
    private LocalDate fechaProgramada;
    private String inspectorNombre;
    private String estado;
    private String calificacionFinal;
    private int cantidadHallazgos;
    private int cantidadMedidasPendientes;

    public InspeccionReporteItem() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigoInspeccion() { return codigoInspeccion; }
    public void setCodigoInspeccion(String codigoInspeccion) { this.codigoInspeccion = codigoInspeccion; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public String getPuestoCodigo() { return puestoCodigo; }
    public void setPuestoCodigo(String puestoCodigo) { this.puestoCodigo = puestoCodigo; }

    public String getTitularNombre() { return titularNombre; }
    public void setTitularNombre(String titularNombre) { this.titularNombre = titularNombre; }

    public String getTipoInspeccion() { return tipoInspeccion; }
    public void setTipoInspeccion(String tipoInspeccion) { this.tipoInspeccion = tipoInspeccion; }

    public LocalDate getFechaProgramada() { return fechaProgramada; }
    public void setFechaProgramada(LocalDate fechaProgramada) { this.fechaProgramada = fechaProgramada; }

    public String getInspectorNombre() { return inspectorNombre; }
    public void setInspectorNombre(String inspectorNombre) { this.inspectorNombre = inspectorNombre; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCalificacionFinal() { return calificacionFinal; }
    public void setCalificacionFinal(String calificacionFinal) { this.calificacionFinal = calificacionFinal; }

    public int getCantidadHallazgos() { return cantidadHallazgos; }
    public void setCantidadHallazgos(int cantidadHallazgos) { this.cantidadHallazgos = cantidadHallazgos; }

    public int getCantidadMedidasPendientes() { return cantidadMedidasPendientes; }
    public void setCantidadMedidasPendientes(int cantidadMedidasPendientes) { this.cantidadMedidasPendientes = cantidadMedidasPendientes; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final InspeccionReporteItem item = new InspeccionReporteItem();

        public Builder id(UUID id) { item.id = id; return this; }
        public Builder codigoInspeccion(String codigoInspeccion) { item.codigoInspeccion = codigoInspeccion; return this; }
        public Builder mercadoNombre(String mercadoNombre) { item.mercadoNombre = mercadoNombre; return this; }
        public Builder puestoCodigo(String puestoCodigo) { item.puestoCodigo = puestoCodigo; return this; }
        public Builder titularNombre(String titularNombre) { item.titularNombre = titularNombre; return this; }
        public Builder tipoInspeccion(String tipoInspeccion) { item.tipoInspeccion = tipoInspeccion; return this; }
        public Builder fechaProgramada(LocalDate fechaProgramada) { item.fechaProgramada = fechaProgramada; return this; }
        public Builder inspectorNombre(String inspectorNombre) { item.inspectorNombre = inspectorNombre; return this; }
        public Builder estado(String estado) { item.estado = estado; return this; }
        public Builder calificacionFinal(String calificacionFinal) { item.calificacionFinal = calificacionFinal; return this; }
        public Builder cantidadHallazgos(int cantidadHallazgos) { item.cantidadHallazgos = cantidadHallazgos; return this; }
        public Builder cantidadMedidasPendientes(int cantidadMedidasPendientes) { item.cantidadMedidasPendientes = cantidadMedidasPendientes; return this; }

        public InspeccionReporteItem build() {
            return item;
        }
    }
}
