package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoRepresentanteLegal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoRepresentacionLegal;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepresentanteResponse {
    private UUID id;
    private String tipoDocumento;
    private String numeroDocumento;
    private String numeroDocumentoEnmascarado;
    private String nombresApellidos;
    private TipoRepresentacionLegal tipoRepresentacion;
    private String tipoRepresentacionDesc;
    private String partidaRegistral;
    private String asientoRegistral;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoRepresentanteLegal estado;
    private String estadoDesc;
    private String badgeClass;
    private String observaciones;
}
