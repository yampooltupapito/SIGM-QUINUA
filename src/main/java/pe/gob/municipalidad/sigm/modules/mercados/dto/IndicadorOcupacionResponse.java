package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class IndicadorOcupacionResponse {

    private long totalPuestos;
    private long puestosHabilitados;
    private long puestosOcupados;
    private long puestosDisponibles;
    private long puestosMantenimiento;
    private long puestosRestringidos;
    private long puestosInactivos;
    private BigDecimal porcentajeOcupacion;
    private String mensajeOcupacion;

    private List<Map<String, Object>> distribucionPorEstado;
    private List<Map<String, Object>> ocupacionPorMercado;
    private List<Map<String, Object>> ocupacionPorSector;
    private List<Map<String, Object>> ocupacionPorPabellon;

    public IndicadorOcupacionResponse() {
    }

    public String getPorcentajeOcupacionTexto() {
        if (puestosHabilitados > 0 && porcentajeOcupacion != null) {
            return String.format(java.util.Locale.US, "%.1f%%", porcentajeOcupacion);
        }
        return "Sin datos suficientes";
    }

    public long getPuestosClausurados() { return puestosRestringidos; }

    public long getTotalPuestos() { return totalPuestos; }
    public void setTotalPuestos(long totalPuestos) { this.totalPuestos = totalPuestos; }

    public long getPuestosHabilitados() { return puestosHabilitados; }
    public void setPuestosHabilitados(long puestosHabilitados) { this.puestosHabilitados = puestosHabilitados; }

    public long getPuestosOcupados() { return puestosOcupados; }
    public void setPuestosOcupados(long puestosOcupados) { this.puestosOcupados = puestosOcupados; }

    public long getPuestosDisponibles() { return puestosDisponibles; }
    public void setPuestosDisponibles(long puestosDisponibles) { this.puestosDisponibles = puestosDisponibles; }

    public long getPuestosMantenimiento() { return puestosMantenimiento; }
    public void setPuestosMantenimiento(long puestosMantenimiento) { this.puestosMantenimiento = puestosMantenimiento; }

    public long getPuestosRestringidos() { return puestosRestringidos; }
    public void setPuestosRestringidos(long puestosRestringidos) { this.puestosRestringidos = puestosRestringidos; }

    public long getPuestosInactivos() { return puestosInactivos; }
    public void setPuestosInactivos(long puestosInactivos) { this.puestosInactivos = puestosInactivos; }

    public BigDecimal getPorcentajeOcupacion() { return porcentajeOcupacion; }
    public void setPorcentajeOcupacion(BigDecimal porcentajeOcupacion) { this.porcentajeOcupacion = porcentajeOcupacion; }

    public String getMensajeOcupacion() { return mensajeOcupacion; }
    public void setMensajeOcupacion(String mensajeOcupacion) { this.mensajeOcupacion = mensajeOcupacion; }

    public List<Map<String, Object>> getDistribucionPorEstado() { return distribucionPorEstado; }
    public void setDistribucionPorEstado(List<Map<String, Object>> distribucionPorEstado) { this.distribucionPorEstado = distribucionPorEstado; }

    public List<Map<String, Object>> getOcupacionPorMercado() { return ocupacionPorMercado; }
    public void setOcupacionPorMercado(List<Map<String, Object>> ocupacionPorMercado) { this.ocupacionPorMercado = ocupacionPorMercado; }

    public List<Map<String, Object>> getOcupacionPorSector() { return ocupacionPorSector; }
    public void setOcupacionPorSector(List<Map<String, Object>> ocupacionPorSector) { this.ocupacionPorSector = ocupacionPorSector; }

    public List<Map<String, Object>> getOcupacionPorPabellon() { return ocupacionPorPabellon; }
    public void setOcupacionPorPabellon(List<Map<String, Object>> ocupacionPorPabellon) { this.ocupacionPorPabellon = ocupacionPorPabellon; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final IndicadorOcupacionResponse r = new IndicadorOcupacionResponse();

        public Builder totalPuestos(long val) { r.totalPuestos = val; return this; }
        public Builder puestosHabilitados(long val) { r.puestosHabilitados = val; return this; }
        public Builder puestosOcupados(long val) { r.puestosOcupados = val; return this; }
        public Builder puestosDisponibles(long val) { r.puestosDisponibles = val; return this; }
        public Builder puestosMantenimiento(long val) { r.puestosMantenimiento = val; return this; }
        public Builder puestosRestringidos(long val) { r.puestosRestringidos = val; return this; }
        public Builder puestosInactivos(long val) { r.puestosInactivos = val; return this; }
        public Builder porcentajeOcupacion(BigDecimal val) { r.porcentajeOcupacion = val; return this; }
        public Builder mensajeOcupacion(String val) { r.mensajeOcupacion = val; return this; }
        public Builder distribucionPorEstado(List<Map<String, Object>> val) { r.distribucionPorEstado = val; return this; }
        public Builder ocupacionPorMercado(List<Map<String, Object>> val) { r.ocupacionPorMercado = val; return this; }
        public Builder ocupacionPorSector(List<Map<String, Object>> val) { r.ocupacionPorSector = val; return this; }
        public Builder ocupacionPorPabellon(List<Map<String, Object>> val) { r.ocupacionPorPabellon = val; return this; }

        public IndicadorOcupacionResponse build() {
            return r;
        }
    }
}
