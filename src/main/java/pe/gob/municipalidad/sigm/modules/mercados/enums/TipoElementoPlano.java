package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum TipoElementoPlano {
    MERCADO("Límite Mercado"),
    SECTOR("Sector"),
    PABELLON("Pabellón"),
    ZONA("Zona Comercial"),
    PUESTO("Puesto Comercial"),
    PASILLO("Pasillo / Circulación"),
    ACCESO("Acceso / Entrada"),
    SALIDA("Salida de Emergencia"),
    ESCALERA("Escalera / Rampa"),
    AREA_COMUN("Área Común / Servicios"),
    ETIQUETA("Etiqueta de Texto"),
    OTRO("Otro Elemento");

    private final String label;

    TipoElementoPlano(String label) {
        this.label = label;
    }
}
