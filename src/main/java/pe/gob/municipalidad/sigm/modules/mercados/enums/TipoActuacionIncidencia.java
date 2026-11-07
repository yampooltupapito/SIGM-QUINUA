package pe.gob.municipalidad.sigm.modules.mercados.enums;

import lombok.Getter;

@Getter
public enum TipoActuacionIncidencia {
    REGISTRO("Registro Inicial", "Ingreso del reporte al sistema institucional"),
    ASIGNACION("Asignación de Responsable", "Designación de funcionario u oficina competente"),
    REVISION("Revisión Técnica", "Evaluación de antecedentes y procedencia"),
    INSPECCION_SOLICITADA("Inspección Solicitada", "Requerimiento de visita in situ al equipo fiscalizador"),
    DILIGENCIA_CAMPO("Diligencia de Campo", "Constitución in situ y constatación de hechos"),
    COMUNICACION("Comunicación / Notificación", "Aviso informativo al administrado o administradores"),
    SUBSANACION_OPERATIVA("Subsanación Operativa", "Acción correctiva material realizada (limpieza, reparación)"),
    RESOLUCION("Resolución Operativa", "Determinación motivada sobre la atención de la incidencia"),
    CIERRE("Cierre Administrativo", "Finalización formal del trámite de incidencia"),
    DESCARTE("Descarte Sustentado", "Desestimación por improcedencia justificada");

    private final String label;
    private final String descripcion;

    TipoActuacionIncidencia(String label, String descripcion) {
        this.label = label;
        this.descripcion = descripcion;
    }
}
