package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums;

/**
 * Eventos auditables en el ciclo de vida de un documento de pago tributario.
 */
public enum TipoEventoDocumentoPago {
    EMISION,
    REIMPRESION,
    DESCARGA,
    VERIFICACION_PUBLICA,
    ANULACION,
    SUSTITUCION,
    FALLO_INTEGRIDAD
}
