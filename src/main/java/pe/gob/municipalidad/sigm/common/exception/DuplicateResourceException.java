package pe.gob.municipalidad.sigm.common.exception;

import lombok.Getter;
import pe.gob.municipalidad.sigm.common.constants.ErrorCodes;

/**
 * Excepción lanzada cuando se intenta crear o duplicar un registro que ya existe de manera unívoca en el sistema.
 */
@Getter
public class DuplicateResourceException extends RuntimeException {

    private final String errorCode;
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s ya existe registrado con %s : '%s'", resourceName, fieldName, fieldValue));
        this.errorCode = ErrorCodes.ERR_DUPLICATE_RESOURCE;
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public DuplicateResourceException(String message) {
        super(message);
        this.errorCode = ErrorCodes.ERR_DUPLICATE_RESOURCE;
        this.resourceName = "Recurso";
        this.fieldName = "identificador";
        this.fieldValue = null;
    }
}
