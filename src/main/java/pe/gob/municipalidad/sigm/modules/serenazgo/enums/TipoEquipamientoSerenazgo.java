package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoEquipamientoSerenazgo {
    RADIO_COMUNICACION("Radio Transceptor VHF/UHF"),
    LINTERNA("Linterna Táctica Recargable"),
    CHALECO_DISTINTIVO("Chaleco Distintivo de Serenazgo"),
    CAMARA_TESTIGO("Cámara Testigo Corporal (Bodycam)"),
    SILBATO("Silbato Reglamentario"),
    VARA_LEY("Vara de Goma Defensiva Reglamentaria"),
    BOTIQUIN("Botiquín de Primeros Auxilios"),
    CONO_SEGURIDAD("Cono de Seguridad Vial"),
    OTRO("Otro Equipamiento Autorizado");

    private final String descripcion;

    TipoEquipamientoSerenazgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
