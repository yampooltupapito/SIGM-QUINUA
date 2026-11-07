package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum TipoInspeccionMercado {
    PROGRAMADA("Inspección Programada Ordinaria"),
    EXTRAORDINARIA("Inspección Extraordinaria / Inopinada"),
    SEGUIMIENTO("Inspección de Seguimiento a Medidas"),
    DOCUMENTAL("Verificación Documental y Contractual"),
    INFRAESTRUCTURA("Inspección Técnica de Puesto e Instalaciones"),
    SALUBRIDAD_HIGIENE("Control Sanitario, Higiene y Manipulación"),
    SALUBRIDAD("Control de Salubridad e Higiene"),
    SEGURIDAD("Inspección de Seguridad y Defensa Civil"),
    ORDENAMIENTO("Control de Ordenamiento Comercial"),
    CONTROL_PESAS("Control de Pesas y Medidas");

    private final String descripcion;

    TipoInspeccionMercado(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
