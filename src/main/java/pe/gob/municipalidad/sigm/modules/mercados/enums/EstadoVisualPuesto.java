package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum EstadoVisualPuesto {
    DISPONIBLE("Disponible", "#10b981", "emerald"),
    OCUPADO("Ocupado", "#3b82f6", "blue"),
    RESERVADO("Reservado / En Trámite", "#f59e0b", "amber"),
    INACTIVO("Inactivo / Mantenimiento", "#64748b", "slate"),
    RESTRINGIDO("Restringido / Clausurado", "#ef4444", "rose");

    private final String label;
    private final String colorHex;
    private final String colorTailwind;

    EstadoVisualPuesto(String label, String colorHex, String colorTailwind) {
        this.label = label;
        this.colorHex = colorHex;
        this.colorTailwind = colorTailwind;
    }
}
