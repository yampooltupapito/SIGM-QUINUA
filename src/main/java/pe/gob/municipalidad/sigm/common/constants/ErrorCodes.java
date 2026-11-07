package pe.gob.municipalidad.sigm.common.constants;

/**
 * Códigos de error canónicos y estables para la API municipal de SIGM-QUINUA.
 */
public final class ErrorCodes {

    private ErrorCodes() {
        // Utility class
    }

    public static final String ERR_RESOURCE_NOT_FOUND = "ERR_RESOURCE_NOT_FOUND";
    public static final String ERR_DUPLICATE_RESOURCE = "ERR_DUPLICATE_RESOURCE";
    public static final String ERR_CONFLICT = "ERR_CONFLICT";
    public static final String ERR_INVALID_REQUEST = "ERR_INVALID_REQUEST";
    public static final String ERR_VALIDATION = "ERR_VALIDATION";
    public static final String ERR_BUSINESS_RULE = "ERR_BUSINESS_RULE";
    public static final String ERR_UNAUTHORIZED = "ERR_UNAUTHORIZED";
    public static final String ERR_FORBIDDEN = "ERR_FORBIDDEN";
    public static final String ERR_METHOD_NOT_ALLOWED = "ERR_METHOD_NOT_ALLOWED";
    public static final String ERR_MALFORMED_JSON = "ERR_MALFORMED_JSON";
    public static final String ERR_INTERNAL = "ERR_INTERNAL";

    // Alias sin prefijo
    public static final String RESOURCE_NOT_FOUND = ERR_RESOURCE_NOT_FOUND;
    public static final String RESOURCE_ALREADY_EXISTS = ERR_DUPLICATE_RESOURCE;
    public static final String CONFLICT_STATE = ERR_CONFLICT;
    public static final String INVALID_REQUEST = ERR_INVALID_REQUEST;
    public static final String VALIDATION_ERROR = ERR_VALIDATION;
    public static final String BUSINESS_RULE_VIOLATION = ERR_BUSINESS_RULE;
    public static final String UNAUTHORIZED_ACCESS = ERR_UNAUTHORIZED;
    public static final String FORBIDDEN_ACCESS = ERR_FORBIDDEN;
    public static final String METHOD_NOT_ALLOWED = ERR_METHOD_NOT_ALLOWED;
    public static final String MALFORMED_JSON = ERR_MALFORMED_JSON;
    public static final String INTERNAL_SERVER_ERROR = ERR_INTERNAL;
}
