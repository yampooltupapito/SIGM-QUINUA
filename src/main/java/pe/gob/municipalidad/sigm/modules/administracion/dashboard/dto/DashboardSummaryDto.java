package pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que consolida las tarjetas estadísticas institucionales con datos reales de PostgreSQL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {

    // Métricas activas con tablas existentes
    private long totalUsuariosActivos;
    private long totalUsuariosInactivos;
    private long totalUsuariosBloqueados;
    private long totalOficinasActivas;
    private long totalRolesActivos;
    private long totalPersonasRegistradas;
    private long totalTiposActores;

    // Métricas de los 4 Servicios Municipales Oficiales
    @Builder.Default
    private String expedientesRecibidos = "Operativo";
    @Builder.Default
    private String mercadosRegistrados = "Operativo";
    @Builder.Default
    private String incidenciasSerenazgo = "Operativo";
    @Builder.Default
    private String recaudacionTributaria = "Operativo";
}
