package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.Builder;
import lombok.Getter;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoRepresentacion;

import java.time.LocalDate;

@Getter
@Builder
public class RepresentanteResponse {
    private Long id;
    private Long personaId;
    private String dni;
    private String nombreCompleto;
    private TipoRepresentacion tipoRepresentacion;
    private String tipoRepresentacionDescripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean vigente;
    private Long documentoSustentoId;
    private String documentoSustentoCodigo;
    private String estado;
}
