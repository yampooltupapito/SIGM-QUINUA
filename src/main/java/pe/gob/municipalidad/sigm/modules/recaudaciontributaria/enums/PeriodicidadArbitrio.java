package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;

@Getter
public enum PeriodicidadArbitrio {
    MENSUAL("Mensual", 12),
    TRIMESTRAL("Trimestral", 4),
    SEMESTRAL("Semestral", 2),
    ANUAL("Anual", 1);

    private final String descripcion;
    private final int cuotasPorAnio;

    PeriodicidadArbitrio(String descripcion, int cuotasPorAnio) {
        this.descripcion = descripcion;
        this.cuotasPorAnio = cuotasPorAnio;
    }
}
