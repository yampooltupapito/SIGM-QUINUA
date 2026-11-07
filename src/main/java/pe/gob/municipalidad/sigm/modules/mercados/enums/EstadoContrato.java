package pe.gob.municipalidad.sigm.modules.mercados.enums;

/**
 * Estados del ciclo de vida administrativo de un contrato de ocupación de mercado.
 */
public enum EstadoContrato {
    BORRADOR,
    EN_REVISION,
    OBSERVADO,
    APROBADO,
    PENDIENTE_FORMALIZACION,
    VIGENTE,
    SUSPENDIDO,
    VENCIDO,
    TERMINADO,
    ANULADO;

    public boolean isEditable() {
        return this == BORRADOR || this == OBSERVADO;
    }

    public boolean isActivo() {
        return this == VIGENTE;
    }

    public boolean canTransitionTo(EstadoContrato nuevo) {
        if (this == nuevo) return true;
        switch (this) {
            case BORRADOR:
                return nuevo == EN_REVISION || nuevo == ANULADO;
            case EN_REVISION:
                return nuevo == OBSERVADO || nuevo == APROBADO || nuevo == ANULADO;
            case OBSERVADO:
                return nuevo == EN_REVISION || nuevo == ANULADO;
            case APROBADO:
                return nuevo == PENDIENTE_FORMALIZACION || nuevo == VIGENTE || nuevo == ANULADO;
            case PENDIENTE_FORMALIZACION:
                return nuevo == VIGENTE || nuevo == ANULADO;
            case VIGENTE:
                return nuevo == SUSPENDIDO || nuevo == VENCIDO || nuevo == TERMINADO || nuevo == ANULADO;
            case SUSPENDIDO:
                return nuevo == VIGENTE || nuevo == TERMINADO || nuevo == ANULADO;
            case VENCIDO:
                return nuevo == TERMINADO || nuevo == VIGENTE; // renovación
            case TERMINADO:
            case ANULADO:
            default:
                return false;
        }
    }
}
