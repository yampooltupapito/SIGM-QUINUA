package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoBeneficioArbitrio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulacionArbitrioRequest {

    @NotNull(message = "El contribuyente es obligatorio")
    private UUID contribuyenteId;

    @NotNull(message = "El predio es obligatorio")
    private UUID predioId;

    @NotNull(message = "El período fiscal es obligatorio")
    private UUID periodoId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Builder.Default
    private LocalDate fechaReferencia = LocalDate.now();

    @Builder.Default
    private String subperiodo = "ANUAL";

    @Builder.Default
    private BigDecimal porcentajeCondomino = new BigDecimal("100.00");

    @Builder.Default
    private TipoBeneficioArbitrio beneficio = TipoBeneficioArbitrio.NINGUNO;

    private String resolucionBeneficio;

    @Builder.Default
    private List<UUID> serviciosIds = new ArrayList<>();

    private String observaciones;
}
