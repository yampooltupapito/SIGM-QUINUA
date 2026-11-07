package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.util.ArrayList;
import java.util.List;

public class SectorEstructuraNodo {
    private SectorResponse sector;
    private List<PabellonEstructuraNodo> pabellones = new ArrayList<>();
    private List<PuestoResumenNodo> puestosDirectos = new ArrayList<>();

    public SectorEstructuraNodo() {
    }

    public SectorEstructuraNodo(SectorResponse sector, List<PabellonEstructuraNodo> pabellones, List<PuestoResumenNodo> puestosDirectos) {
        this.sector = sector;
        this.pabellones = pabellones != null ? pabellones : new ArrayList<>();
        this.puestosDirectos = puestosDirectos != null ? puestosDirectos : new ArrayList<>();
    }

    public static SectorEstructuraNodoBuilder builder() {
        return new SectorEstructuraNodoBuilder();
    }

    public SectorResponse getSector() { return sector; }
    public void setSector(SectorResponse sector) { this.sector = sector; }

    public List<PabellonEstructuraNodo> getPabellones() { return pabellones; }
    public void setPabellones(List<PabellonEstructuraNodo> pabellones) { this.pabellones = pabellones; }

    public List<PuestoResumenNodo> getPuestosDirectos() { return puestosDirectos; }
    public void setPuestosDirectos(List<PuestoResumenNodo> puestosDirectos) { this.puestosDirectos = puestosDirectos; }

    public static class SectorEstructuraNodoBuilder {
        private SectorResponse sector;
        private List<PabellonEstructuraNodo> pabellones = new ArrayList<>();
        private List<PuestoResumenNodo> puestosDirectos = new ArrayList<>();

        public SectorEstructuraNodoBuilder sector(SectorResponse sector) { this.sector = sector; return this; }
        public SectorEstructuraNodoBuilder pabellones(List<PabellonEstructuraNodo> pabellones) { this.pabellones = pabellones; return this; }
        public SectorEstructuraNodoBuilder puestosDirectos(List<PuestoResumenNodo> puestosDirectos) { this.puestosDirectos = puestosDirectos; return this; }

        public SectorEstructuraNodo build() {
            return new SectorEstructuraNodo(sector, pabellones, puestosDirectos);
        }
    }
}
