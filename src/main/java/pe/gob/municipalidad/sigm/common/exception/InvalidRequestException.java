package pe.gob.municipalidad.sigm.common.exception;

import lombok.Getter;
import pe.gob.municipalidad.sigm.common.constants.ErrorCodes;

/**
 * Excepción lanzada cuando una solicitud HTTP o parámetro de entrada es semánticamente incorrecto o no permitido.
 */
@Getter
public class InvalidRequestException extends RuntimeException {

    private final String errorCode;

    public InvalidRequestException(String message) {
        super(message);
        this.errorCode = ErrorCodes.ERR_INVALID_REQUEST;
    }

    public InvalidRequestException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
