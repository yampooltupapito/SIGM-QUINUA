package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoPatrullajeSerenazgo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrullajeCrearRequest {

    @NotNull(message = "El tipo de patrullaje es obligatorio")
    private TipoPatrullajeSerenazgo tipoPatrullaje;

    @NotNull(message = "El sector de patrullaje es obligatorio")
    private UUID sectorId;

    private UUID zonaId;

    private UUID rutaId;

    private UUID programacionServicioId;

    @NotNull(message = "La fecha y hora de inicio programada es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaHoraInicioProgramada;

    @NotNull(message = "La fecha y hora de fin programada es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaHoraFinProgramada;

    @NotNull(message = "Debe designar un efectivo de serenazgo responsable")
    private UUID responsableId;

    private UUID unidadMovilId;

    @Builder.Default
    private List<UUID> participantesIds = new ArrayList<>();

    @Builder.Default
    private List<UUID> equipamientoIds = new ArrayList<>();

    @NotBlank(message = "El objetivo operativo del patrullaje es obligatorio")
    @Size(max = 1000, message = "El objetivo no puede exceder 1000 caracteres")
    private String objetivoOperativo;

    private String descripcion;

    private String observaciones;

    @Builder.Default
    private Boolean esExtraordinario = false;

    private String motivoExtraordinario;
}
