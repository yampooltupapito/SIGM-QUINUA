package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionEvaluacionResponse {

    private Long id;
    private String tipoEvaluacion;
    private String resultado;
    private String motivo;
    private Long documentoSustentoId;
    private String documentoSustentoNombre;
    private String usuarioEvaluador;
    private OffsetDateTime fechaEvaluacion;
}
