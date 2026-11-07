package pe.gob.municipalidad.sigm.web.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pe.gob.municipalidad.sigm.common.constants.ErrorCodes;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.exception.UnauthorizedException;
import pe.gob.municipalidad.sigm.common.response.ErrorResponse;
import pe.gob.municipalidad.sigm.common.util.DateUtils;
import pe.gob.municipalidad.sigm.common.util.TraceContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para SIGM-QUINUA.
 * Registra detalles técnicos detallados con traceId y entrega respuestas limpias,
 * consistentes y seguras (JSON o vista HTML según corresponda), sin exponer stack traces
 * ni información confidencial del servidor.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Errores de validación de Bean Validation en DTOs (@Valid / @Validated).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        log.warn("[Trace: {}] Error de validación en {}: {}", traceId, request.getRequestURI(), ex.getMessage());

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message("Los datos enviados no son válidos")
                .errorCode(ErrorCodes.VALIDATION_ERROR)
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .errors(errors)
                .timestamp(DateUtils.nowOffset())
                .traceId(traceId)
                .build();

        if (isRestRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return createModelAndView("errors/400", HttpStatus.BAD_REQUEST, response);
    }

    /**
     * Violaciones de restricciones a nivel de parámetros (@RequestParam, @PathVariable).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Object handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        log.warn("[Trace: {}] Violación de restricciones en {}: {}", traceId, request.getRequestURI(), ex.getMessage());

        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String property = violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "parametro";
            // Extraer el nombre simple del campo si viene con prefijo de método
            if (property.contains(".")) {
                property = property.substring(property.lastIndexOf('.') + 1);
            }
            errors.put(property, violation.getMessage());
        }

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message("Violación de restricción en los parámetros de entrada.")
                .errorCode(ErrorCodes.VALIDATION_ERROR)
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .errors(errors)
                .timestamp(DateUtils.nowOffset())
                .traceId(traceId)
                .build();

        if (isRestRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return createModelAndView("errors/400", HttpStatus.BAD_REQUEST, response);
    }

    /**
     * Petición inválida de negocio o datos mal formados.
     */
    @ExceptionHandler(InvalidRequestException.class)
    public Object handleInvalidRequest(InvalidRequestException ex, HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        log.warn("[Trace: {}] Solicitud inválida en {}: [{}] {}", traceId, request.getRequestURI(), ex.getErrorCode(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode() != null ? ex.getErrorCode() : ErrorCodes.INVALID_REQUEST)
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .timestamp(DateUtils.nowOffset())
                .traceId(traceId)
                .build();

        if (isRestRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return createModelAndView("errors/400", HttpStatus.BAD_REQUEST, response);
    }

    /**
     * JSON mal formado o tipos no deserializables.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Object handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        log.warn("[Trace: {}] JSON mal formado o no legible en {}: {}", traceId, request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message("El cuerpo de la solicitud no contiene un formato JSON válido o contiene datos ilegibles.")
                .errorCode(ErrorCodes.MALFORMED_JSON)
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .timestamp(DateUtils.nowOffset())
                .traceId(traceId)
                .build();

        if (isRestRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return createModelAndView("errors/400", HttpStatus.BAD_REQUEST, response);
    }

    /**
     * Parámetro de tipo incorrecto en la URL (ej: pasar texto donde se espera número).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Object handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        log.warn("[Trace: {}] Tipo de parámetro incorrecto en {}: parámetro '{}'", traceId, request.getRequestURI(), ex.getName());

        Map<String, String> errors = Map.of(ex.getName(), "El valor proporcionado no tiene el tipo de dato esperado.");

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message("Parámetro de solicitud inválido.")
                .errorCode(ErrorCodes.INVALID_REQUEST)
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .errors(errors)
                .timestamp(DateUtils.nowOffset())
                .traceId(traceId)
                .build();

        if (isRestRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return createModelAndView("errors/400", HttpStatus.BAD_REQUEST, response);
    }

    /**
     * Recurso de negocio no encontrado (404).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        log.info("[Trace: {}] Recurso no encontrado en {}: {}", traceId, request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode() != null ? ex.getErrorCode() : ErrorCodes.RESOURCE_NOT_FOUND)
                .status(HttpStatus.NOT_FOUND.value())
                .path(request.getRequestURI())
                .timestamp(DateUtils.nowOffset())
                .traceId(traceId)
                .build();

        if (isRestRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return createModelAndView("errors/404", HttpStatus.NOT_FOUND, response);
    }

    /**
     * Ruta o recurso estático web no encontrado (404).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        log.debug("[Trace: {}] Ruta no encontrada: {}", traceId, request.getRequestURI());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message("La ruta o recurso solicitado no existe en el servidor.")
                .errorCode(ErrorCodes.RESOURCE_NOT_FOUND)
                .status(HttpStatus.NOT_FOUND.value())
                .path(request.getRequestURI())
                .timestamp(DateUtils.nowOffset())
                .traceId(traceId)
                .build();

        if (isRestRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return createModelAndView("errors/404", HttpStatus.NOT_FOUND, response);
    }

    /**
     * Recurso duplicado (409 Conflict).
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public Object handleDuplicateResource(DuplicateResourceException ex, HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        log.warn("[Trace: {}] Recurso duplicado en {}: {}", traceId, request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode() != null ? ex.getErrorCode() : ErrorCodes.RESOURCE_ALREADY_EXISTS)
                .status(HttpStatus.CONFLICT.value())
                .path(request.getRequestURI())
                .timestamp(DateUtils.nowOffset())
                .traceId(traceId)
                .build();

        if (isRestRequest(request)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        return createModelAndView("errors/400", HttpStatus.CONFLICT, response);
    }

    /**
     * Conflicto de estado en la entidad (409 Conflict).
     */
    @ExceptionHandler(ConflictException.class)
    public Object handleConflict(ConflictException ex, HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        log.warn("[Trace: {}] Conflicto de estado en {}: {}", traceId, request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode() != null ? ex.getErrorCode() : ErrorCodes.CONFLICT_STATE)
                .status(HttpStatus.CONFLICT.value())
                .path(request.getRequestURI())
                .timestamp(DateUtils.nowOffset())
                .traceId(traceId)
                .build();

        if (isRestRequest(request)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        return createModelAndView("errors/400", HttpStatus.CONFLICT, response);
    }

    /**
     * Regla de negocio incumplida (422 Unprocessable Entity).
     */
    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException ex, HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        log.warn("[Trace: {}] Regla de negocio no satisfecha en {}: [{}] {}", traceId, request.getRequestURI(), ex.getErrorCode(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode() != null ? ex.getErrorCode() : ErrorCodes.BUSINESS_RULE_VIOLATION)
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .path(request.getRequestURI())
                .timestamp(DateUtils.nowOffset())
                .traceId(traceId)
                .build();

        if (isRestRequest(request)) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }

        return createModelAndView("errors/400", HttpStatus.UNPROCESSABLE_ENTITY, response);
    }

    /**
     * Método HTTP no soportado (405 Method Not Allowed).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        log.warn("[Trace: {}] Método HTTP {} no soportado en {}", traceId, ex.getMethod(), request.getRequestURI());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(String.format("El método HTTP '%s' no está permitido para esta ruta.", ex.getMethod()))
                .errorCode(ErrorCodes.METHOD_NOT_ALLOWED)
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .path(request.getRequestURI())
                .timestamp(DateUtils.nowOffset())
                .traceId(traceId)
                .build();

        if (isRestRequest(request)) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
        }

        return createModelAndView("errors/400", HttpStatus.METHOD_NOT_ALLOWED, response);
    }

    /**
     * Acceso denegado (403 Forbidden).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        log.warn("[Trace: {}] Acceso denegado a recurso {} por usuario {}", traceId, request.getRequestURI(), request.getRemoteUser());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message("No cuenta con los privilegios o permisos necesarios para acceder a este recurso municipal.")
                .errorCode(ErrorCodes.FORBIDDEN_ACCESS)
                .status(HttpStatus.FORBIDDEN.value())
                .path(request.getRequestURI())
                .timestamp(DateUtils.nowOffset())
                .traceId(traceId)
                .build();

        if (isRestRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        return createModelAndView("errors/403", HttpStatus.FORBIDDEN, response);
    }

    /**
     * Acceso no autenticado (401 Unauthorized) si llega a este nivel.
     */
    @ExceptionHandler({UnauthorizedException.class, AuthenticationException.class})
    public Object handleUnauthorized(Exception ex, HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        log.warn("[Trace: {}] Acceso no autorizado en {}: {}", traceId, request.getRequestURI(), ex.getMessage());

        String message = (ex.getMessage() != null && !ex.getMessage().isBlank())
                ? ex.getMessage()
                : "Debe autenticarse con credenciales válidas para acceder a este recurso.";

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(message)
                .errorCode(ErrorCodes.UNAUTHORIZED_ACCESS)
                .status(HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI())
                .timestamp(DateUtils.nowOffset())
                .traceId(traceId)
                .build();

        if (isRestRequest(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return createModelAndView("errors/401", HttpStatus.UNAUTHORIZED, response);
    }

    /**
     * Error interno no controlado del servidor (500 Internal Server Error).
     * Jamás expone stack traces, secretos o consultas SQL al cliente.
     */
    @ExceptionHandler(Exception.class)
    public Object handleGlobalException(Exception ex, HttpServletRequest request) {
        String traceId = TraceContext.getTraceId();
        log.error("[Trace: {}] Error no controlado en servidor al procesar {}: ", traceId, request.getRequestURI(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message("Ocurrió un error inesperado al procesar la solicitud. Por favor intente nuevamente o contacte a soporte técnico municipal indicando el identificador de trazabilidad.")
                .errorCode(ErrorCodes.INTERNAL_SERVER_ERROR)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(request.getRequestURI())
                .timestamp(DateUtils.nowOffset())
                .traceId(traceId)
                .build();

        if (isRestRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        return createModelAndView("errors/500", HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    private boolean isRestRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        String uri = request.getRequestURI();
        return (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE)) || uri.startsWith("/api/");
    }

    private ModelAndView createModelAndView(String viewName, HttpStatus status, ErrorResponse errorResponse) {
        ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.addObject("error", errorResponse);
        modelAndView.setStatus(status);
        return modelAndView;
    }
}
