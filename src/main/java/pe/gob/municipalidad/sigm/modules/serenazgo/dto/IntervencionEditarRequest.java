package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.ResultadoIntervencionSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoIntervencionSerenazgo;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionEditarRequest {

    private UUID atencionId;

    @NotNull(message = "Debe clasificar el tipo de intervención")
    private TipoIntervencionSerenazgo tipoIntervencion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaFin;

    @NotNull(message = "Debe indicar el sereno responsable operativo")
    private UUID responsableId;

    private UUID unidadMovilId;

    private UUID sectorId;

    private UUID zonaId;

    private String ubicacionDescripcion;

    @DecimalMin(value = "-90.0", message = "La latitud debe ser mayor o igual a -90")
    @DecimalMax(value = "90.0", message = "La latitud debe ser menor o igual a 90")
    private Double latitud;

    @DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
    @DecimalMax(value = "180.0", message = "La longitud debe ser menor o igual a 180")
    private Double longitud;

    @NotBlank(message = "Debe ingresar una descripción objetiva de las actuaciones")
    @Size(min = 10, max = 5000)
    private String descripcionActuacion;

    @NotNull(message = "Debe seleccionar un resultado administrativo")
    private ResultadoIntervencionSerenazgo resultado;

    @NotNull(message = "Debe seleccionar el nivel de confidencialidad")
    private NivelConfidencialidad nivelConfidencialidad;

    private String observaciones;

    private String motivoSubsanacion;
}
