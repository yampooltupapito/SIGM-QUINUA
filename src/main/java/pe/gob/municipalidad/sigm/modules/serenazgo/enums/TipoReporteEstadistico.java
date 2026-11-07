package pe.gob.municipalidad.sigm.modules.serenazgo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Catálogo de tipos de reportes estadísticos e institucionales
 * del subsistema de Seguridad Ciudadana y Serenazgo de Quinua.
 */
@Getter
@RequiredArgsConstructor
public enum TipoReporteEstadistico {

    GENERAL_SEGURIDAD(
            "Reporte General de Seguridad Ciudadana",
            "Consolidado ejecutivo de incidencias, alertas, patrullajes, intervenciones y recursos.",
            "AGREGADO"),

    INCIDENCIAS(
            "Reporte Estadístico de Incidencias",
            "Análisis detallado de tipologías, prioridades, sectores y estados de incidencias.",
            "OPERATIVO"),

    ALERTAS_CIUDADANAS(
            "Reporte de Alertas y Reportes Vecinales",
            "Comunicaciones vecinales, canales de ingreso, tiempos de verificación y anonimización.",
            "CIUDADANO"),

    ATENCIONES_OPERATIVAS(
            "Reporte de Atenciones y Tiempos de Respuesta",
            "Métricas cronológicas de asignación, despacho, aceptación y duración de atenciones.",
            "DESEMPENO"),

    INTERVENCIONES(
            "Reporte de Intervenciones de Serenazgo",
            "Actuaciones de apoyo, actas levantadas, validaciones y resultados administrativos.",
            "OPERATIVO"),

    PATRULLAJES_RECORRIDOS(
            "Reporte de Patrullajes y Cumplimiento de Rutas",
            "Patrullajes programados vs ejecutados, puntos de control visitados y distancias estimadas.",
            "OPERATIVO"),

    PERSONAL_TURNOS(
            "Reporte de Personal y Turnos de Servicio",
            "Programación de turnos, servicios asignados, asistencia y personal habilitado.",
            "RECURSOS"),

    UNIDADES_EQUIPAMIENTO(
            "Reporte de Unidades Móviles y Equipamiento",
            "Estado de flota vehicular, disponibilidad operativa y asignación a patrullajes.",
            "RECURSOS"),

    SUPERVISION_OPERATIVA(
            "Reporte de Supervisión y Control Administrativo",
            "Expedientes de supervisión, observaciones registradas, subsanaciones y cierres.",
            "SUPERVISION"),

    GEOGRAFICO_SECTORES(
            "Reporte Territorial por Sectores y Zonas",
            "Distribución espacial de novedades y densidad operativa en el distrito de Quinua.",
            "GEOGRAFICO");

    private final String titulo;
    private final String descripcion;
    private final String categoria;
}
