package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Categorías de reportes e inteligencia tributaria municipal.
 */
@Getter
@RequiredArgsConstructor
public enum CategoriaReporteTributario {
    RECAUDACION("Recaudación y Pagos", "Ingresos confirmados, medios de pago y evolución cronológica"),
    OBLIGACIONES("Obligaciones y Emisión", "Emisión de tributos, cuotas y conceptos anuales"),
    DEUDAS("Cartera y Saldos", "Saldos pendientes, deudas corrientes y exigibles"),
    MOROSIDAD("Morosidad y Antigüedad", "Tasas de morosidad, recuperación y tramos de deuda"),
    CONTRIBUYENTES("Contribuyentes y Padrón", "Universo de personas naturales, jurídicas y cumplimiento"),
    PREDIOS("Predios y Catastro", "Distribución de predios urbanos, rústicos y autovalúos"),
    CAJA("Cajas y Tesorería", "Sesiones de caja, recaudadores y arqueos municipales"),
    FRACCIONAMIENTOS("Fraccionamientos", "Convenios suscritos, cuotas amortizadas e incumplimientos"),
    NOTIFICACIONES("Notificaciones y Actos", "Diligenciamiento de actos, medios y validez legal"),
    DOCUMENTOS("Documentos y Recibos", "Recibos oficiales, constancias y valores emitidos"),
    INDICADORES("Indicadores y Metas", "Cumplimiento presupuestal y metas de recaudación");

    private final String nombre;
    private final String descripcion;
}

