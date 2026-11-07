package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.ResultadoIntervencionSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoIntervencionSerenazgo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionCrearRequest {

    @NotNull(message = "Debe asociar la intervención a una incidencia registrada")
    private UUID incidenciaId;

    private UUID atencionId;

    @NotNull(message = "Debe clasificar el tipo de intervención preventiva")
    private TipoIntervencionSerenazgo tipoIntervencion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaFin;

    @NotNull(message = "Debe designar al sereno responsable operativo de la actuación")
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

    @NotBlank(message = "Debe ingresar una descripción objetiva de los hechos y actuaciones realizadas")
    @Size(min = 10, max = 5000, message = "La descripción debe tener entre 10 y 5000 caracteres")
    private String descripcionActuacion;

    @NotNull(message = "Debe seleccionar un resultado administrativo preventivo")
    private ResultadoIntervencionSerenazgo resultado;

    @NotNull(message = "Debe seleccionar el nivel de confidencialidad institucional")
    @Builder.Default
    private NivelConfidencialidad nivelConfidencialidad = NivelConfidencialidad.PUBLICO;

    @Builder.Default
    private Boolean registroRetrospectivo = false;

    private String motivoRegistroRetrospectivo;

    private String observaciones;

    @Builder.Default
    private Boolean guardarComoBorrador = false;

    @Builder.Default
    private List<UUID> personalParticipanteIds = new ArrayList<>();

    @Builder.Default
    private List<String> funcionesParticipantes = new ArrayList<>();

    @Builder.Default
    private List<UUID> equipamientoIds = new ArrayList<>();
}
