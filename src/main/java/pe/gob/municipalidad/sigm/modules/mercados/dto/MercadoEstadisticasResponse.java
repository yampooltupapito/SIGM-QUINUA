package pe.gob.municipalidad.sigm.modules.mercados.dto;

public class MercadoEstadisticasResponse {
    private long totalMercados;
    private long mercadosActivos;
    private long mercadosInactivos;
    private long mercadosEnMantenimiento;
    private long mercadosSuspendidos;
    private long totalPuestosAsociados;
    private long totalAsignacionesVigentes;

    public MercadoEstadisticasResponse() {
    }

    public MercadoEstadisticasResponse(long totalMercados, long mercadosActivos, long mercadosInactivos,
                                      long mercadosEnMantenimiento, long mercadosSuspendidos,
                                      long totalPuestosAsociados, long totalAsignacionesVigentes) {
        this.totalMercados = totalMercados;
        this.mercadosActivos = mercadosActivos;
        this.mercadosInactivos = mercadosInactivos;
        this.mercadosEnMantenimiento = mercadosEnMantenimiento;
        this.mercadosSuspendidos = mercadosSuspendidos;
        this.totalPuestosAsociados = totalPuestosAsociados;
        this.totalAsignacionesVigentes = totalAsignacionesVigentes;
    }

    public static MercadoEstadisticasResponseBuilder builder() {
        return new MercadoEstadisticasResponseBuilder();
    }

    public long getTotalMercados() { return totalMercados; }
    public void setTotalMercados(long totalMercados) { this.totalMercados = totalMercados; }

    public long getMercadosActivos() { return mercadosActivos; }
    public void setMercadosActivos(long mercadosActivos) { this.mercadosActivos = mercadosActivos; }

    public long getMercadosInactivos() { return mercadosInactivos; }
    public void setMercadosInactivos(long mercadosInactivos) { this.mercadosInactivos = mercadosInactivos; }

    public long getMercadosEnMantenimiento() { return mercadosEnMantenimiento; }
    public void setMercadosEnMantenimiento(long mercadosEnMantenimiento) { this.mercadosEnMantenimiento = mercadosEnMantenimiento; }

    public long getMercadosSuspendidos() { return mercadosSuspendidos; }
    public void setMercadosSuspendidos(long mercadosSuspendidos) { this.mercadosSuspendidos = mercadosSuspendidos; }

    public long getTotalPuestosAsociados() { return totalPuestosAsociados; }
    public void setTotalPuestosAsociados(long totalPuestosAsociados) { this.totalPuestosAsociados = totalPuestosAsociados; }

    public long getTotalAsignacionesVigentes() { return totalAsignacionesVigentes; }
    public void setTotalAsignacionesVigentes(long totalAsignacionesVigentes) { this.totalAsignacionesVigentes = totalAsignacionesVigentes; }

    public static class MercadoEstadisticasResponseBuilder {
        private long totalMercados;
        private long mercadosActivos;
        private long mercadosInactivos;
        private long mercadosEnMantenimiento;
        private long mercadosSuspendidos;
        private long totalPuestosAsociados;
        private long totalAsignacionesVigentes;

        public MercadoEstadisticasResponseBuilder totalMercados(long totalMercados) { this.totalMercados = totalMercados; return this; }
        public MercadoEstadisticasResponseBuilder mercadosActivos(long mercadosActivos) { this.mercadosActivos = mercadosActivos; return this; }
        public MercadoEstadisticasResponseBuilder mercadosInactivos(long mercadosInactivos) { this.mercadosInactivos = mercadosInactivos; return this; }
        public MercadoEstadisticasResponseBuilder mercadosEnMantenimiento(long mercadosEnMantenimiento) { this.mercadosEnMantenimiento = mercadosEnMantenimiento; return this; }
        public MercadoEstadisticasResponseBuilder mercadosSuspendidos(long mercadosSuspendidos) { this.mercadosSuspendidos = mercadosSuspendidos; return this; }
        public MercadoEstadisticasResponseBuilder totalPuestosAsociados(long totalPuestosAsociados) { this.totalPuestosAsociados = totalPuestosAsociados; return this; }
        public MercadoEstadisticasResponseBuilder totalAsignacionesVigentes(long totalAsignacionesVigentes) { this.totalAsignacionesVigentes = totalAsignacionesVigentes; return this; }

        public MercadoEstadisticasResponse build() {
            return new MercadoEstadisticasResponse(totalMercados, mercadosActivos, mercadosInactivos,
                    mercadosEnMantenimiento, mercadosSuspendidos, totalPuestosAsociados, totalAsignacionesVigentes);
        }
    }
}
