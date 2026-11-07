package pe.gob.municipalidad.sigm.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.gob.municipalidad.sigm.common.util.DateUtils;
import pe.gob.municipalidad.sigm.common.util.TraceContext;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Envoltorio estándar para respuestas de error de la API REST de SIGM-QUINUA.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Builder.Default
    private boolean success = false;

    private String message;

    private String errorCode;

    private int status;

    private String path;

    private Map<String, String> errors;

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime timestamp = DateUtils.nowOffset();

    @Builder.Default
    private String traceId = TraceContext.getTraceId();
}
