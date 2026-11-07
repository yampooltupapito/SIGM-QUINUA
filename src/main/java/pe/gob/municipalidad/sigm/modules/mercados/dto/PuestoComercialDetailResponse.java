package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.util.ArrayList;
import java.util.List;

public class PuestoComercialDetailResponse {

    private PuestoComercialResponse puesto;
    private String ocupanteActualNombre;
    private String ocupanteActualDni;
    private String ocupanteContratoNumero;
    private List<PuestoCambioEstadoResponse> historialEstados = new ArrayList<>();
    private List<HistorialUbicacionResponse> historialUbicacion = new ArrayList<>();
    private List<PuestoDocumentoResponse> documentos = new ArrayList<>();
    private List<AsignacionResponse> asignaciones = new ArrayList<>();

    public PuestoComercialDetailResponse() {
    }

    public PuestoComercialDetailResponse(PuestoComercialResponse puesto, String ocupanteActualNombre,
                                         String ocupanteActualDni, String ocupanteContratoNumero,
                                         List<PuestoCambioEstadoResponse> historialEstados,
                                         List<HistorialUbicacionResponse> historialUbicacion,
                                         List<PuestoDocumentoResponse> documentos,
                                         List<AsignacionResponse> asignaciones) {
        this.puesto = puesto;
        this.ocupanteActualNombre = ocupanteActualNombre;
        this.ocupanteActualDni = ocupanteActualDni;
        this.ocupanteContratoNumero = ocupanteContratoNumero;
        this.historialEstados = historialEstados != null ? historialEstados : new ArrayList<>();
        this.historialUbicacion = historialUbicacion != null ? historialUbicacion : new ArrayList<>();
        this.documentos = documentos != null ? documentos : new ArrayList<>();
        this.asignaciones = asignaciones != null ? asignaciones : new ArrayList<>();
    }

    public static PuestoComercialDetailResponseBuilder builder() {
        return new PuestoComercialDetailResponseBuilder();
    }

    public PuestoComercialResponse getPuesto() { return puesto; }
    public void setPuesto(PuestoComercialResponse puesto) { this.puesto = puesto; }

    public String getOcupanteActualNombre() { return ocupanteActualNombre; }
    public void setOcupanteActualNombre(String ocupanteActualNombre) { this.ocupanteActualNombre = ocupanteActualNombre; }

    public String getOcupanteActualDni() { return ocupanteActualDni; }
    public void setOcupanteActualDni(String ocupanteActualDni) { this.ocupanteActualDni = ocupanteActualDni; }

    public String getOcupanteContratoNumero() { return ocupanteContratoNumero; }
    public void setOcupanteContratoNumero(String ocupanteContratoNumero) { this.ocupanteContratoNumero = ocupanteContratoNumero; }

    public List<PuestoCambioEstadoResponse> getHistorialEstados() { return historialEstados; }
    public void setHistorialEstados(List<PuestoCambioEstadoResponse> historialEstados) { this.historialEstados = historialEstados; }

    public List<HistorialUbicacionResponse> getHistorialUbicacion() { return historialUbicacion; }
    public void setHistorialUbicacion(List<HistorialUbicacionResponse> historialUbicacion) { this.historialUbicacion = historialUbicacion; }

    public List<PuestoDocumentoResponse> getDocumentos() { return documentos; }
    public void setDocumentos(List<PuestoDocumentoResponse> documentos) { this.documentos = documentos; }

    public List<AsignacionResponse> getAsignaciones() { return asignaciones; }
    public void setAsignaciones(List<AsignacionResponse> asignaciones) { this.asignaciones = asignaciones; }

    public static class PuestoComercialDetailResponseBuilder {
        private PuestoComercialResponse puesto;
        private String ocupanteActualNombre;
        private String ocupanteActualDni;
        private String ocupanteContratoNumero;
        private List<PuestoCambioEstadoResponse> historialEstados = new ArrayList<>();
        private List<HistorialUbicacionResponse> historialUbicacion = new ArrayList<>();
        private List<PuestoDocumentoResponse> documentos = new ArrayList<>();
        private List<AsignacionResponse> asignaciones = new ArrayList<>();

        public PuestoComercialDetailResponseBuilder puesto(PuestoComercialResponse puesto) { this.puesto = puesto; return this; }
        public PuestoComercialDetailResponseBuilder ocupanteActualNombre(String ocupanteActualNombre) { this.ocupanteActualNombre = ocupanteActualNombre; return this; }
        public PuestoComercialDetailResponseBuilder ocupanteActualDni(String ocupanteActualDni) { this.ocupanteActualDni = ocupanteActualDni; return this; }
        public PuestoComercialDetailResponseBuilder ocupanteContratoNumero(String ocupanteContratoNumero) { this.ocupanteContratoNumero = ocupanteContratoNumero; return this; }
        public PuestoComercialDetailResponseBuilder historialEstados(List<PuestoCambioEstadoResponse> historialEstados) { this.historialEstados = historialEstados; return this; }
        public PuestoComercialDetailResponseBuilder historialUbicacion(List<HistorialUbicacionResponse> historialUbicacion) { this.historialUbicacion = historialUbicacion; return this; }
        public PuestoComercialDetailResponseBuilder documentos(List<PuestoDocumentoResponse> documentos) { this.documentos = documentos; return this; }
        public PuestoComercialDetailResponseBuilder asignaciones(List<AsignacionResponse> asignaciones) { this.asignaciones = asignaciones; return this; }

        public PuestoComercialDetailResponse build() {
            return new PuestoComercialDetailResponse(puesto, ocupanteActualNombre, ocupanteActualDni,
                    ocupanteContratoNumero, historialEstados, historialUbicacion, documentos, asignaciones);
        }
    }
}
