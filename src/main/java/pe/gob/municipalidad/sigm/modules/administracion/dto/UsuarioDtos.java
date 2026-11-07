package pe.gob.municipalidad.sigm.modules.administracion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public final class UsuarioDtos {

    private UsuarioDtos() {}

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioCreateRequest {

        @NotBlank(message = "El nombre de usuario es obligatorio.")
        @Size(min = 4, max = 50, message = "El nombre de usuario debe tener entre 4 y 50 caracteres.")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos.")
        private String username;

        @NotBlank(message = "El tipo de documento es obligatorio.")
        private String tipoDocumento;

        @NotBlank(message = "El número de documento es obligatorio.")
        @Pattern(regexp = "^[0-9]{8,12}$", message = "El documento de identidad debe tener entre 8 y 12 dígitos numéricos.")
        private String numeroDocumento;

        @NotBlank(message = "Los nombres son obligatorios.")
        @Size(max = 100, message = "Los nombres no pueden superar los 100 caracteres.")
        private String nombres;

        @NotBlank(message = "El apellido paterno es obligatorio.")
        @Size(max = 100, message = "El apellido paterno no puede superar los 100 caracteres.")
        private String apellidoPaterno;

        @Size(max = 100, message = "El apellido materno no puede superar los 100 caracteres.")
        private String apellidoMaterno;

        @NotBlank(message = "El correo institucional o personal es obligatorio.")
        @Email(message = "El formato del correo electrónico no es válido.")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres.")
        private String correo;

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres.")
        private String telefono;

        @Size(max = 255, message = "La dirección no puede superar los 255 caracteres.")
        private String direccion;

        @Size(min = 8, max = 100, message = "La contraseña inicial debe tener entre 8 y 100 caracteres si se especifica.")
        private String password;

        private Set<Long> rolIds;
        private Set<Long> roleIds;
        private Long oficinaId;
        private Long cargoId;
        private Long tipoActorId;

        public Set<Long> getRolIds() {
            return rolIds != null ? rolIds : roleIds;
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioUpdateRequest {

        @NotBlank(message = "Los nombres son obligatorios.")
        @Size(max = 100, message = "Los nombres no pueden superar los 100 caracteres.")
        private String nombres;

        @NotBlank(message = "El apellido paterno es obligatorio.")
        @Size(max = 100, message = "El apellido paterno no puede superar los 100 caracteres.")
        private String apellidoPaterno;

        @Size(max = 100, message = "El apellido materno no puede superar los 100 caracteres.")
        private String apellidoMaterno;

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El formato del correo electrónico no es válido.")
        private String correo;

        private String telefono;
        private String direccion;

        private Boolean enabled;
        private Boolean accountLocked;

        private Set<Long> rolIds;
        private Set<Long> roleIds;

        public Set<Long> getRolIds() {
            return rolIds != null ? rolIds : roleIds;
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PasswordResetRequest {

        @NotBlank(message = "La nueva contraseña es obligatoria.")
        @Size(min = 8, max = 100, message = "La nueva contraseña debe tener al menos 8 caracteres.")
        private String nuevaPassword;

        private String confirmarPassword;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioResponse {
        private Long id;
        private String username;
        private Boolean enabled;
        private Boolean accountLocked;
        private Integer failedAttempts;
        private Long personaId;
        private String tipoDocumento;
        private String numeroDocumento;
        private String personaNombre;
        private String nombreCompleto;
        private String correo;
        private String telefono;
        private Set<String> roles;
        private List<String> oficinas;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public String getPersonaNombre() {
            return personaNombre != null ? personaNombre : nombreCompleto;
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioDetailResponse {
        private Long id;
        private String username;
        private Boolean enabled;
        private Boolean accountLocked;
        private Integer failedAttempts;
        private Long personaId;
        private String tipoDocumento;
        private String numeroDocumento;
        private String nombres;
        private String apellidoPaterno;
        private String apellidoMaterno;
        private String personaNombre;
        private String nombreCompleto;
        private String correo;
        private String telefono;
        private String direccion;
        private Set<String> roles;
        private List<OficinaDtos.AsignacionOficinaResponse> asignacionesOficina;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
