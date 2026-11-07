package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioContactoPreferido;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoContribuyente;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContribuyenteCreateRequest {

    @NotNull(message = "El tipo de contribuyente es obligatorio")
    private TipoContribuyente tipoContribuyente;

    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    private String numeroDocumento;

    // Campos persona natural
    private String nombres;
    private String primerApellido;
    private String segundoApellido;

    // Campos persona jurídica
    private String nombreRazonSocial;
    private String nombreComercial;
    private String representanteLegalNombre;
    private String representanteLegalDocumento;

    // Domicilio inicial
    private String domicilioFiscal;
    private String referenciaDomicilio;
    private String comunidadCentroPoblado;
    private String tipoVia;
    private String nombreVia;
    private String numeroMunicipal;

    // Contacto
    private String telefono;
    private String telefonoAlternativo;
    private String correo;
    private MedioContactoPreferido medioContactoPreferido;

    private Long oficinaRegistroId;
    private String observaciones;
}
