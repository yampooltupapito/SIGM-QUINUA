package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de reglas y beneficios tributarios estructurados.
 */
@Getter
@RequiredArgsConstructor
public enum TipoReglaTributaria {
    DEDUCCION_BASE_IMPONIBLE("Deducción de Base Imponible", "Deducción de un número de UITs (ej. adultos mayores o pensionistas)"),
    EXONERACION_TOTAL("Exoneración Total (100%)", "Exoneración legal íntegra de la obligación"),
    EXONERACION_PARCIAL("Exoneración Parcial", "Porcentaje de beneficio sobre el importe determinado"),
    TOPE_MINIMO_EMISION("Tope Mínimo Tributario", "Monto mínimo garantizado de cobro (ej. alícuota mínima de emisión)"),
    DESCUENTO_PRONTO_PAGO("Descuento por Pronto Pago", "Beneficio por cancelación al contado o dentro de plazo"),
    OTRA_REGLA("Otra Regla Normativa", "Regla específica aprobada por ordenanza");

    private final String descripcion;
    private final String detalle;

    public String getNombre() {
        return descripcion;
    }
}
