package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;

import java.util.List;
import java.util.UUID;

public interface PatrullajeSerenazgoService {

    Page<PatrullajeResponse> listarPatrullajes(PatrullajeFilter filter, Pageable pageable);

    List<PatrullajeResponse> listarPatrullajesParaExportacion(PatrullajeFilter filter);

    PatrullajeDetailResponse obtenerPatrullajePorId(UUID id);

    DashboardPatrullajesResponse obtenerDashboardKpis();

    PatrullajeDetailResponse crearPatrullaje(PatrullajeCrearRequest request, String username);

    PatrullajeDetailResponse asignarRecursos(UUID id, PatrullajeAsignarRequest request, String username);

    PatrullajeDetailResponse iniciarPatrullaje(UUID id, PatrullajeIniciarRequest request, String username);

    PatrullajeDetailResponse registrarPasoPunto(UUID id, PatrullajeRegistrarVisitaRequest request, String username);

    PatrullajeDetailResponse registrarNovedad(UUID id, PatrullajeRegistrarNovedadRequest request, String username);

    PatrullajeDetailResponse pausarPatrullaje(UUID id, PatrullajePausarRequest request, String username);

    PatrullajeDetailResponse reanudarPatrullaje(UUID id, PatrullajeReanudarRequest request, String username);

    PatrullajeDetailResponse cambiarRuta(UUID id, PatrullajeCambioRutaRequest request, String username);

    PatrullajeDetailResponse finalizarPatrullaje(UUID id, PatrullajeFinalizarRequest request, String username);

    PatrullajeDetailResponse cancelarPatrullaje(UUID id, PatrullajeCancelarRequest request, String username);

    List<RutaPatrullajeResponse> listarRutasPorSector(UUID sectorId);

    List<RutaPatrullajeResponse> listarRutasActivas();

    RutaPatrullajeResponse obtenerRutaPorId(UUID rutaId);

    byte[] exportarExcel(PatrullajeFilter filter);

    byte[] generarFichaPdf(UUID id);
}
