package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoVinculacionContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoVinculacionContribuyente;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VinculacionResponse {
    private UUID id;
    private Long usuarioId;
    private String username;
    private String usuarioNombreCompleto;
    private UUID contribuyenteId;
    private String contribuyenteCodigo;
    private String contribuyenteNombre;
    private String contribuyenteDocumento;
    private TipoVinculacionContribuyente tipoVinculacion;
    private EstadoVinculacionContribuyente estado;
    private OffsetDateTime fechaSolicitud;
    private OffsetDateTime fechaVerificacion;
    private String verificadoPorUsername;
    private Long documentoSustentoId;
    private String documentoSustentoNombre;
    private LocalDate fechaVigenciaHasta;
    private String motivoObservacion;
    private String motivoRechazoRevocacion;
    private boolean activo;
    private boolean vigente;
}

