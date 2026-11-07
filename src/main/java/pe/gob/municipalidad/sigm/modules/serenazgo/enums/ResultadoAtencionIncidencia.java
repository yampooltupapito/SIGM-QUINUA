package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum ResultadoAtencionIncidencia {
    SITUACION_CONTROLADA("Situación Normalizada / Controlada"),
    VERIFICACION_SIN_NOVEDAD("Verificación en Sitio Sin Novedad"),
    DISPERSION_PREVENTIVA("Dispersión Preventiva Realizada"),
    ORIENTACION_CIUDADANA("Orientación y Auxilio al Ciudadano"),
    DERIVADO_A_POLICIA("Coordinación / Derivación a la PNP"),
    DERIVADO_A_SALUD("Atención de Salud / SAMU Coordinada"),
    NO_LOCALIZADO("Sujeto o Hecho No Localizado"),
    FALSA_ALARMA("Falsa Alarma / Inexistencia de Hecho"),
    OTRO("Otro Resultado Administrativo");

    private final String descripcion;

    ResultadoAtencionIncidencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEtiqueta() {
        return descripcion;
    }
}
