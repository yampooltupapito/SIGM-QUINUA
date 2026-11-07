package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

/**
 * Petición de actualización de datos autorizados de un comerciante.
 * Protege datos de identidad (DNI/RUC) para evitar sustituciones arbitrarias.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComercianteUpdateRequest {

    private String codigoComerciante;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Size(max = 150, message = "El correo no puede exceder 150 caracteres")
    private String correo;

    @Size(max = 255, message = "La dirección de contacto no puede exceder 255 caracteres")
    private String direccion;

    @Size(max = 255, message = "El nombre comercial no puede exceder 255 caracteres")
    private String nombreComercial;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

    private Set<Long> actividadIds;

    private Long version;
}
