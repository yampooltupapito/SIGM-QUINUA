package pe.gob.municipalidad.sigm.modules.mercados.enums;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

public enum PeriodoConsulta {
    HOY("Hoy"),
    ULTIMOS_7_DIAS("Últimos 7 días"),
    MES_ACTUAL("Mes Actual"),
    MES_ANTERIOR("Mes Anterior"),
    ANIO_ACTUAL("Año Actual"),
    PERSONALIZADO("Rango Personalizado");

    private final String label;
    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    PeriodoConsulta(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getDescripcion() {
        return label;
    }

    public LocalDate[] resolverRango(LocalDate fechaDesde, LocalDate fechaHasta) {
        LocalDate hoy = LocalDate.now(ZONA_LIMA);
        switch (this) {
            case HOY:
                return new LocalDate[]{hoy, hoy};
            case ULTIMOS_7_DIAS:
                return new LocalDate[]{hoy.minusDays(6), hoy};
            case MES_ACTUAL:
                YearMonth ymActual = YearMonth.from(hoy);
                return new LocalDate[]{ymActual.atDay(1), ymActual.atEndOfMonth()};
            case MES_ANTERIOR:
                YearMonth ymAnterior = YearMonth.from(hoy).minusMonths(1);
                return new LocalDate[]{ymAnterior.atDay(1), ymAnterior.atEndOfMonth()};
            case ANIO_ACTUAL:
                return new LocalDate[]{LocalDate.of(hoy.getYear(), 1, 1), LocalDate.of(hoy.getYear(), 12, 31)};
            case PERSONALIZADO:
            default:
                LocalDate desde = fechaDesde != null ? fechaDesde : hoy.minusMonths(1);
                LocalDate hasta = fechaHasta != null ? fechaHasta : hoy;
                if (desde.isAfter(hasta)) {
                    LocalDate temp = desde;
                    desde = hasta;
                    hasta = temp;
                }
                return new LocalDate[]{desde, hasta};
        }
    }
}
