package pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para información de contexto del usuario autenticado en navbar y bienvenida.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private String username;
    private String nombreCompleto;
    private String iniciales;
    private String tipoDocumento;
    private String numeroDocumento;
    private String correo;
    private String telefono;
    private String cargo;
    private Long oficinaId;
    private String oficinaCodigo;
    private String oficina;
    private String fotoUrl;
    @Builder.Default
    private List<String> roles = new ArrayList<>();
    @Builder.Default
    private List<String> permisos = new ArrayList<>();
    private String fechaHoraActualPeru;
    private boolean esSuperAdmin;
    private boolean esAdmin;
    private boolean esJefeOficina;
    private boolean esOperador;
    private boolean esCiudadano;
}
