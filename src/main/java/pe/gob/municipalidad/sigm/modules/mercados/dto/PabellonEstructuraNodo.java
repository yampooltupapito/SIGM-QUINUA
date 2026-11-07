package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.util.ArrayList;
import java.util.List;

public class PabellonEstructuraNodo {
    private PabellonResponse pabellon;
    private List<PuestoResumenNodo> puestos = new ArrayList<>();

    public PabellonEstructuraNodo() {
    }

    public PabellonEstructuraNodo(PabellonResponse pabellon, List<PuestoResumenNodo> puestos) {
        this.pabellon = pabellon;
        this.puestos = puestos != null ? puestos : new ArrayList<>();
    }

    public static PabellonEstructuraNodoBuilder builder() {
        return new PabellonEstructuraNodoBuilder();
    }

    public PabellonResponse getPabellon() { return pabellon; }
    public void setPabellon(PabellonResponse pabellon) { this.pabellon = pabellon; }

    public List<PuestoResumenNodo> getPuestos() { return puestos; }
    public void setPuestos(List<PuestoResumenNodo> puestos) { this.puestos = puestos; }

    public static class PabellonEstructuraNodoBuilder {
        private PabellonResponse pabellon;
        private List<PuestoResumenNodo> puestos = new ArrayList<>();

        public PabellonEstructuraNodoBuilder pabellon(PabellonResponse pabellon) { this.pabellon = pabellon; return this; }
        public PabellonEstructuraNodoBuilder puestos(List<PuestoResumenNodo> puestos) { this.puestos = puestos; return this; }

        public PabellonEstructuraNodo build() {
            return new PabellonEstructuraNodo(pabellon, puestos);
        }
    }
}
