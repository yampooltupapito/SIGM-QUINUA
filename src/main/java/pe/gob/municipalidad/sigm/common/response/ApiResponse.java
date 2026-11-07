package pe.gob.municipalidad.sigm.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pe.gob.municipalidad.sigm.common.pagination.PagedResponse;
import pe.gob.municipalidad.sigm.common.util.DateUtils;
import pe.gob.municipalidad.sigm.common.util.TraceContext;

import java.time.OffsetDateTime;

/**
 * Envoltorio genérico y estandarizado para respuestas exitosas de la API REST de SIGM-QUINUA.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Builder.Default
    private boolean success = true;

    private String message;

    private T data;

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime timestamp = DateUtils.nowOffset();

    @Builder.Default
    private String traceId = TraceContext.getTraceId();

    /* =========================================================================
     * Métodos Factoría para Creación de Instancias
     * ========================================================================= */

    public static <T> ApiResponse<T> of(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(DateUtils.nowOffset())
                .traceId(TraceContext.getTraceId())
                .build();
    }

    public static <T> ApiResponse<T> of(T data) {
        return of("Operación realizada correctamente.", data);
    }

    public static <T> ApiResponse<T> messageOnly(String message) {
        return of(message, null);
    }

    /* =========================================================================
     * Métodos Factoría para Respuestas ResponseEntity
     * ========================================================================= */

    /**
     * Respuesta HTTP 200 OK con mensaje y datos.
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(of(message, data));
    }

    /**
     * Respuesta HTTP 200 OK con datos y mensaje por defecto.
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(of(data));
    }

    /**
     * Respuesta HTTP 200 OK con solo mensaje de confirmación.
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(String message) {
        return ResponseEntity.ok(messageOnly(message));
    }

    /**
     * Respuesta HTTP 201 Created para recursos creados con éxito.
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(of(message, data));
    }

    /**
     * Respuesta HTTP 204 No Content sin cuerpo.
     */
    public static <T> ResponseEntity<T> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Respuesta HTTP 200 OK para resultados paginados.
     */
    public static <T> ResponseEntity<ApiResponse<PagedResponse<T>>> paginated(String message, PagedResponse<T> pagedData) {
        return ResponseEntity.ok(of(message, pagedData));
    }

    /**
     * Respuesta HTTP 200 OK para resultados paginados con mensaje por defecto.
     */
    public static <T> ResponseEntity<ApiResponse<PagedResponse<T>>> paginated(PagedResponse<T> pagedData) {
        return ResponseEntity.ok(of("Consulta paginada obtenida con éxito.", pagedData));
    }
}
