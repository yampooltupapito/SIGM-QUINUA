package pe.gob.municipalidad.sigm.modules.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public final class PortalNoticiaDtos {

    private PortalNoticiaDtos() {}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticiaCreateRequest {
        @NotBlank(message = "El título de la publicación es obligatorio")
        @Size(max = 255, message = "El título no puede exceder 255 caracteres")
        private String titulo;

        @NotBlank(message = "El resumen es obligatorio")
        @Size(max = 500, message = "El resumen no puede exceder 500 caracteres")
        private String resumen;

        @NotBlank(message = "El contenido detallado es obligatorio")
        private String contenido;

        @Pattern(regexp = "^(NOTICIA|COMUNICADO|CONVOCATORIA)$", message = "Tipo de publicación inválido")
        @Builder.Default
        private String tipo = "NOTICIA";

        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
        @Builder.Default
        private String categoria = "INSTITUCIONAL";

        @Size(max = 500, message = "La URL de imagen no puede exceder 500 caracteres")
        private String imagenUrl;

        @NotBlank(message = "El autor institucional es obligatorio")
        @Size(max = 150, message = "El autor no puede exceder 150 caracteres")
        @Builder.Default
        private String autor = "Municipalidad Distrital de Quinua";

        @Builder.Default
        private Boolean destacada = false;

        @Builder.Default
        private Boolean publicarInmediatamente = true;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticiaUpdateRequest {
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 255, message = "El título no puede exceder 255 caracteres")
        private String titulo;

        @NotBlank(message = "El resumen es obligatorio")
        @Size(max = 500, message = "El resumen no puede exceder 500 caracteres")
        private String resumen;

        @NotBlank(message = "El contenido es obligatorio")
        private String contenido;

        @Pattern(regexp = "^(NOTICIA|COMUNICADO|CONVOCATORIA)$", message = "Tipo de publicación inválido")
        private String tipo;

        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
        private String categoria;

        @Size(max = 500, message = "La URL de imagen no puede exceder 500 caracteres")
        private String imagenUrl;

        @NotBlank(message = "El autor es obligatorio")
        @Size(max = 150, message = "El autor no puede exceder 150 caracteres")
        private String autor;

        @Pattern(regexp = "^(BORRADOR|PUBLICADO|ARCHIVADO)$", message = "Estado inválido")
        private String estado;

        private Boolean destacada;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticiaResponse {
        private Long id;
        private String titulo;
        private String slug;
        private String resumen;
        private String tipo;
        private String categoria;
        private String imagenUrl;
        private String autor;
        private String estado;
        private LocalDateTime fechaPublicacion;
        private Boolean destacada;
        private Integer visitas;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticiaDetailResponse {
        private Long id;
        private String titulo;
        private String slug;
        private String resumen;
        private String contenido;
        private String tipo;
        private String categoria;
        private String imagenUrl;
        private String autor;
        private String estado;
        private LocalDateTime fechaPublicacion;
        private Boolean destacada;
        private Integer visitas;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
