package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

public enum AccionHistorialIntervencion {
    CREACION("Creación de Intervención"),
    EDICION("Modificación de Datos Generales"),
    ENVIO_REVISION("Envío a Revisión de Supervisor"),
    OBSERVACION("Registro Observado por Inconsistencias"),
    SUBSANACION("Subsanación de Observaciones"),
    VALIDACION("Validación Administrativa Aprobada"),
    ARCHIVADO("Archivado Administrativo"),
    AGREGAR_PARTICIPANTE("Incorporación de Personal Participante"),
    REMOVER_PARTICIPANTE("Desvinculación de Participante"),
    REGISTRAR_ACTUACION("Registro de Actuación Operativa"),
    REGISTRAR_COMUNICACION("Registro de Comunicación Institucional"),
    ACTUALIZAR_COMUNICACION("Actualización de Estado de Comunicación"),
    ASIGNAR_EQUIPAMIENTO("Asignación de Equipamiento Utilizado"),
    ADJUNTAR_DOCUMENTO("Anexo de Documento / Acta");

    private final String descripcion;

    AccionHistorialIntervencion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
