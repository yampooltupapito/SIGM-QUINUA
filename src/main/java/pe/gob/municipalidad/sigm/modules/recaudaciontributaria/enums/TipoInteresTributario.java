package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de interés legalmente reconocidos en el Código Tributario peruano y normativa municipal.
 */
@Getter
@RequiredArgsConstructor
public enum TipoInteresTributario {
    TIM_MENSUAL("Tasa de Interés Moratorio Mensual (Art. 33 TUO Código Tributario)"),
    TIM_DIARIO("Tasa de Interés Moratorio Diaria (TIM Mensual / 30)"),
    COMPENSATORIO("Interés Compensatorio / Fraccionamiento");

    private final String descripcion;
}
