package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaRegistroCiudadanoRequest {

    @NotBlank(message = "El título o resumen del reporte es obligatorio")
    @Size(max = 200, message = "El título no puede exceder los 200 caracteres")
    private String titulo;

    @NotNull(message = "Debe clasificar el tipo de situación comunicada")
    private UUID tipoReporteId;

    @NotBlank(message = "La descripción de los hechos comunicados es obligatoria")
    @Size(max = 2000, message = "La descripción no puede exceder los 2000 caracteres")
    private String descripcion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime fechaHoraOcurrencia;

    @NotBlank(message = "La referencia de ubicación es obligatoria para la localización")
    @Size(max = 255, message = "La ubicación no puede exceder los 255 caracteres")
    private String ubicacionReferencia;

    @DecimalMin(value = "-90.0", message = "La latitud mínima permitida es -90.0")
    @DecimalMax(value = "90.0", message = "La latitud máxima permitida es 90.0")
    private BigDecimal latitud;

    @DecimalMin(value = "-180.0", message = "La longitud mínima permitida es -180.0")
    @DecimalMax(value = "180.0", message = "La longitud máxima permitida es 180.0")
    private BigDecimal longitud;

    private UUID sectorId;
    private UUID zonaId;

    @Builder.Default
    private Boolean reportanteAnonimo = false;

    @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
    private String reportanteNombre;

    @Email(message = "El formato de correo electrónico no es válido")
    @Size(max = 120, message = "El correo no puede exceder los 120 caracteres")
    private String reportanteEmail;

    @Size(max = 30, message = "El teléfono no puede exceder los 30 caracteres")
    private String reportanteTelefonoContacto;

    @Size(max = 20, message = "El documento no puede exceder los 20 caracteres")
    private String reportanteDocumento;
}
