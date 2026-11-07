package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReprogramarServicioRequest {

    @NotNull(message = "El nuevo turno es obligatorio")
    private UUID nuevoTurnoId;

    @NotNull(message = "La nueva fecha de servicio es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate nuevaFechaServicio;

    private UUID nuevoSectorId;
    private UUID nuevaZonaId;

    @NotBlank(message = "El motivo de la reprogramación es obligatorio")
    private String motivo;
}
