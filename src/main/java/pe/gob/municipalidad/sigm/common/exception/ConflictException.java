package pe.gob.municipalidad.sigm.common.exception;

import lombok.Getter;
import pe.gob.municipalidad.sigm.common.constants.ErrorCodes;

/**
 * Excepción lanzada cuando una operación colisiona con el estado actual de un registro de negocio.
 * Por ejemplo: intentar anular un expediente municipal que ya fue resuelto y archivado.
 */
@Getter
public class ConflictException extends RuntimeException {

    private final String errorCode;

    public ConflictException(String message) {
        super(message);
        this.errorCode = ErrorCodes.ERR_CONFLICT;
    }

    public ConflictException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
