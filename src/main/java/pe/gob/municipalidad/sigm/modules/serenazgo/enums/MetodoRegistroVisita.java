package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum MetodoRegistroVisita {
    MANUAL("Registro Manual en Parte / Ficha"),
    GPS_VERIFICADO("GPS Verificado en Dispositivo"),
    SUPERVISADO("Confirmación por Supervisor de Turno"),
    RADIO_OPERADOR("Marcación por Radio a Central");

    private final String descripcion;

    MetodoRegistroVisita(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
