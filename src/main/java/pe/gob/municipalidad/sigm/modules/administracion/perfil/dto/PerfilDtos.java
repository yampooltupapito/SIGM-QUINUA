package pe.gob.municipalidad.sigm.modules.administracion.perfil.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Objetos de Transferencia de Datos (DTO) para la gestión del perfil,
 * seguridad, credenciales, preferencias y asignaciones institucionales.
 */
public class PerfilDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerfilResponse {
        private Long usuarioId;
        private Long personaId;
        private String username;
        private String nombres;
        private String apellidoPaterno;
        private String apellidoMaterno;
        private String nombreCompleto;
        private String tipoDocumento;
        private String numeroDocumento;
        private String numeroDocumentoEnmascarado;
        private String correo;
        private String telefono;
        private String direccion;
        private String fotoUrl;
        private Boolean enabled;
        private Boolean accountLocked;
        private LocalDateTime fechaRegistro;
        private List<RolInfoDto> roles;
        private OficinaInfoDto oficinaPrincipal;
        private CargoInfoDto cargoPrincipal;
        private List<String> tiposActores;
        private UsuarioPreferenciaDto preferencias;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolInfoDto {
        private Long id;
        private String codigo;
        private String nombre;
        private String descripcion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OficinaInfoDto {
        private Long id;
        private String codigo;
        private String nombre;
        private String sigla;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CargoInfoDto {
        private Long id;
        private String codigo;
        private String nombre;
        private Integer nivelJerarquico;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActualizarDatosPersonalesRequest {
        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres.")
        private String telefono;

        @NotBlank(message = "El correo electrónico es obligatorio.")
        @Email(message = "El formato de correo electrónico no es válido.")
        @Size(max = 150, message = "El correo electrónico no puede superar los 150 caracteres.")
        private String correo;

        @Size(max = 255, message = "La dirección no puede superar los 255 caracteres.")
        private String direccion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CambiarPasswordRequest {
        @NotBlank(message = "La contraseña actual es obligatoria.")
        private String passwordActual;

        @NotBlank(message = "La nueva contraseña es obligatoria.")
        @Size(min = 8, max = 100, message = "La nueva contraseña debe tener entre 8 y 100 caracteres.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._\\-#]).{8,}$",
                message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial (@$!%*?&._-#)."
        )
        private String passwordNuevo;

        @NotBlank(message = "La confirmación de la nueva contraseña es obligatoria.")
        private String confirmacionPasswordNuevo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioPreferenciaDto {
        @Pattern(regexp = "OSCURO|CLARO|SISTEMA", message = "El tema debe ser OSCURO, CLARO o SISTEMA.")
        private String tema;

        @NotBlank(message = "El idioma es obligatorio.")
        @Size(max = 10)
        private String idioma;

        @NotNull(message = "La preferencia de notificación por correo es obligatoria.")
        private Boolean notifEmail;

        @NotNull(message = "La preferencia de notificación de sistema es obligatoria.")
        private Boolean notifSistema;

        @NotNull(message = "El tamaño de página es obligatorio.")
        @Min(value = 5, message = "El tamaño de página mínimo es 5.")
        @Max(value = 100, message = "El tamaño de página máximo es 100.")
        private Integer pageSize;

        private Boolean sidebarColapsado;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SesionActivaDto {
        private Long id;
        private String tokenId;
        private String ip;
        private String userAgent;
        private String dispositivoAproximado;
        private OffsetDateTime fechaInicio;
        private OffsetDateTime fechaExpiracion;
        private Boolean activa;
        private Boolean esActual;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermisosConsolidadosDto {
        private List<RolInfoDto> roles;
        private List<String> modulosAutorizados;
        private List<PermisoDetalleDto> permisosEfectivos;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermisoDetalleDto {
        private Long id;
        private String modulo;
        private String accion;
        private String codigo;
        private String descripcion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AsignacionOficinaDto {
        private Long id;
        private Long oficinaId;
        private String oficinaNombre;
        private String oficinaCodigo;
        private Long cargoId;
        private String cargoNombre;
        private Long tipoActorId;
        private String tipoActorNombre;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private Boolean principal;
        private String estado;
    }
}
