package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotNull;
import pe.gob.municipalidad.sigm.modules.mercados.enums.FormatoExportacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoReporteMercado;

public class ExportacionReporteRequest {

    @NotNull(message = "El tipo de reporte es obligatorio")
    private TipoReporteMercado tipoReporte;

    @NotNull(message = "El formato de exportación es obligatorio")
    private FormatoExportacion formato;

    private ReporteMercadoFilter filtro;

    public ExportacionReporteRequest() {
    }

    public ExportacionReporteRequest(TipoReporteMercado tipoReporte, FormatoExportacion formato, ReporteMercadoFilter filtro) {
        this.tipoReporte = tipoReporte;
        this.formato = formato;
        this.filtro = filtro;
    }

    public TipoReporteMercado getTipoReporte() {
        return tipoReporte;
    }

    public void setTipoReporte(TipoReporteMercado tipoReporte) {
        this.tipoReporte = tipoReporte;
    }

    public FormatoExportacion getFormato() {
        return formato;
    }

    public void setFormato(FormatoExportacion formato) {
        this.formato = formato;
    }

    public ReporteMercadoFilter getFiltro() {
        return filtro;
    }

    public void setFiltro(ReporteMercadoFilter filtro) {
        this.filtro = filtro;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TipoReporteMercado tipoReporte;
        private FormatoExportacion formato;
        private ReporteMercadoFilter filtro;

        public Builder tipoReporte(TipoReporteMercado tipoReporte) {
            this.tipoReporte = tipoReporte;
            return this;
        }

        public Builder formato(FormatoExportacion formato) {
            this.formato = formato;
            return this;
        }

        public Builder filtro(ReporteMercadoFilter filtro) {
            this.filtro = filtro;
            return this;
        }

        public ExportacionReporteRequest build() {
            return new ExportacionReporteRequest(tipoReporte, formato, filtro);
        }
    }
}
