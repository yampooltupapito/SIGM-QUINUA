package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PeriodicidadConcepto {
    ANUAL("Anual", 1),
    TRIMESTRAL("Trimestral", 4),
    MENSUAL("Mensual", 12),
    UNICO("Único / Eventual", 1);

    private final String descripcion;
    private final int cuotasPorAnio;

    public String getNombre() {
        return descripcion;
    }
}
