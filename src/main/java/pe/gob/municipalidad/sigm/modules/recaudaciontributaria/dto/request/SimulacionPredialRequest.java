package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoBeneficioPredial;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulacionPredialRequest {

    @NotNull(message = "El contribuyente es obligatorio para ejecutar el cálculo")
    private UUID contribuyenteId;

    @NotNull(message = "El período fiscal es obligatorio")
    private UUID periodoId;

    private UUID configuracionId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaReferencia;

    @Builder.Default
    private TipoBeneficioPredial beneficio = TipoBeneficioPredial.NINGUNO;

    private String resolucionBeneficio;

    @Builder.Default
    private List<UUID> prediosSeleccionadosIds = new ArrayList<>();

    private String observaciones;
}
