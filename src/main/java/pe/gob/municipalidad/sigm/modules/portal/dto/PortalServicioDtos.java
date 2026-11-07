package pe.gob.municipalidad.sigm.modules.portal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class PortalServicioDtos {

    private PortalServicioDtos() {}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServicioUpdateRequest {
        @NotBlank(message = "El nombre del servicio es obligatorio")
        @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
        private String nombre;

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        private String descripcion;

        @NotBlank(message = "El icono es obligatorio")
        @Size(max = 50, message = "El icono no puede exceder 50 caracteres")
        private String icono;

        @Min(value = 1, message = "El orden mínimo es 1")
        private Integer orden;

        @Pattern(regexp = "^(DISPONIBLE|PROXIMAMENTE|EN_DESARROLLO)$", message = "Disponibilidad inválida")
        private String estadoDisponibilidad;

        @Size(max = 255, message = "La URL no puede exceder 255 caracteres")
        private String urlAcceso;

        @Pattern(regexp = "^(ACTIVO|INACTIVO)$", message = "Estado inválido")
        private String estado;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServicioResponse {
        private Long id;
        private String codigo;
        private String nombre;
        private String descripcion;
        private String icono;
        private Integer orden;
        private String estadoDisponibilidad;
        private String urlAcceso;
        private String estado;
    }
}
