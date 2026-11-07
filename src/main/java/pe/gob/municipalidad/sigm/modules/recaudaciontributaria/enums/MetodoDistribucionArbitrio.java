package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

@Getter
public enum MetodoDistribucionArbitrio {
    USO_PREDIO_ACTIVIDAD("Uso del Predio y Nivel de Actividad"),
    FRENTE_LINEAL("Longitud de Frente Lineal (Metros)"),
    AREA_CONSTRUIDA("Área Techada Construida (m2)"),
    UBICACION_ZONAL("Ubicación y Cobertura Zonal"),
    RIESGO_SECTOR("Nivel de Riesgo del Sector"),
    PONDERADO_MIXTO("Criterio Ponderado Mixto"),
    BASE_IGUALITARIA("Costo Equitativo Base");

    private final String descripcion;

    MetodoDistribucionArbitrio(String descripcion) {
        this.descripcion = descripcion;
    }
}
