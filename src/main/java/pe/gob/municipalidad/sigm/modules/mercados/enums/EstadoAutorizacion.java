package pe.gob.municipalidad.sigm.modules.mercados.enums;

/**
 * Estados legales y administrativos de una autorización comercial en mercado.
 */
public enum EstadoAutorizacion {
    BORRADOR,
    EN_EVALUACION,
    OBSERVADA,
    APROBADA,
    EMITIDA,
    VIGENTE,
    SUSPENDIDA,
    VENCIDA,
    REVOCADA,
    ANULADA;

    public boolean isEditable() {
        return this == BORRADOR || this == OBSERVADA;
    }

    public boolean isActivo() {
        return this == VIGENTE || this == EMITIDA;
    }

    public boolean canTransitionTo(EstadoAutorizacion nuevo) {
        if (this == nuevo) return true;
        switch (this) {
            case BORRADOR:
                return nuevo == EN_EVALUACION || nuevo == ANULADA;
            case EN_EVALUACION:
                return nuevo == OBSERVADA || nuevo == APROBADA || nuevo == ANULADA;
            case OBSERVADA:
                return nuevo == EN_EVALUACION || nuevo == ANULADA;
            case APROBADA:
                return nuevo == EMITIDA || nuevo == VIGENTE || nuevo == ANULADA;
            case EMITIDA:
                return nuevo == VIGENTE || nuevo == SUSPENDIDA || nuevo == REVOCADA || nuevo == ANULADA;
            case VIGENTE:
                return nuevo == SUSPENDIDA || nuevo == VENCIDA || nuevo == REVOCADA || nuevo == ANULADA;
            case SUSPENDIDA:
                return nuevo == VIGENTE || nuevo == REVOCADA || nuevo == ANULADA;
            case VENCIDA:
                return nuevo == VIGENTE || nuevo == REVOCADA || nuevo == ANULADA;
            case REVOCADA:
            case ANULADA:
            default:
                return false;
        }
    }
}
