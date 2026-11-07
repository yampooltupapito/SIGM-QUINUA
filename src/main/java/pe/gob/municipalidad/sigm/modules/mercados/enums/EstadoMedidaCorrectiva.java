package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum EstadoMedidaCorrectiva {
    PENDIENTE("Pendiente de Subsanación"),
    EN_PROCESO("En Proceso de Implementación"),
    PRESENTADA_PARA_VERIFICACION("Subsanación Presentada - En Revisión"),
    SUBSANADA("Subsanada Conforme"),
    NO_SUBSANADA("No Subsanada - Plazo Vencido"),
    CANCELADA("Cancelada por Disposición Administrativa");

    private final String descripcion;

    EstadoMedidaCorrectiva(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
