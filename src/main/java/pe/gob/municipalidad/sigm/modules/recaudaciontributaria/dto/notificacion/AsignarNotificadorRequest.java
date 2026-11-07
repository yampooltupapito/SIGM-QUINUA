package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.notificacion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarNotificadorRequest {

    @NotNull(message = "El notificador responsable es obligatorio")
    private Long notificadorId;

    private LocalDate fechaProgramada;

    @Size(max = 150, message = "El sector o ruta no puede exceder 150 caracteres")
    private String sectorRuta;

    private String motivoReasignacion;

    private String observaciones;
}

