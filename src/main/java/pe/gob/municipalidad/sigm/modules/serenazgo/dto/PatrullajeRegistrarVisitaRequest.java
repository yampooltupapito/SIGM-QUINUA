package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoVisitaPunto;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.MetodoRegistroVisita;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrullajeRegistrarVisitaRequest {

    @NotNull(message = "Debe especificar el punto de control visitado u omitido")
    private UUID puntoControlId;

    @NotNull(message = "El estado de la visita es obligatorio")
    @Builder.Default
    private EstadoVisitaPunto estadoVisita = EstadoVisitaPunto.VISITADO;

    @Builder.Default
    private MetodoRegistroVisita metodoRegistro = MetodoRegistroVisita.MANUAL;

    private String motivoOmision;

    private BigDecimal latitudRegistro;

    private BigDecimal longitudRegistro;

    private String observaciones;
}
