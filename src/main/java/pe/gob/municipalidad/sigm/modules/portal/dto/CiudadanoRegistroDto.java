package pe.gob.municipalidad.sigm.modules.portal.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class CiudadanoRegistroDto {

    private CiudadanoRegistroDto() {}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CiudadanoRegistroRequest {
        @NotBlank(message = "El tipo de documento es obligatorio")
        @Pattern(regexp = "^(DNI|CARNET_EXTRANJERIA|PASAPORTE)$", message = "Tipo de documento no válido")
        @Builder.Default
        private String tipoDocumento = "DNI";

        @NotBlank(message = "El número de documento es obligatorio")
        @Pattern(regexp = "^[0-9A-Za-z]{8,15}$", message = "El número de documento debe tener entre 8 y 15 caracteres alfanuméricos")
        private String numeroDocumento;

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres")
        private String nombres;

        @NotBlank(message = "El apellido paterno es obligatorio")
        @Size(min = 2, max = 100, message = "El apellido paterno debe tener entre 2 y 100 caracteres")
        private String apellidoPaterno;

        @Size(max = 100, message = "El apellido materno no puede superar 100 caracteres")
        private String apellidoMaterno;

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Formato de correo electrónico no válido")
        @Size(max = 150, message = "El correo no puede exceder 150 caracteres")
        private String correo;

        @Pattern(regexp = "^$|^[0-9+()\\s-]{6,20}$", message = "Número telefónico no válido")
        private String telefono;

        @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
        private String direccion;

        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._-]{4,30}$", message = "El usuario debe tener entre 4 y 30 caracteres (letras, números, puntos o guiones)")
        private String username;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 50, message = "La contraseña debe tener al menos 8 caracteres")
        private String password;

        @NotBlank(message = "La confirmación de contraseña es obligatoria")
        private String confirmPassword;

        @AssertTrue(message = "Debe aceptar los términos de uso y la política de privacidad institucional")
        private boolean terminosAceptados;

        // Campo trampa (Honeypot) antispam
        private String website;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CiudadanoRegistroResponse {
        private boolean success;
        private String message;
        private String username;
        private String numeroDocumento;
        private String nombreCompleto;
    }
}
