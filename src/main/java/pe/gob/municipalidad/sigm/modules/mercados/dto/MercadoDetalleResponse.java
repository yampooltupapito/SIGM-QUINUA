package pe.gob.municipalidad.sigm.modules.mercados.dto;

import pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.PuestoResponse;

import java.util.List;

public class MercadoDetalleResponse {
    private MercadoResponse mercado;
    private ResumenPuestosMercadoResponse resumenPuestos;
    private List<SectorResponse> sectores;
    private List<PabellonResponse> pabellones;
    private List<PuestoResponse> puestos;
    private List<MercadoDocumentoResponse> documentos;
    private List<MercadoHistorialItemResponse> historialEstados;
    private long totalAsignacionesVigentes;

    public MercadoDetalleResponse() {
    }

    public MercadoDetalleResponse(MercadoResponse mercado, ResumenPuestosMercadoResponse resumenPuestos,
                                  List<SectorResponse> sectores, List<PabellonResponse> pabellones,
                                  List<PuestoResponse> puestos, List<MercadoDocumentoResponse> documentos,
                                  List<MercadoHistorialItemResponse> historialEstados, long totalAsignacionesVigentes) {
        this.mercado = mercado;
        this.resumenPuestos = resumenPuestos;
        this.sectores = sectores;
        this.pabellones = pabellones;
        this.puestos = puestos;
        this.documentos = documentos;
        this.historialEstados = historialEstados;
        this.totalAsignacionesVigentes = totalAsignacionesVigentes;
    }

    public static MercadoDetalleResponseBuilder builder() {
        return new MercadoDetalleResponseBuilder();
    }

    public MercadoResponse getMercado() { return mercado; }
    public void setMercado(MercadoResponse mercado) { this.mercado = mercado; }

    public ResumenPuestosMercadoResponse getResumenPuestos() { return resumenPuestos; }
    public void setResumenPuestos(ResumenPuestosMercadoResponse resumenPuestos) { this.resumenPuestos = resumenPuestos; }

    public List<SectorResponse> getSectores() { return sectores; }
    public void setSectores(List<SectorResponse> sectores) { this.sectores = sectores; }

    public List<PabellonResponse> getPabellones() { return pabellones; }
    public void setPabellones(List<PabellonResponse> pabellones) { this.pabellones = pabellones; }

    public List<PuestoResponse> getPuestos() { return puestos; }
    public void setPuestos(List<PuestoResponse> puestos) { this.puestos = puestos; }

    public List<MercadoDocumentoResponse> getDocumentos() { return documentos; }
    public void setDocumentos(List<MercadoDocumentoResponse> documentos) { this.documentos = documentos; }

    public List<MercadoHistorialItemResponse> getHistorialEstados() { return historialEstados; }
    public void setHistorialEstados(List<MercadoHistorialItemResponse> historialEstados) { this.historialEstados = historialEstados; }

    public long getTotalAsignacionesVigentes() { return totalAsignacionesVigentes; }
    public void setTotalAsignacionesVigentes(long totalAsignacionesVigentes) { this.totalAsignacionesVigentes = totalAsignacionesVigentes; }

    public static class MercadoDetalleResponseBuilder {
        private MercadoResponse mercado;
        private ResumenPuestosMercadoResponse resumenPuestos;
        private List<SectorResponse> sectores;
        private List<PabellonResponse> pabellones;
        private List<PuestoResponse> puestos;
        private List<MercadoDocumentoResponse> documentos;
        private List<MercadoHistorialItemResponse> historialEstados;
        private long totalAsignacionesVigentes;

        public MercadoDetalleResponseBuilder mercado(MercadoResponse mercado) { this.mercado = mercado; return this; }
        public MercadoDetalleResponseBuilder resumenPuestos(ResumenPuestosMercadoResponse resumenPuestos) { this.resumenPuestos = resumenPuestos; return this; }
        public MercadoDetalleResponseBuilder sectores(List<SectorResponse> sectores) { this.sectores = sectores; return this; }
        public MercadoDetalleResponseBuilder pabellones(List<PabellonResponse> pabellones) { this.pabellones = pabellones; return this; }
        public MercadoDetalleResponseBuilder puestos(List<PuestoResponse> puestos) { this.puestos = puestos; return this; }
        public MercadoDetalleResponseBuilder documentos(List<MercadoDocumentoResponse> documentos) { this.documentos = documentos; return this; }
        public MercadoDetalleResponseBuilder historialEstados(List<MercadoHistorialItemResponse> historialEstados) { this.historialEstados = historialEstados; return this; }
        public MercadoDetalleResponseBuilder totalAsignacionesVigentes(long totalAsignacionesVigentes) { this.totalAsignacionesVigentes = totalAsignacionesVigentes; return this; }

        public MercadoDetalleResponse build() {
            return new MercadoDetalleResponse(mercado, resumenPuestos, sectores, pabellones, puestos, documentos, historialEstados, totalAsignacionesVigentes);
        }
    }
}
