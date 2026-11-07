package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoRepresentacion;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Petición de empadronamiento de un comerciante Persona Jurídica.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComercianteJuridicoCreateRequest {

    private UUID personaJuridicaId;

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "^(10|15|16|17|20)[0-9]{9}$", message = "El RUC debe tener 11 dígitos y comenzar con prefijo oficial válido")
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 255, message = "La razón social no puede exceder 255 caracteres")
    private String razonSocial;

    @Size(max = 255, message = "El nombre comercial no puede exceder 255 caracteres")
    private String nombreComercial;

    @Size(max = 255, message = "El domicilio fiscal no puede exceder 255 caracteres")
    private String domicilioFiscal;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Size(max = 150, message = "El correo no puede exceder 150 caracteres")
    private String correo;

    private String codigoComerciante;

    private EstadoComerciante estado;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

    private Set<Long> actividadIds;

    // Representante Legal inicial (opcional u obligatorio según procedimiento)
    private Long representantePersonaId;

    @Pattern(regexp = "^$|^[0-9]{8}$", message = "El DNI del representante debe tener 8 dígitos")
    private String representanteDni;

    @Size(max = 100)
    private String representanteNombres;

    @Size(max = 100)
    private String representanteApellidoPaterno;

    @Size(max = 100)
    private String representanteApellidoMaterno;

    @Builder.Default
    private TipoRepresentacion representanteTipo = TipoRepresentacion.REPRESENTANTE_LEGAL;

    private LocalDate representanteFechaInicio;

    private Long representanteDocumentoSustentoId;
}
