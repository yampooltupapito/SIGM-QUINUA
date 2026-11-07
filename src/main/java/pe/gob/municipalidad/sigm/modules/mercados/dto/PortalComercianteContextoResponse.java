package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoVinculoComerciante;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalComercianteContextoResponse {
    private UUID comercianteId;
    private String codigoComerciante;
    private String nombreComercialOTitular;
    private TipoComerciante tipoComerciante;
    private TipoVinculoComerciante tipoVinculo;
    private boolean activo;
}
