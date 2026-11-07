package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Naturaleza jurídica del concepto según el Código Tributario y la Ley de Tributación Municipal.
 */
@Getter
@RequiredArgsConstructor
public enum NaturalezaTributaria {
    IMPUESTO("Impuesto Municipal", "Tributo cuyo cumplimiento no origina contraprestación directa"),
    TASA_ARBITRIO("Tasa - Arbitrio Municipal", "Tasa por prestación, mantenimiento o mejora de un servicio público"),
    TASA_DERECHO("Tasa - Derecho Administrativo", "Tasa por trámite de procedimiento administrativo o uso de bienes públicos"),
    TASA_LICENCIA("Tasa - Licencia Municipal", "Tasa que grava la obtención de autorizaciones específicas para actividades"),
    CONTRIBUCION_ESPECIAL("Contribución Especial de Obras", "Tributo derivado de la realización de obras públicas o actividades estatales"),
    OTRO("Otro Ingreso Tributario", "Otros conceptos legalmente tipificados");

    private final String descripcion;
    private final String detalle;

    public String getNombre() {
        return descripcion;
    }
}
