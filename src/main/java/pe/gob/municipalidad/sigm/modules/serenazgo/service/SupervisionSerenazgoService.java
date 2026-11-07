package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.supervision.*;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;

import java.util.List;
import java.util.UUID;

public interface SupervisionSerenazgoService {

    IndicadoresSupervisionResponse obtenerIndicadores(FiltroSupervisionRequest filtro, CustomUserDetails user);

    List<BandejaPendienteItemDto> obtenerBandejaPendientes(FiltroSupervisionRequest filtro, CustomUserDetails user);

    GraficosSupervisionDto obtenerGraficos(FiltroSupervisionRequest filtro, CustomUserDetails user);

    Page<SeguimientoResponse> listarSeguimientos(FiltroSupervisionRequest filtro, Pageable pageable, CustomUserDetails user);

    SeguimientoDetalleResponse obtenerDetalleSeguimiento(UUID id, CustomUserDetails user);

    SeguimientoResponse crearSeguimiento(CrearSeguimientoRequest req, CustomUserDetails user, String ip);

    SeguimientoResponse asignarResponsable(UUID id, AsignarResponsableRequest req, CustomUserDetails user, String ip);

    SeguimientoResponse registrarObservacion(UUID id, RegistrarObservacionRequest req, CustomUserDetails user, String ip);

    SeguimientoResponse registrarSubsanacion(UUID id, RegistrarSubsanacionRequest req, CustomUserDetails user, String ip);

    SeguimientoResponse revisarRespuesta(UUID id, RevisarRespuestaRequest req, CustomUserDetails user, String ip);

    SeguimientoResponse escalarSeguimiento(UUID id, EscalarSeguimientoRequest req, CustomUserDetails user, String ip);

    SeguimientoResponse resolverSeguimiento(UUID id, ResolverSeguimientoRequest req, CustomUserDetails user, String ip);

    SeguimientoResponse cerrarSeguimiento(UUID id, CerrarSeguimientoRequest req, CustomUserDetails user, String ip);

    SeguimientoResponse cancelarSeguimiento(UUID id, CancelarSeguimientoRequest req, CustomUserDetails user, String ip);

    Resource exportarExcel(FiltroSupervisionRequest filtro, CustomUserDetails user);

    Resource exportarPdf(FiltroSupervisionRequest filtro, CustomUserDetails user);

    Resource generarFichaSeguimientoPdf(UUID id, CustomUserDetails user);
}
