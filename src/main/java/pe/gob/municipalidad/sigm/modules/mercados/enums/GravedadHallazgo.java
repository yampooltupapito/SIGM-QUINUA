package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum GravedadHallazgo {
    LEVE("Leve - Subsanación Inmediata o Recomendación"),
    MODERADA("Moderada - Plazo de Subsanación Breve"),
    GRAVE("Grave - Afecta Salubridad u Orden General"),
    MUY_GRAVE("Muy Grave - Riesgo Inminente o Reincidencia");

    private final String descripcion;

    GravedadHallazgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
