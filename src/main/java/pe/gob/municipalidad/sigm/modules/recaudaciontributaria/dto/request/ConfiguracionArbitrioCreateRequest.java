package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MetodoDistribucionArbitrio;

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
public class ConfiguracionArbitrioCreateRequest {

    @NotNull(message = "El servicio de arbitrio es obligatorio")
    private UUID servicioId;

    @NotNull(message = "El período fiscal es obligatorio")
    private UUID periodoId;

    private UUID normaId;
    private UUID sectorId;
    private String zona;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate fechaInicioVigencia;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFinVigencia;

    @NotNull(message = "La metodología de distribución es obligatoria")
    private MetodoDistribucionArbitrio metodologiaDistribucion;

    private String descripcionJustificacion;

    @Builder.Default
    private List<CostoItemRequest> costos = new ArrayList<>();

    @Builder.Default
    private List<CriterioItemRequest> criterios = new ArrayList<>();
}
