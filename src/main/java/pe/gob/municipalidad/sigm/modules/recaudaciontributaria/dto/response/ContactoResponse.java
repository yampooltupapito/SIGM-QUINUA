package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoContactoContribuyente;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactoResponse {
    private UUID id;
    private TipoContactoContribuyente tipoContacto;
    private String tipoContactoDesc;
    private String valor;
    private String valorEnmascarado;
    private String descripcion;
    private boolean esPrincipal;
    private boolean autorizadoNotificacion;
    private String estado;
}
