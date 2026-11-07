package pe.gob.municipalidad.sigm.modules.administracion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Contenedor de Data Transfer Objects para administración de Tipos de Actores.
 */
public final class TipoActorDtos {

    private TipoActorDtos() {}

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TipoActorCreateRequest {

        @NotBlank(message = "El código es obligatorio")
        @Size(max = 50, message = "El código no debe superar 50 caracteres")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "El código solo debe contener letras mayúsculas, números y guiones bajos")
        private String codigo;

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        private String nombre;

        @Size(max = 500, message = "La descripción no debe superar 500 caracteres")
        private String descripcion;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TipoActorUpdateRequest {

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        private String nombre;

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        private String descripcion;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TipoActorResponse {
        private Long id;
        private String codigo;
        private String nombre;
        private String descripcion;
        private String estado;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
