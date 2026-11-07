package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum SentidoDecisionInfraccion {
    ARCHIVO("Archivo Definitivo", "Cierre por falta de infracción comprobada o prescripción"),
    ABSOLUCION("Absolución / Desestimación", "Determinación de no responsabilidad del administrado"),
    SANCION_FORMAL("Sanción Formal Motivada", "Sanción impuesta por resolución firme de autoridad competente"),
    MEDIDA_COMPLEMENTARIA("Medida Complementaria", "Clausura temporal, decomiso o adecuación ordenada por resolución");

    private final String label;
    private final String descripcion;

    SentidoDecisionInfraccion(String label, String descripcion) {
        this.label = label;
        this.descripcion = descripcion;
    }
}
