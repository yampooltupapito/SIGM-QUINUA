package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTerritorialResponse {
    private long totalSectores;
    private long sectoresActivos;
    private long totalZonas;
    private long zonasActivas;
    private long sectoresConDelimitacion;
    private long zonasSinDelimitacion;
}
