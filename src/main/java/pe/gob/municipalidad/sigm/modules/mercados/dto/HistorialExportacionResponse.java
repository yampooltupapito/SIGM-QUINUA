package pe.gob.municipalidad.sigm.modules.mercados.dto;

import pe.gob.municipalidad.sigm.modules.mercados.enums.FormatoExportacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoReporteMercado;

import java.time.OffsetDateTime;
import java.util.UUID;

public class HistorialExportacionResponse {
    private UUID id;
    private String username;
    private TipoReporteMercado tipoReporte;
    private String tipoReporteTitulo;
    private FormatoExportacion formato;
    private String mercadoNombre;
    private OffsetDateTime fechaGeneracion;
    private String estado;
    private int cantidadRegistros;
    private String nombreArchivo;
    private long tamanioBytes;
    private String urlDescarga;

    public HistorialExportacionResponse() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public TipoReporteMercado getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(TipoReporteMercado tipoReporte) { this.tipoReporte = tipoReporte; }

    public String getTipoReporteTitulo() { return tipoReporteTitulo; }
    public void setTipoReporteTitulo(String tipoReporteTitulo) { this.tipoReporteTitulo = tipoReporteTitulo; }

    public FormatoExportacion getFormato() { return formato; }
    public void setFormato(FormatoExportacion formato) { this.formato = formato; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public OffsetDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(OffsetDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getCantidadRegistros() { return cantidadRegistros; }
    public void setCantidadRegistros(int cantidadRegistros) { this.cantidadRegistros = cantidadRegistros; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public long getTamanioBytes() { return tamanioBytes; }
    public void setTamanioBytes(long tamanioBytes) { this.tamanioBytes = tamanioBytes; }

    public String getUrlDescarga() { return urlDescarga; }
    public void setUrlDescarga(String urlDescarga) { this.urlDescarga = urlDescarga; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final HistorialExportacionResponse res = new HistorialExportacionResponse();

        public Builder id(UUID id) { res.id = id; return this; }
        public Builder username(String username) { res.username = username; return this; }
        public Builder tipoReporte(TipoReporteMercado tipoReporte) { res.tipoReporte = tipoReporte; return this; }
        public Builder tipoReporteTitulo(String tipoReporteTitulo) { res.tipoReporteTitulo = tipoReporteTitulo; return this; }
        public Builder formato(FormatoExportacion formato) { res.formato = formato; return this; }
        public Builder mercadoNombre(String mercadoNombre) { res.mercadoNombre = mercadoNombre; return this; }
        public Builder fechaGeneracion(OffsetDateTime fechaGeneracion) { res.fechaGeneracion = fechaGeneracion; return this; }
        public Builder estado(String estado) { res.estado = estado; return this; }
        public Builder cantidadRegistros(int cantidadRegistros) { res.cantidadRegistros = cantidadRegistros; return this; }
        public Builder nombreArchivo(String nombreArchivo) { res.nombreArchivo = nombreArchivo; return this; }
        public Builder tamanioBytes(long tamanioBytes) { res.tamanioBytes = tamanioBytes; return this; }
        public Builder urlDescarga(String urlDescarga) { res.urlDescarga = urlDescarga; return this; }

        public HistorialExportacionResponse build() {
            return res;
        }
    }
}
