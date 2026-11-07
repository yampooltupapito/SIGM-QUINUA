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
import java.util.Set;

public final class RolDtos {

    private RolDtos() {}

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolCreateRequest {

        @NotBlank(message = "El código del rol es obligatorio.")
        @Size(min = 3, max = 50, message = "El código del rol debe tener entre 3 y 50 caracteres.")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "El código del rol debe estar en mayúsculas y solo contener letras, números y guiones bajos (ej: OPERADOR_RENTAS).")
        private String codigo;

        @NotBlank(message = "El nombre del rol es obligatorio.")
        @Size(max = 100, message = "El nombre del rol no puede superar los 100 caracteres.")
        private String nombre;

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
        private String descripcion;

        private Set<Long> permisoIds;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolUpdateRequest {

        @NotBlank(message = "El nombre del rol es obligatorio.")
        @Size(max = 100, message = "El nombre del rol no puede superar los 100 caracteres.")
        private String nombre;

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
        private String descripcion;

        private String estado;

        private Set<Long> permisoIds;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolResponse {
        private Long id;
        private String codigo;
        private String nombre;
        private String descripcion;
        private String estado;
        private Set<String> permisosCodigos;
        private int totalPermisos;
        private long cantidadUsuarios;
        private int totalUsuarios;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
