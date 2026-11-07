package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionCreateRequest {

    @NotNull(message = "El concepto tributario es obligatorio")
    private UUID conceptoId;

    @NotNull(message = "El período tributario es obligatorio")
    private UUID periodoId;

    private UUID normaId;

    private String descripcionMotivo;

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate fechaInicioVigencia;

    private LocalDate fechaFinVigencia;

    private UUID sectorId;

    private String zona;

    private Long oficinaResponsableId;
}
