package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoTurnoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoTurnoSerenazgo;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnoUpdateRequest {

    @NotBlank(message = "El nombre del turno es obligatorio")
    @Size(max = 100, message = "El nombre no debe superar los 100 caracteres")
    private String nombre;

    private String descripcion;

    @Builder.Default
    private TipoTurnoSerenazgo tipoTurno = TipoTurnoSerenazgo.DIURNO;

    @NotNull(message = "La hora de inicio es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime horaFin;

    @Builder.Default
    private Boolean cruzaMedianoche = false;

    @Builder.Default
    private String colorHex = "#2563eb";

    private Long oficinaResponsableId;

    private String observaciones;

    @Builder.Default
    private EstadoTurnoSerenazgo estado = EstadoTurnoSerenazgo.ACTIVO;
}
