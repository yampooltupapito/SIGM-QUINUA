package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.SentidoEvaluacionGestion;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionGestionResponse {

    private Long id;
    private OffsetDateTime fechaEvaluacion;
    private String usuarioEvaluador;
    private SentidoEvaluacionGestion sentidoEvaluacion;
    private String sentidoEvaluacionLabel;
    private String informeTecnico;
    private String observaciones;
    private Long documentoSustentoId;
}
