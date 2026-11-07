package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum EntidadComunicacion {
    POLICIA_NACIONAL_PERU("Policía Nacional del Perú", "Comisaría Rural PNP Quinua / Radio Patrulla"),
    SUBPREFECTURA("Subprefectura Distrital", "Subprefectura de Quinua - Garantías del Orden Público"),
    CENTRO_EMERGENCIA_MUJER("Centro Emergencia Mujer", "CEM - MIMP Quinua / Ayacucho"),
    CUERPO_BOMBEROS("Cuerpo de Bomberos", "Compañía de Bomberos Voluntarios"),
    DEFENSA_CIVIL("Defensa Civil Municipal", "Subgerencia de Gestión del Riesgo de Desastres"),
    ESTABLECIMIENTO_SALUD("Centro de Salud / MINSA", "Puesto de Salud de Quinua / SAMU 106"),
    RONDAS_CAMPESINAS("Rondas Campesinas / Comunales", "Organizaciones comunales de vigilancia rural"),
    FISCALIA_MINISTERIO_PUBLICO("Ministerio Público / Fiscalía", "Fiscalía de Prevención del Delito / Penal"),
    OTRA_DEPENDENCIA("Otra Dependencia", "Otra entidad pública o privada autorizada");

    private final String etiqueta;
    private final String descripcion;

    EntidadComunicacion(String etiqueta, String descripcion) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
