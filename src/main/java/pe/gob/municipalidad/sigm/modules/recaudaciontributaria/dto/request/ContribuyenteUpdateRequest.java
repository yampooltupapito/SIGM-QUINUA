package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioContactoPreferido;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContribuyenteUpdateRequest {

    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    private String numeroDocumento;

    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String nombreRazonSocial;
    private String nombreComercial;

    private String representanteLegalNombre;
    private String representanteLegalDocumento;

    private String domicilioFiscal;
    private String referenciaDomicilio;

    private String telefono;
    private String telefonoAlternativo;
    private String correo;
    private MedioContactoPreferido medioContactoPreferido;

    private String observaciones;
}
