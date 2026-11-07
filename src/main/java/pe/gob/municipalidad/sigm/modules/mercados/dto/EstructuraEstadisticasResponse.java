package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.util.UUID;

public class EstructuraEstadisticasResponse {
    private UUID mercadoId;
    private String mercadoNombre;
    private long totalSectores;
    private long sectoresActivos;
    private long sectoresInactivos;
    private long totalPabellones;
    private long pabellonesActivos;
    private long pabellonesInactivos;
    private long totalZonas;
    private long zonasActivas;
    private long zonasAsignables;
    private long zonasComunes;
    private long totalPuestos;
    private long puestosDisponibles;
    private long puestosOcupados;
    private long puestosReservados;
    private long puestosMantenimiento;

    public EstructuraEstadisticasResponse() {
    }

    public EstructuraEstadisticasResponse(UUID mercadoId, String mercadoNombre, long totalSectores,
                                         long sectoresActivos, long sectoresInactivos, long totalPabellones,
                                         long pabellonesActivos, long pabellonesInactivos, long totalZonas,
                                         long zonasActivas, long zonasAsignables, long zonasComunes,
                                         long totalPuestos, long puestosDisponibles, long puestosOcupados,
                                         long puestosReservados, long puestosMantenimiento) {
        this.mercadoId = mercadoId;
        this.mercadoNombre = mercadoNombre;
        this.totalSectores = totalSectores;
        this.sectoresActivos = sectoresActivos;
        this.sectoresInactivos = sectoresInactivos;
        this.totalPabellones = totalPabellones;
        this.pabellonesActivos = pabellonesActivos;
        this.pabellonesInactivos = pabellonesInactivos;
        this.totalZonas = totalZonas;
        this.zonasActivas = zonasActivas;
        this.zonasAsignables = zonasAsignables;
        this.zonasComunes = zonasComunes;
        this.totalPuestos = totalPuestos;
        this.puestosDisponibles = puestosDisponibles;
        this.puestosOcupados = puestosOcupados;
        this.puestosReservados = puestosReservados;
        this.puestosMantenimiento = puestosMantenimiento;
    }

    public static EstructuraEstadisticasResponseBuilder builder() {
        return new EstructuraEstadisticasResponseBuilder();
    }

    public UUID getMercadoId() { return mercadoId; }
    public void setMercadoId(UUID mercadoId) { this.mercadoId = mercadoId; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public long getTotalSectores() { return totalSectores; }
    public void setTotalSectores(long totalSectores) { this.totalSectores = totalSectores; }

    public long getSectoresActivos() { return sectoresActivos; }
    public void setSectoresActivos(long sectoresActivos) { this.sectoresActivos = sectoresActivos; }

    public long getSectoresInactivos() { return sectoresInactivos; }
    public void setSectoresInactivos(long sectoresInactivos) { this.sectoresInactivos = sectoresInactivos; }

    public long getTotalPabellones() { return totalPabellones; }
    public void setTotalPabellones(long totalPabellones) { this.totalPabellones = totalPabellones; }

    public long getPabellonesActivos() { return pabellonesActivos; }
    public void setPabellonesActivos(long pabellonesActivos) { this.pabellonesActivos = pabellonesActivos; }

    public long getPabellonesInactivos() { return pabellonesInactivos; }
    public void setPabellonesInactivos(long pabellonesInactivos) { this.pabellonesInactivos = pabellonesInactivos; }

    public long getTotalZonas() { return totalZonas; }
    public void setTotalZonas(long totalZonas) { this.totalZonas = totalZonas; }

    public long getZonasActivas() { return zonasActivas; }
    public void setZonasActivas(long zonasActivas) { this.zonasActivas = zonasActivas; }

    public long getZonasAsignables() { return zonasAsignables; }
    public void setZonasAsignables(long zonasAsignables) { this.zonasAsignables = zonasAsignables; }

    public long getZonasComunes() { return zonasComunes; }
    public void setZonasComunes(long zonasComunes) { this.zonasComunes = zonasComunes; }

    public long getTotalPuestos() { return totalPuestos; }
    public void setTotalPuestos(long totalPuestos) { this.totalPuestos = totalPuestos; }

    public long getPuestosDisponibles() { return puestosDisponibles; }
    public void setPuestosDisponibles(long puestosDisponibles) { this.puestosDisponibles = puestosDisponibles; }

    public long getPuestosOcupados() { return puestosOcupados; }
    public void setPuestosOcupados(long puestosOcupados) { this.puestosOcupados = puestosOcupados; }

    public long getPuestosReservados() { return puestosReservados; }
    public void setPuestosReservados(long puestosReservados) { this.puestosReservados = puestosReservados; }

    public long getPuestosMantenimiento() { return puestosMantenimiento; }
    public void setPuestosMantenimiento(long puestosMantenimiento) { this.puestosMantenimiento = puestosMantenimiento; }

    public static class EstructuraEstadisticasResponseBuilder {
        private UUID mercadoId;
        private String mercadoNombre;
        private long totalSectores;
        private long sectoresActivos;
        private long sectoresInactivos;
        private long totalPabellones;
        private long pabellonesActivos;
        private long pabellonesInactivos;
        private long totalZonas;
        private long zonasActivas;
        private long zonasAsignables;
        private long zonasComunes;
        private long totalPuestos;
        private long puestosDisponibles;
        private long puestosOcupados;
        private long puestosReservados;
        private long puestosMantenimiento;

        public EstructuraEstadisticasResponseBuilder mercadoId(UUID mercadoId) { this.mercadoId = mercadoId; return this; }
        public EstructuraEstadisticasResponseBuilder mercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; return this; }
        public EstructuraEstadisticasResponseBuilder totalSectores(long totalSectores) { this.totalSectores = totalSectores; return this; }
        public EstructuraEstadisticasResponseBuilder sectoresActivos(long sectoresActivos) { this.sectoresActivos = sectoresActivos; return this; }
        public EstructuraEstadisticasResponseBuilder sectoresInactivos(long sectoresInactivos) { this.sectoresInactivos = sectoresInactivos; return this; }
        public EstructuraEstadisticasResponseBuilder totalPabellones(long totalPabellones) { this.totalPabellones = totalPabellones; return this; }
        public EstructuraEstadisticasResponseBuilder pabellonesActivos(long pabellonesActivos) { this.pabellonesActivos = pabellonesActivos; return this; }
        public EstructuraEstadisticasResponseBuilder pabellonesInactivos(long pabellonesInactivos) { this.pabellonesInactivos = pabellonesInactivos; return this; }
        public EstructuraEstadisticasResponseBuilder totalZonas(long totalZonas) { this.totalZonas = totalZonas; return this; }
        public EstructuraEstadisticasResponseBuilder zonasActivas(long zonasActivas) { this.zonasActivas = zonasActivas; return this; }
        public EstructuraEstadisticasResponseBuilder zonasAsignables(long zonasAsignables) { this.zonasAsignables = zonasAsignables; return this; }
        public EstructuraEstadisticasResponseBuilder zonasComunes(long zonasComunes) { this.zonasComunes = zonasComunes; return this; }
        public EstructuraEstadisticasResponseBuilder totalPuestos(long totalPuestos) { this.totalPuestos = totalPuestos; return this; }
        public EstructuraEstadisticasResponseBuilder puestosDisponibles(long puestosDisponibles) { this.puestosDisponibles = puestosDisponibles; return this; }
        public EstructuraEstadisticasResponseBuilder puestosOcupados(long puestosOcupados) { this.puestosOcupados = puestosOcupados; return this; }
        public EstructuraEstadisticasResponseBuilder puestosReservados(long puestosReservados) { this.puestosReservados = puestosReservados; return this; }
        public EstructuraEstadisticasResponseBuilder puestosMantenimiento(long puestosMantenimiento) { this.puestosMantenimiento = puestosMantenimiento; return this; }

        public EstructuraEstadisticasResponse build() {
            return new EstructuraEstadisticasResponse(mercadoId, mercadoNombre, totalSectores, sectoresActivos,
                    sectoresInactivos, totalPabellones, pabellonesActivos, pabellonesInactivos, totalZonas,
                    zonasActivas, zonasAsignables, zonasComunes, totalPuestos, puestosDisponibles,
                    puestosOcupados, puestosReservados, puestosMantenimiento);
        }
    }
}
