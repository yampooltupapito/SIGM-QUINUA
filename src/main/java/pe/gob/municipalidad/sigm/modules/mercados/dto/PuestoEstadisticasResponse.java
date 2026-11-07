package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PuestoEstadisticasResponse {

    private UUID mercadoId;
    private String mercadoNombre;
    private long totalPuestos;
    private long puestosDisponibles;
    private long puestosOcupados;
    private long puestosReservados;
    private long puestosMantenimiento;
    private long puestosInhabilitados;
    private long puestosNoAsignables;
    private List<SectorEstadisticaItem> porSector = new ArrayList<>();
    private List<TipoPuestoEstadisticaItem> porTipo = new ArrayList<>();

    public PuestoEstadisticasResponse() {
    }

    public PuestoEstadisticasResponse(UUID mercadoId, String mercadoNombre, long totalPuestos,
                                      long puestosDisponibles, long puestosOcupados, long puestosReservados,
                                      long puestosMantenimiento, long puestosInhabilitados, long puestosNoAsignables,
                                      List<SectorEstadisticaItem> porSector, List<TipoPuestoEstadisticaItem> porTipo) {
        this.mercadoId = mercadoId;
        this.mercadoNombre = mercadoNombre;
        this.totalPuestos = totalPuestos;
        this.puestosDisponibles = puestosDisponibles;
        this.puestosOcupados = puestosOcupados;
        this.puestosReservados = puestosReservados;
        this.puestosMantenimiento = puestosMantenimiento;
        this.puestosInhabilitados = puestosInhabilitados;
        this.puestosNoAsignables = puestosNoAsignables;
        this.porSector = porSector != null ? porSector : new ArrayList<>();
        this.porTipo = porTipo != null ? porTipo : new ArrayList<>();
    }

    public static PuestoEstadisticasResponseBuilder builder() {
        return new PuestoEstadisticasResponseBuilder();
    }

    public UUID getMercadoId() { return mercadoId; }
    public void setMercadoId(UUID mercadoId) { this.mercadoId = mercadoId; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

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

    public long getPuestosInhabilitados() { return puestosInhabilitados; }
    public void setPuestosInhabilitados(long puestosInhabilitados) { this.puestosInhabilitados = puestosInhabilitados; }

    public long getPuestosNoAsignables() { return puestosNoAsignables; }
    public void setPuestosNoAsignables(long puestosNoAsignables) { this.puestosNoAsignables = puestosNoAsignables; }

    public List<SectorEstadisticaItem> getPorSector() { return porSector; }
    public void setPorSector(List<SectorEstadisticaItem> porSector) { this.porSector = porSector; }

    public List<TipoPuestoEstadisticaItem> getPorTipo() { return porTipo; }
    public void setPorTipo(List<TipoPuestoEstadisticaItem> porTipo) { this.porTipo = porTipo; }

    public static class SectorEstadisticaItem {
        private UUID sectorId;
        private String sectorNombre;
        private long total;
        private long disponibles;
        private long ocupados;

        public SectorEstadisticaItem() {}
        public SectorEstadisticaItem(UUID sectorId, String sectorNombre, long total, long disponibles, long ocupados) {
            this.sectorId = sectorId;
            this.sectorNombre = sectorNombre;
            this.total = total;
            this.disponibles = disponibles;
            this.ocupados = ocupados;
        }

        public UUID getSectorId() { return sectorId; }
        public void setSectorId(UUID sectorId) { this.sectorId = sectorId; }
        public String getSectorNombre() { return sectorNombre; }
        public void setSectorNombre(String sectorNombre) { this.sectorNombre = sectorNombre; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getDisponibles() { return disponibles; }
        public void setDisponibles(long disponibles) { this.disponibles = disponibles; }
        public long getOcupados() { return ocupados; }
        public void setOcupados(long ocupados) { this.ocupados = ocupados; }
    }

    public static class TipoPuestoEstadisticaItem {
        private Long tipoPuestoId;
        private String tipoPuestoNombre;
        private long cantidad;
        private double porcentaje;

        public TipoPuestoEstadisticaItem() {}
        public TipoPuestoEstadisticaItem(Long tipoPuestoId, String tipoPuestoNombre, long cantidad, double porcentaje) {
            this.tipoPuestoId = tipoPuestoId;
            this.tipoPuestoNombre = tipoPuestoNombre;
            this.cantidad = cantidad;
            this.porcentaje = porcentaje;
        }

        public Long getTipoPuestoId() { return tipoPuestoId; }
        public void setTipoPuestoId(Long tipoPuestoId) { this.tipoPuestoId = tipoPuestoId; }
        public String getTipoPuestoNombre() { return tipoPuestoNombre; }
        public void setTipoPuestoNombre(String tipoPuestoNombre) { this.tipoPuestoNombre = tipoPuestoNombre; }
        public long getCantidad() { return cantidad; }
        public void setCantidad(long cantidad) { this.cantidad = cantidad; }
        public double getPorcentaje() { return porcentaje; }
        public void setPorcentaje(double porcentaje) { this.porcentaje = porcentaje; }
    }

    public static class PuestoEstadisticasResponseBuilder {
        private UUID mercadoId;
        private String mercadoNombre;
        private long totalPuestos;
        private long puestosDisponibles;
        private long puestosOcupados;
        private long puestosReservados;
        private long puestosMantenimiento;
        private long puestosInhabilitados;
        private long puestosNoAsignables;
        private List<SectorEstadisticaItem> porSector = new ArrayList<>();
        private List<TipoPuestoEstadisticaItem> porTipo = new ArrayList<>();

        public PuestoEstadisticasResponseBuilder mercadoId(UUID mercadoId) { this.mercadoId = mercadoId; return this; }
        public PuestoEstadisticasResponseBuilder mercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; return this; }
        public PuestoEstadisticasResponseBuilder totalPuestos(long totalPuestos) { this.totalPuestos = totalPuestos; return this; }
        public PuestoEstadisticasResponseBuilder puestosDisponibles(long puestosDisponibles) { this.puestosDisponibles = puestosDisponibles; return this; }
        public PuestoEstadisticasResponseBuilder puestosOcupados(long puestosOcupados) { this.puestosOcupados = puestosOcupados; return this; }
        public PuestoEstadisticasResponseBuilder puestosReservados(long puestosReservados) { this.puestosReservados = puestosReservados; return this; }
        public PuestoEstadisticasResponseBuilder puestosMantenimiento(long puestosMantenimiento) { this.puestosMantenimiento = puestosMantenimiento; return this; }
        public PuestoEstadisticasResponseBuilder puestosInhabilitados(long puestosInhabilitados) { this.puestosInhabilitados = puestosInhabilitados; return this; }
        public PuestoEstadisticasResponseBuilder puestosNoAsignables(long puestosNoAsignables) { this.puestosNoAsignables = puestosNoAsignables; return this; }
        public PuestoEstadisticasResponseBuilder porSector(List<SectorEstadisticaItem> porSector) { this.porSector = porSector; return this; }
        public PuestoEstadisticasResponseBuilder porTipo(List<TipoPuestoEstadisticaItem> porTipo) { this.porTipo = porTipo; return this; }

        public PuestoEstadisticasResponse build() {
            return new PuestoEstadisticasResponse(mercadoId, mercadoNombre, totalPuestos, puestosDisponibles,
                    puestosOcupados, puestosReservados, puestosMantenimiento, puestosInhabilitados, puestosNoAsignables,
                    porSector, porTipo);
        }
    }
}
