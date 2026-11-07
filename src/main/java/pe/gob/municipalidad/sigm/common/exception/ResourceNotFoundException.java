package pe.gob.municipalidad.sigm.common.exception;

import lombok.Getter;
import pe.gob.municipalidad.sigm.common.constants.ErrorCodes;

/**
 * Excepción lanzada cuando una entidad o recurso no es encontrado en la base de datos.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String errorCode;
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s no encontrado(a) con %s : '%s'", resourceName, fieldName, fieldValue));
        this.errorCode = ErrorCodes.ERR_RESOURCE_NOT_FOUND;
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCodes.ERR_RESOURCE_NOT_FOUND;
        this.resourceName = "Recurso";
        this.fieldName = "id";
        this.fieldValue = null;
    }

    public ResourceNotFoundException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.resourceName = "Recurso";
        this.fieldName = "id";
        this.fieldValue = null;
    }
}
