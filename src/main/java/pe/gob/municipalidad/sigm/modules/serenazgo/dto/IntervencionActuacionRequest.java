package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoActuacionIntervencion;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionActuacionRequest {

    @NotNull(message = "Debe indicar el tipo de actuación")
    private TipoActuacionIntervencion tipoActuacion;

    @NotBlank(message = "Debe describir la actuación preventiva realizada")
    @Size(min = 5, max = 2000, message = "La descripción debe tener entre 5 y 2000 caracteres")
    private String descripcion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaHora;

    private UUID personalResponsableId;

    private String observaciones;
}
