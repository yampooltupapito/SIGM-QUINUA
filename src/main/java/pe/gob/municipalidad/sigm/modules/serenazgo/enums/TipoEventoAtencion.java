package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum TipoEventoAtencion {
    CREACION_ATENCION("Atención Registrada"),
    ASIGNACION_RECURSOS("Personal y Recursos Asignados"),
    DESPACHO_OPERATIVO("Despacho Emitido por la Central"),
    ACEPTACION_UNIDAD("Asignación Aceptada por la Unidad"),
    RECHAZO_UNIDAD("Asignación Rechazada con Justificación"),
    INICIO_ATENCION("Llegada al Sitio e Inicio de Actuaciones"),
    REGISTRO_NOVEDAD("Novedad Operativa en Sitio"),
    SOLICITUD_APOYO("Solicitud de Apoyo Operativo Adicional"),
    PAUSA_EN_ESPERA("Atención Puesta en Espera"),
    REANUDACION("Reanudación de la Atención"),
    REASIGNACION_PERSONAL("Reasignación de Personal"),
    REASIGNACION_UNIDAD("Reasignación de Unidad Móvil"),
    CAMBIO_RESPONSABLE("Cambio de Responsable Operativo"),
    ESCALAMIENTO_INSTITUCIONAL("Escalamiento a Autoridad Externa"),
    FINALIZACION("Finalización Operativa y Cierre de Atención"),
    CANCELACION("Cancelación Administrativa");

    private final String descripcion;

    TipoEventoAtencion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEtiqueta() {
        return descripcion;
    }
}
