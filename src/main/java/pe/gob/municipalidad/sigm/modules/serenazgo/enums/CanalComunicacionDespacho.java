package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum CanalComunicacionDespacho {
    RADIO_TETRA_VHF("Radiocomunicación (VHF / TETRA)"),
    RADIO_VHF("Radiocomunicación VHF"),
    TELEFONIA_MOVIL("Telefonía Móvil / Mensajería Oficial"),
    PRESENCIAL_BASE("Indicación Verbal Presencial en Base"),
    TERMINAL_DATOS("Terminal Portátil de Datos"),
    OTRO("Otro Canal Institucional");

    private final String descripcion;

    CanalComunicacionDespacho(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEtiqueta() {
        return descripcion;
    }
}
