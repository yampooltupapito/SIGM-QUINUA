package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Beneficios, deducciones y exoneraciones aplicables al Impuesto Predial Municipal.
 */
@Getter
@RequiredArgsConstructor
public enum TipoBeneficioPredial {

    NINGUNO("Ninguno / Afecto Ordinario", "Afectación ordinaria sin deducción ni beneficio especial"),
    PENSIONISTA_ADULTO_MAYOR_50_UIT("Deducción 50 UIT (Pensionista / Adulto Mayor)", "Deducción de 50 UIT de la base imponible del predio único de vivienda propia conforme al Art. 19 del TUO de la Ley de Tributación Municipal y Ley 30490"),
    INAFECTACION_ESTATAL("Inafectación Estatal / Bien Público", "Inafectación expresa a favor del Estado, entidades públicas o templos religiosos"),
    EXONERACION_ESPECIAL("Exoneración Especial Autorizada", "Exoneración legal debidamente acreditada con resolución administrativa");

    private final String nombre;
    private final String descripcion;
}
