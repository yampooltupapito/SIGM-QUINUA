package pe.gob.municipalidad.sigm.common.exception;

import lombok.Getter;

/**
 * Excepción para violaciones de reglas de negocio en SIGM-QUINUA.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
