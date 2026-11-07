package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDashboardSeguridadResponse {
    private long totalPersonalActivo;
    private long totalUnidadesOperativas;
    private long totalSectoresActivos;
    private long totalZonasActivas;
    private long incidenciasAbiertas;
    private long incidenciasHoy;
    private long alertasPendientes;
    private long patrullajesEnCurso;
    private long intervencionesEnCurso;
}
