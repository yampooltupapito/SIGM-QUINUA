package pe.gob.municipalidad.sigm.modules.portal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public final class PortalContactoDtos {

    private PortalContactoDtos() {}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactoPublicRequest {
        @NotBlank(message = "El nombre y apellidos son obligatorios")
        @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
        private String remitenteNombre;

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato de correo electrónico no es válido")
        @Size(max = 150, message = "El correo no puede exceder 150 caracteres")
        private String remitenteCorreo;

        @Pattern(regexp = "^$|^[0-9+()\\s-]{6,20}$", message = "El número telefónico no es válido")
        private String remitenteTelefono;

        @NotBlank(message = "El asunto de su consulta es obligatorio")
        @Size(max = 200, message = "El asunto no puede exceder 200 caracteres")
        private String asunto;

        @NotBlank(message = "El mensaje detallado es obligatorio")
        @Size(min = 10, max = 3000, message = "El mensaje debe contener entre 10 y 3000 caracteres")
        private String mensaje;

        // Campo trampa (Honeypot) antispam transparente para robots
        private String website;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactoAtencionRequest {
        @NotBlank(message = "La respuesta u observación interna es obligatoria")
        private String respuestaInterna;

        @Pattern(regexp = "^(PENDIENTE|EN_PROCESO|ATENDIDO)$", message = "Estado inválido")
        private String estado;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactoResponse {
        private Long id;
        private String codigo;
        private String remitenteNombre;
        private String remitenteCorreo;
        private String remitenteTelefono;
        private String asunto;
        private String mensaje;
        private String estado;
        private String respuestaInterna;
        private String atendidoPor;
        private LocalDateTime atendidoAt;
        private LocalDateTime createdAt;
    }
}
