package pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadAdministrativaSupervision;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoRegistroSupervision;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltroSupervisionRequest {

    @Builder.Default
    private String periodo = "HOY"; // HOY, ULTIMOS_7_DIAS, MES_ACTUAL, PERSONALIZADO

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFin;

    private UUID sectorId;

    private TipoRegistroSupervision tipoRegistro;

    private String estado;

    private PrioridadAdministrativaSupervision prioridad;

    private Long responsableId;

    private String texto;
}
