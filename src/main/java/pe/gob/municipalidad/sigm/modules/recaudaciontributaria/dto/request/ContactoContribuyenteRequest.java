package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoContactoContribuyente;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactoContribuyenteRequest {

    @NotNull(message = "El tipo de contacto es obligatorio")
    private TipoContactoContribuyente tipoContacto;

    @NotBlank(message = "El valor del contacto es obligatorio")
    private String valor;

    private String descripcion;
    private boolean esPrincipal;
    private boolean autorizadoNotificacion;
}
