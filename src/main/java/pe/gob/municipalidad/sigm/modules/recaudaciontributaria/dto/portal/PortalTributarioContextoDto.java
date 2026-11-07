package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoVinculacionContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoVinculacionContribuyente;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO que representa un perfil de contribuyente vinculado disponible para el ciudadano.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalTributarioContextoDto {
    private UUID vinculacionId;
    private UUID contribuyenteId;
    private String codigoContribuyente;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombreRazonSocial;
    private TipoVinculacionContribuyente tipoVinculacion;
    private String tipoVinculacionEtiqueta;
    private EstadoVinculacionContribuyente estado;
    private LocalDate vigenciaHasta;
    private boolean esActivo;
    private boolean esTitular;
}

