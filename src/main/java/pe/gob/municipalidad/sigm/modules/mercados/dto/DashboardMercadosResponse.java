package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardMercadosResponse {

    // Infraestructura
    private long totalMercados;
    private long mercadosActivos;
    private long totalSectores;
    private long totalPabellones;
    private long totalPuestos;
    private long puestosOcupados;
    private long puestosDisponibles;
    private long puestosMantenimiento;
    private long puestosRestringidos;
    private BigDecimal porcentajeOcupacion;
    private String textoOcupacion;

    // Padrón y Legal
    private long totalComerciantes;
    private long comerciantesActivos;
    private long asignacionesVigentes;
    private long contratosVigentes;
    private long autorizacionesVigentes;
    private long instrumentosPorVencer;

    // Finanzas
    private BigDecimal obligacionesEmitidasMonto;
    private BigDecimal recaudacionBrutaMonto;
    private BigDecimal reversosMonto;
    private BigDecimal recaudacionNetaMonto;
    private BigDecimal deudaExigibleMonto;
    private long obligacionesPendientesCount;
    private long pagosConfirmadosCount;

    // Fiscalización y Operaciones
    private long inspeccionesRealizadas;
    private long inspeccionesConHallazgos;
    private long incidenciasAbiertas;
    private long incidenciasCriticas;
    private long renovacionesEnTramite;

    // Comparación con periodo anterior
    private BigDecimal recaudacionPeriodoAnterior;
    private BigDecimal variacionRecaudacionPorcentaje;
    private boolean tendenciaRecaudacionPositiva;

    // Gráficos Reales (Listas de datos para renderizar)
    private List<Map<String, Object>> ocupacionPorMercado;
    private List<Map<String, Object>> recaudacionMensual;
    private List<Map<String, Object>> incidenciasPorEstado;
    private List<Map<String, Object>> obligacionesPorConcepto;

    public DashboardMercadosResponse() {
    }

    public String getPorcentajeOcupacionTexto() {
        if (textoOcupacion != null && !textoOcupacion.isBlank()) {
            return textoOcupacion;
        }
        if (porcentajeOcupacion != null) {
            return String.format(java.util.Locale.US, "%.1f%%", porcentajeOcupacion);
        }
        return "0.0%";
    }

    public long getObligacionesPendientes() { return obligacionesPendientesCount; }
    public BigDecimal getRecaudacionPeriodo() { return recaudacionNetaMonto != null ? recaudacionNetaMonto : BigDecimal.ZERO; }

    public long getTotalMercados() { return totalMercados; }
    public void setTotalMercados(long totalMercados) { this.totalMercados = totalMercados; }

    public long getMercadosActivos() { return mercadosActivos; }
    public void setMercadosActivos(long mercadosActivos) { this.mercadosActivos = mercadosActivos; }

    public long getTotalSectores() { return totalSectores; }
    public void setTotalSectores(long totalSectores) { this.totalSectores = totalSectores; }

    public long getTotalPabellones() { return totalPabellones; }
    public void setTotalPabellones(long totalPabellones) { this.totalPabellones = totalPabellones; }

    public long getTotalPuestos() { return totalPuestos; }
    public void setTotalPuestos(long totalPuestos) { this.totalPuestos = totalPuestos; }

    public long getPuestosOcupados() { return puestosOcupados; }
    public void setPuestosOcupados(long puestosOcupados) { this.puestosOcupados = puestosOcupados; }

    public long getPuestosDisponibles() { return puestosDisponibles; }
    public void setPuestosDisponibles(long puestosDisponibles) { this.puestosDisponibles = puestosDisponibles; }

    public long getPuestosMantenimiento() { return puestosMantenimiento; }
    public void setPuestosMantenimiento(long puestosMantenimiento) { this.puestosMantenimiento = puestosMantenimiento; }

    public long getPuestosRestringidos() { return puestosRestringidos; }
    public void setPuestosRestringidos(long puestosRestringidos) { this.puestosRestringidos = puestosRestringidos; }

    public BigDecimal getPorcentajeOcupacion() { return porcentajeOcupacion; }
    public void setPorcentajeOcupacion(BigDecimal porcentajeOcupacion) { this.porcentajeOcupacion = porcentajeOcupacion; }

    public String getTextoOcupacion() { return textoOcupacion; }
    public void setTextoOcupacion(String textoOcupacion) { this.textoOcupacion = textoOcupacion; }

    public long getTotalComerciantes() { return totalComerciantes; }
    public void setTotalComerciantes(long totalComerciantes) { this.totalComerciantes = totalComerciantes; }

    public long getComerciantesActivos() { return comerciantesActivos; }
    public void setComerciantesActivos(long comerciantesActivos) { this.comerciantesActivos = comerciantesActivos; }

    public long getAsignacionesVigentes() { return asignacionesVigentes; }
    public void setAsignacionesVigentes(long asignacionesVigentes) { this.asignacionesVigentes = asignacionesVigentes; }

    public long getContratosVigentes() { return contratosVigentes; }
    public void setContratosVigentes(long contratosVigentes) { this.contratosVigentes = contratosVigentes; }

    public long getAutorizacionesVigentes() { return autorizacionesVigentes; }
    public void setAutorizacionesVigentes(long autorizacionesVigentes) { this.autorizacionesVigentes = autorizacionesVigentes; }

    public long getInstrumentosPorVencer() { return instrumentosPorVencer; }
    public void setInstrumentosPorVencer(long instrumentosPorVencer) { this.instrumentosPorVencer = instrumentosPorVencer; }

    public BigDecimal getObligacionesEmitidasMonto() { return obligacionesEmitidasMonto; }
    public void setObligacionesEmitidasMonto(BigDecimal obligacionesEmitidasMonto) { this.obligacionesEmitidasMonto = obligacionesEmitidasMonto; }

    public BigDecimal getRecaudacionBrutaMonto() { return recaudacionBrutaMonto; }
    public void setRecaudacionBrutaMonto(BigDecimal recaudacionBrutaMonto) { this.recaudacionBrutaMonto = recaudacionBrutaMonto; }

    public BigDecimal getReversosMonto() { return reversosMonto; }
    public void setReversosMonto(BigDecimal reversosMonto) { this.reversosMonto = reversosMonto; }

    public BigDecimal getRecaudacionNetaMonto() { return recaudacionNetaMonto; }
    public void setRecaudacionNetaMonto(BigDecimal recaudacionNetaMonto) { this.recaudacionNetaMonto = recaudacionNetaMonto; }

    public BigDecimal getDeudaExigibleMonto() { return deudaExigibleMonto; }
    public void setDeudaExigibleMonto(BigDecimal deudaExigibleMonto) { this.deudaExigibleMonto = deudaExigibleMonto; }

    public long getObligacionesPendientesCount() { return obligacionesPendientesCount; }
    public void setObligacionesPendientesCount(long obligacionesPendientesCount) { this.obligacionesPendientesCount = obligacionesPendientesCount; }

    public long getPagosConfirmadosCount() { return pagosConfirmadosCount; }
    public void setPagosConfirmadosCount(long pagosConfirmadosCount) { this.pagosConfirmadosCount = pagosConfirmadosCount; }

    public long getInspeccionesRealizadas() { return inspeccionesRealizadas; }
    public void setInspeccionesRealizadas(long inspeccionesRealizadas) { this.inspeccionesRealizadas = inspeccionesRealizadas; }

    public long getInspeccionesConHallazgos() { return inspeccionesConHallazgos; }
    public void setInspeccionesConHallazgos(long inspeccionesConHallazgos) { this.inspeccionesConHallazgos = inspeccionesConHallazgos; }

    public long getIncidenciasAbiertas() { return incidenciasAbiertas; }
    public void setIncidenciasAbiertas(long incidenciasAbiertas) { this.incidenciasAbiertas = incidenciasAbiertas; }

    public long getIncidenciasCriticas() { return incidenciasCriticas; }
    public void setIncidenciasCriticas(long incidenciasCriticas) { this.incidenciasCriticas = incidenciasCriticas; }

    public long getRenovacionesEnTramite() { return renovacionesEnTramite; }
    public void setRenovacionesEnTramite(long renovacionesEnTramite) { this.renovacionesEnTramite = renovacionesEnTramite; }

    public BigDecimal getRecaudacionPeriodoAnterior() { return recaudacionPeriodoAnterior; }
    public void setRecaudacionPeriodoAnterior(BigDecimal recaudacionPeriodoAnterior) { this.recaudacionPeriodoAnterior = recaudacionPeriodoAnterior; }

    public BigDecimal getVariacionRecaudacionPorcentaje() { return variacionRecaudacionPorcentaje; }
    public void setVariacionRecaudacionPorcentaje(BigDecimal variacionRecaudacionPorcentaje) { this.variacionRecaudacionPorcentaje = variacionRecaudacionPorcentaje; }

    public boolean isTendenciaRecaudacionPositiva() { return tendenciaRecaudacionPositiva; }
    public void setTendenciaRecaudacionPositiva(boolean tendenciaRecaudacionPositiva) { this.tendenciaRecaudacionPositiva = tendenciaRecaudacionPositiva; }

    public List<Map<String, Object>> getOcupacionPorMercado() { return ocupacionPorMercado; }
    public void setOcupacionPorMercado(List<Map<String, Object>> ocupacionPorMercado) { this.ocupacionPorMercado = ocupacionPorMercado; }

    public List<Map<String, Object>> getRecaudacionMensual() { return recaudacionMensual; }
    public void setRecaudacionMensual(List<Map<String, Object>> recaudacionMensual) { this.recaudacionMensual = recaudacionMensual; }

    public List<Map<String, Object>> getIncidenciasPorEstado() { return incidenciasPorEstado; }
    public void setIncidenciasPorEstado(List<Map<String, Object>> incidenciasPorEstado) { this.incidenciasPorEstado = incidenciasPorEstado; }

    public List<Map<String, Object>> getObligacionesPorConcepto() { return obligacionesPorConcepto; }
    public void setObligacionesPorConcepto(List<Map<String, Object>> obligacionesPorConcepto) { this.obligacionesPorConcepto = obligacionesPorConcepto; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final DashboardMercadosResponse r = new DashboardMercadosResponse();

        public Builder totalMercados(long val) { r.totalMercados = val; return this; }
        public Builder mercadosActivos(long val) { r.mercadosActivos = val; return this; }
        public Builder totalSectores(long val) { r.totalSectores = val; return this; }
        public Builder totalPabellones(long val) { r.totalPabellones = val; return this; }
        public Builder totalPuestos(long val) { r.totalPuestos = val; return this; }
        public Builder puestosOcupados(long val) { r.puestosOcupados = val; return this; }
        public Builder puestosDisponibles(long val) { r.puestosDisponibles = val; return this; }
        public Builder puestosMantenimiento(long val) { r.puestosMantenimiento = val; return this; }
        public Builder puestosRestringidos(long val) { r.puestosRestringidos = val; return this; }
        public Builder porcentajeOcupacion(BigDecimal val) { r.porcentajeOcupacion = val; return this; }
        public Builder textoOcupacion(String val) { r.textoOcupacion = val; return this; }
        public Builder totalComerciantes(long val) { r.totalComerciantes = val; return this; }
        public Builder comerciantesActivos(long val) { r.comerciantesActivos = val; return this; }
        public Builder asignacionesVigentes(long val) { r.asignacionesVigentes = val; return this; }
        public Builder contratosVigentes(long val) { r.contratosVigentes = val; return this; }
        public Builder autorizacionesVigentes(long val) { r.autorizacionesVigentes = val; return this; }
        public Builder instrumentosPorVencer(long val) { r.instrumentosPorVencer = val; return this; }
        public Builder obligacionesEmitidasMonto(BigDecimal val) { r.obligacionesEmitidasMonto = val; return this; }
        public Builder recaudacionBrutaMonto(BigDecimal val) { r.recaudacionBrutaMonto = val; return this; }
        public Builder reversosMonto(BigDecimal val) { r.reversosMonto = val; return this; }
        public Builder recaudacionNetaMonto(BigDecimal val) { r.recaudacionNetaMonto = val; return this; }
        public Builder deudaExigibleMonto(BigDecimal val) { r.deudaExigibleMonto = val; return this; }
        public Builder obligacionesPendientesCount(long val) { r.obligacionesPendientesCount = val; return this; }
        public Builder pagosConfirmadosCount(long val) { r.pagosConfirmadosCount = val; return this; }
        public Builder inspeccionesRealizadas(long val) { r.inspeccionesRealizadas = val; return this; }
        public Builder inspeccionesConHallazgos(long val) { r.inspeccionesConHallazgos = val; return this; }
        public Builder incidenciasAbiertas(long val) { r.incidenciasAbiertas = val; return this; }
        public Builder incidenciasCriticas(long val) { r.incidenciasCriticas = val; return this; }
        public Builder renovacionesEnTramite(long val) { r.renovacionesEnTramite = val; return this; }
        public Builder recaudacionPeriodoAnterior(BigDecimal val) { r.recaudacionPeriodoAnterior = val; return this; }
        public Builder variacionRecaudacionPorcentaje(BigDecimal val) { r.variacionRecaudacionPorcentaje = val; return this; }
        public Builder tendenciaRecaudacionPositiva(boolean val) { r.tendenciaRecaudacionPositiva = val; return this; }
        public Builder ocupacionPorMercado(List<Map<String, Object>> val) { r.ocupacionPorMercado = val; return this; }
        public Builder recaudacionMensual(List<Map<String, Object>> val) { r.recaudacionMensual = val; return this; }
        public Builder incidenciasPorEstado(List<Map<String, Object>> val) { r.incidenciasPorEstado = val; return this; }
        public Builder obligacionesPorConcepto(List<Map<String, Object>> val) { r.obligacionesPorConcepto = val; return this; }

        public DashboardMercadosResponse build() {
            return r;
        }
    }
}
