package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum OrigenHechoIncidencia {
    HECHO_REPORTADO("Hecho Reportado por Vecino o Tercero"),
    HECHO_OBSERVADO("Hecho Observado Directamente por Efectivo de Serenazgo"),
    COMUNICACION_OFICIAL("Comunicación Oficial Interinstitucional / PNP / Bomberos");

    private final String descripcion;

    OrigenHechoIncidencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
