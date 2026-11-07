package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EstructuraMercadoResponse {
    private UUID mercadoId;
    private String mercadoCodigo;
    private String mercadoNombre;
    private String mercadoEstado;
    private EstructuraEstadisticasResponse estadisticas;
    private List<SectorEstructuraNodo> sectores = new ArrayList<>();
    private List<PabellonEstructuraNodo> pabellonesSinSector = new ArrayList<>();
    private List<PuestoResumenNodo> puestosDirectos = new ArrayList<>();
    private List<ZonaResponse> zonas = new ArrayList<>();

    public EstructuraMercadoResponse() {
    }

    public EstructuraMercadoResponse(UUID mercadoId, String mercadoCodigo, String mercadoNombre,
                                    String mercadoEstado, EstructuraEstadisticasResponse estadisticas,
                                    List<SectorEstructuraNodo> sectores,
                                    List<PabellonEstructuraNodo> pabellonesSinSector,
                                    List<PuestoResumenNodo> puestosDirectos,
                                    List<ZonaResponse> zonas) {
        this.mercadoId = mercadoId;
        this.mercadoCodigo = mercadoCodigo;
        this.mercadoNombre = mercadoNombre;
        this.mercadoEstado = mercadoEstado;
        this.estadisticas = estadisticas;
        this.sectores = sectores != null ? sectores : new ArrayList<>();
        this.pabellonesSinSector = pabellonesSinSector != null ? pabellonesSinSector : new ArrayList<>();
        this.puestosDirectos = puestosDirectos != null ? puestosDirectos : new ArrayList<>();
        this.zonas = zonas != null ? zonas : new ArrayList<>();
    }

    public static EstructuraMercadoResponseBuilder builder() {
        return new EstructuraMercadoResponseBuilder();
    }

    public UUID getMercadoId() { return mercadoId; }
    public void setMercadoId(UUID mercadoId) { this.mercadoId = mercadoId; }

    public String getMercadoCodigo() { return mercadoCodigo; }
    public void setMercadoCodigo(String mercadoCodigo) { this.mercadoCodigo = mercadoCodigo; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public String getMercadoEstado() { return mercadoEstado; }
    public void setMercadoEstado(String mercadoEstado) { this.mercadoEstado = mercadoEstado; }

    public EstructuraEstadisticasResponse getEstadisticas() { return estadisticas; }
    public void setEstadisticas(EstructuraEstadisticasResponse estadisticas) { this.estadisticas = estadisticas; }

    public List<SectorEstructuraNodo> getSectores() { return sectores; }
    public void setSectores(List<SectorEstructuraNodo> sectores) { this.sectores = sectores; }

    public List<PabellonEstructuraNodo> getPabellonesSinSector() { return pabellonesSinSector; }
    public void setPabellonesSinSector(List<PabellonEstructuraNodo> pabellonesSinSector) { this.pabellonesSinSector = pabellonesSinSector; }

    public List<PuestoResumenNodo> getPuestosDirectos() { return puestosDirectos; }
    public void setPuestosDirectos(List<PuestoResumenNodo> puestosDirectos) { this.puestosDirectos = puestosDirectos; }

    public List<ZonaResponse> getZonas() { return zonas; }
    public void setZonas(List<ZonaResponse> zonas) { this.zonas = zonas; }

    public static class EstructuraMercadoResponseBuilder {
        private UUID mercadoId;
        private String mercadoCodigo;
        private String mercadoNombre;
        private String mercadoEstado;
        private EstructuraEstadisticasResponse estadisticas;
        private List<SectorEstructuraNodo> sectores = new ArrayList<>();
        private List<PabellonEstructuraNodo> pabellonesSinSector = new ArrayList<>();
        private List<PuestoResumenNodo> puestosDirectos = new ArrayList<>();
        private List<ZonaResponse> zonas = new ArrayList<>();

        public EstructuraMercadoResponseBuilder mercadoId(UUID mercadoId) { this.mercadoId = mercadoId; return this; }
        public EstructuraMercadoResponseBuilder mercadoCodigo(String mercadoCodigo) { this.mercadoCodigo = mercadoCodigo; return this; }
        public EstructuraMercadoResponseBuilder mercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; return this; }
        public EstructuraMercadoResponseBuilder mercadoEstado(String mercadoEstado) { this.mercadoEstado = mercadoEstado; return this; }
        public EstructuraMercadoResponseBuilder estadisticas(EstructuraEstadisticasResponse estadisticas) { this.estadisticas = estadisticas; return this; }
        public EstructuraMercadoResponseBuilder sectores(List<SectorEstructuraNodo> sectores) { this.sectores = sectores; return this; }
        public EstructuraMercadoResponseBuilder pabellonesSinSector(List<PabellonEstructuraNodo> pabellonesSinSector) { this.pabellonesSinSector = pabellonesSinSector; return this; }
        public EstructuraMercadoResponseBuilder puestosDirectos(List<PuestoResumenNodo> puestosDirectos) { this.puestosDirectos = puestosDirectos; return this; }
        public EstructuraMercadoResponseBuilder zonas(List<ZonaResponse> zonas) { this.zonas = zonas; return this; }

        public EstructuraMercadoResponse build() {
            return new EstructuraMercadoResponse(mercadoId, mercadoCodigo, mercadoNombre, mercadoEstado,
                    estadisticas, sectores, pabellonesSinSector, puestosDirectos, zonas);
        }
    }
}
