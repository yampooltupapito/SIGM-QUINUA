package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EntidadEscalamiento {
    POLICIA_NACIONAL_PERU("Policía Nacional del Perú (PNP)"),
    SAMU_SALUD("Servicio de Atención Médica de Urgencia (SAMU/Salud)"),
    BOMBEROS_CGBVP("Cuerpo General de Bomberos Voluntarios (CGBVP)"),
    CENTRO_EMERGENCIA_MUJER("Centro Emergencia Mujer (CEM)"),
    MINISTERIO_PUBLICO("Ministerio Público / Fiscalía"),
    DEFENSA_CIVIL("Defensa Civil / Gestión del Riesgo de Desastres"),
    SUBPREFECTURA("Subprefectura Distrital"),
    OTRA("Otra Entidad Competente");

    private final String descripcion;

    EntidadEscalamiento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEtiqueta() {
        return descripcion;
    }
}
