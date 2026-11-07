package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum FuncionOperativaPersonal {
    JEFE_GRUPO("Jefe / Responsable de Grupo"),
    CONDUCTOR("Conductor de Unidad Móvil"),
    PATRULLERO("Patrullero / Agente Operativo"),
    OPERADOR_RADIO("Operador de Radiocomunicaciones"),
    APOYO("Efectivo de Apoyo");

    private final String descripcion;

    FuncionOperativaPersonal(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
