package pe.gob.municipalidad.sigm.common.exception;

/**
 * Excepción lanzada ante intentos de acceso no autorizados o credenciales inválidas.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
