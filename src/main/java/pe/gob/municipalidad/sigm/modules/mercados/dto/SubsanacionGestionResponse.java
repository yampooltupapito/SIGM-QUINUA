package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.ResultadoSubsanacionGestion;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubsanacionGestionResponse {

    private Long id;
    private Long observacionId;
    private String descripcion;
    private OffsetDateTime fechaSubsanacion;
    private String usuarioSubsanador;
    private Long documentoId;
    private ResultadoSubsanacionGestion resultadoRevision;
    private String resultadoRevisionLabel;
}
