package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados del procesamiento de un reporte tributario exportado.
 */
@Getter
@RequiredArgsConstructor
public enum EstadoExportacionReporte {
    PENDIENTE("En Generación", "bg-amber-100 text-amber-800 border-amber-300"),
    COMPLETADO("Disponible para Descarga", "bg-emerald-100 text-emerald-800 border-emerald-300"),
    FALLIDO("Error al Generar", "bg-rose-100 text-rose-800 border-rose-300");

    private final String etiqueta;
    private final String badgeClass;
}

