package pe.gob.municipalidad.sigm.modules.portal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class PortalFaqDtos {

    private PortalFaqDtos() {}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FaqCreateRequest {
        @NotBlank(message = "La pregunta es obligatoria")
        @Size(max = 300, message = "La pregunta no puede exceder 300 caracteres")
        private String pregunta;

        @NotBlank(message = "La respuesta es obligatoria")
        private String respuesta;

        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
        @Builder.Default
        private String categoria = "GENERAL";

        @Min(value = 1, message = "El orden mínimo es 1")
        @Builder.Default
        private Integer orden = 1;

        @Pattern(regexp = "^(ACTIVO|INACTIVO)$", message = "Estado inválido")
        @Builder.Default
        private String estado = "ACTIVO";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FaqUpdateRequest {
        @NotBlank(message = "La pregunta es obligatoria")
        @Size(max = 300, message = "La pregunta no puede exceder 300 caracteres")
        private String pregunta;

        @NotBlank(message = "La respuesta es obligatoria")
        private String respuesta;

        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
        private String categoria;

        @Min(value = 1, message = "El orden mínimo es 1")
        private Integer orden;

        @Pattern(regexp = "^(ACTIVO|INACTIVO)$", message = "Estado inválido")
        private String estado;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FaqResponse {
        private Long id;
        private String pregunta;
        private String respuesta;
        private String categoria;
        private Integer orden;
        private String estado;
    }
}
