package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaClasificarRequest {

    @NotNull(message = "Debe seleccionar el nuevo tipo de reporte")
    private UUID nuevoTipoReporteId;

    @NotBlank(message = "El motivo de la clasificación es obligatorio")
    private String motivo;
}
