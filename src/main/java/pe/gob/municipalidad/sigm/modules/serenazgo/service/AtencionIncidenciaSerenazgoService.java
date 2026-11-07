package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.EquipamientoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.IncidenciaSeguridadCiudadana;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.PersonalSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.UnidadMovilSerenazgo;

import java.util.List;
import java.util.UUID;

public interface AtencionIncidenciaSerenazgoService {

    DashboardAtencionesResponse obtenerMetricas();

    Page<AtencionResponse> listarAtenciones(AtencionFilter filtro, Pageable pageable);

    Page<IncidenciaSeguridadCiudadana> listarIncidenciasPendientes(Pageable pageable);

    AtencionDetailResponse obtenerDetallePorId(UUID id);

    AtencionDetailResponse obtenerDetallePorCodigo(String codigo);

    AtencionDetailResponse crearAtencion(AtencionCrearRequest request, String username);

    AtencionDetailResponse asignarRecursos(UUID atencionId, AtencionAsignarRecursosRequest request, String username);

    AtencionDetailResponse despachar(UUID atencionId, AtencionDespacharRequest request, String username);

    AtencionDetailResponse responderDespacho(UUID atencionId, AtencionAceptarRechazarRequest request, String username);

    AtencionDetailResponse iniciarAtencion(UUID atencionId, AtencionIniciarRequest request, String username);

    AtencionDetailResponse registrarNovedad(UUID atencionId, AtencionNovedadRequest request, String username);

    AtencionDetailResponse pausarEnEspera(UUID atencionId, AtencionPausarReanudarRequest request, String username);

    AtencionDetailResponse reanudarAtencion(UUID atencionId, AtencionPausarReanudarRequest request, String username);

    AtencionDetailResponse reasignar(UUID atencionId, AtencionReasignarRequest request, String username);

    AtencionDetailResponse escalar(UUID atencionId, AtencionEscalarRequest request, String username);

    AtencionDetailResponse finalizarAtencion(UUID atencionId, AtencionFinalizarRequest request, String username);

    AtencionDetailResponse cancelarAtencion(UUID atencionId, AtencionCancelarRequest request, String username);

    default AtencionDetailResponse despacharAtencion(UUID atencionId, AtencionDespacharRequest request, String username) {
        return despachar(atencionId, request, username);
    }

    default AtencionDetailResponse aceptarORechazarAtencion(UUID atencionId, AtencionAceptarRechazarRequest request, String username) {
        return responderDespacho(atencionId, request, username);
    }

    default AtencionDetailResponse pausarAtencion(UUID atencionId, AtencionPausarReanudarRequest request, String username) {
        return pausarEnEspera(atencionId, request, username);
    }

    default AtencionDetailResponse escalarAtencion(UUID atencionId, AtencionEscalarRequest request, String username) {
        return escalar(atencionId, request, username);
    }

    boolean isUnidadDisponible(UUID unidadId);

    boolean isPersonalDisponible(UUID personalId);

    List<PersonalSerenazgo> listarPersonalDisponible();

    List<UnidadMovilSerenazgo> listarUnidadesDisponibles();

    List<EquipamientoSerenazgo> listarEquipamientoDisponible();

    byte[] exportarPdf(AtencionFilter filtro);

    byte[] exportarExcel(AtencionFilter filtro);
}
