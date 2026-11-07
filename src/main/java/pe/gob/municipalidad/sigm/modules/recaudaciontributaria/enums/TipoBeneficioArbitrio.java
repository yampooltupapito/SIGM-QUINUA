package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

@Getter
public enum TipoBeneficioArbitrio {
    NINGUNO("Sin Beneficio / Tarifa General", 0.0),
    EXONERACION_ESTATAL_TOTAL("Inafectación Estatal / Entidad Pública (100%)", 100.0),
    BENEFICIO_SOCIAL_ADULTO_MAYOR("Beneficio Social Adulto Mayor / Pensionista (50%)", 50.0),
    DESCUENTO_PREDIO_DESOCUPADO("Predio Desocupado / Terreno sin Uso (30%)", 30.0),
    INAFECTACION_LEGAL("Inafectación Constitucional / Legal Expresa (100%)", 100.0);

    private final String descripcion;
    private final double porcentajeDescuento;

    TipoBeneficioArbitrio(String descripcion, double porcentajeDescuento) {
        this.descripcion = descripcion;
        this.porcentajeDescuento = porcentajeDescuento;
    }
}
