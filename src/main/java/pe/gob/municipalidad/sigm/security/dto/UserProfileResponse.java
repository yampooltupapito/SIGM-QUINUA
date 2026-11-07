package pe.gob.municipalidad.sigm.security.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * DTO para endpoint /api/auth/me con información del usuario autenticado sin revelar secretos.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    private Long id;

    private String username;

    private String nombreCompleto;

    private String numeroDocumento;

    private String correo;

    private Set<String> roles;

    private Set<String> permisos;
}
