package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;

import java.util.UUID;

public interface RecursosSeguridadService {

    DashboardRecursosResponse obtenerDashboardRecursos();

    MantenimientoResponse finalizarMantenimiento(UUID mantenimientoId, FinalizarMantenimientoRequest request, String username);

    IncidenciaTecnicaResponse resolverIncidencia(UUID incidenciaId, ResolverIncidenciaTecnicaRequest request, String username);

    byte[] exportarExcel(UnidadMovilFilter unidadFilter, EquipamientoFilter equipFilter);
}
