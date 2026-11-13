package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum CanalReporteIncidencia {
    LLAMADA_OPERADOR("Llamada Telefónica a Central"),
    ALERTA_CIUDADANA("Alerta Digital de Portal Ciudadano"),
    PORTAL_CIUDADANO("Portal Web Ciudadano"),
    PATRULLAJE("Avistamiento en Patrullaje"),
    PRESENCIAL_BASE("Reporte Presencial en Base"),
    RADIO_COMUNICACION("Enlace Radial / Interinstitucional"),
    RADIO_VHF("Radiocomunicación VHF"),
    RADIO_TETRA_VHF("Radiocomunicación (VHF / TETRA)"),
    BOTON_PANICO("Botón de Pánico Digital"),
    OTRO("Otro Canal Oficial");

    private final String descripcion;

    CanalReporteIncidencia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static CanalReporteIncidencia fromString(String valor) {
        if (valor == null || valor.isBlank()) {
            return LLAMADA_OPERADOR;
        }
        for (CanalReporteIncidencia canal : values()) {
            if (canal.name().equalsIgnoreCase(valor.trim())) {
                return canal;
            }
        }
        return switch (valor.trim().toUpperCase()) {
            case "RADIO", "RADIO_VHF", "RADIO_TETRA_VHF" -> RADIO_VHF;
            case "PORTAL", "PORTAL_CIUDADANO", "WEB", "ALERTA" -> PORTAL_CIUDADANO;
            case "PATRULLAJE" -> PATRULLAJE;
            case "PRESENCIAL", "PRESENCIAL_BASE" -> PRESENCIAL_BASE;
            default -> LLAMADA_OPERADOR;
        };
    }
}
