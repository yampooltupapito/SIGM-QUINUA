package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Nivel de agregación temporal para series analíticas.
 */
@Getter
@RequiredArgsConstructor
public enum AgrupacionTemporal {

    DIA("Diaria", "yyyy-MM-dd"),
    SEMANA("Semanal", "yyyy-'W'ww"),
    MES("Mensual", "yyyy-MM"),
    TRIMESTRE("Trimestral", "yyyy-'Q'q"),
    ANIO("Anual", "yyyy");

    private final String descripcion;
    private final String patronFormato;
}
