package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoObservacionGestion;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObservacionGestionResponse {

    private Long id;
    private String descripcion;
    private String requisitoRelacionado;
    private OffsetDateTime fechaObservacion;
    private String usuarioObservador;
    private EstadoObservacionGestion estado;
    private String estadoLabel;
    private Long documentoId;
    private List<SubsanacionGestionResponse> subsanaciones;
}
