package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoPatrullajeSerenazgo {
    PATRULLAJE_MUNICIPAL("Patrullaje Municipal Ordinario"),
    PATRULLAJE_A_PIE("Patrullaje Preventivo a Pie"),
    PATRULLAJE_MOTORIZADO("Patrullaje Preventivo Motorizado"),
    PATRULLAJE_INTEGRADO_PNP("Patrullaje Integrado con PNP"),
    VIGILANCIA_ESTATICA("Puesto Fijo / Vigilancia Estática"),
    MIXTO("Patrullaje Mixto (A Pie y Motorizado)");

    private final String descripcion;

    TipoPatrullajeSerenazgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
