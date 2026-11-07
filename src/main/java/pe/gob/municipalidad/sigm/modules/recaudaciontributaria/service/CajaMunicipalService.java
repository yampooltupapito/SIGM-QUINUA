package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCajaMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConciliacionCaja;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoSesionCaja;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CajaMunicipalService {

    Page<CajaResponse> listarCajas(String busqueda, EstadoCajaMunicipal estado, Pageable pageable);

    List<CajaResponse> listarCajasActivas();

    CajaResponse obtenerCajaPorId(UUID id);

    CajaResponse crearCaja(CrearCajaRequest request, String usuario);

    CajaResponse actualizarEstadoCaja(UUID id, EstadoCajaMunicipal nuevoEstado, String usuario);

    SesionCajaResponse abrirSesion(AbrirSesionCajaRequest request, String usuario);

    SesionCajaResponse cerrarSesion(UUID sesionId, CerrarSesionCajaRequest request, String usuario);

    SesionCajaResponse obtenerSesionPorId(UUID sesionId);

    Optional<SesionCajaResponse> obtenerSesionActivaPorCaja(UUID cajaId);

    Optional<SesionCajaResponse> obtenerSesionActivaPorCajero(Long cajeroId);

    Page<SesionCajaResponse> listarSesiones(String busqueda, UUID cajaId, Long cajeroId, EstadoSesionCaja estado, OffsetDateTime fechaDesde, OffsetDateTime fechaHasta, Pageable pageable);

    ConciliacionCajaResponse conciliarSesion(ConciliarSesionRequest request, String usuario);

    ConciliacionCajaResponse aprobarConciliacion(UUID conciliacionId, String observaciones, String usuario);

    Page<ConciliacionCajaResponse> listarConciliaciones(String busqueda, UUID sesionCajaId, EstadoConciliacionCaja estado, OffsetDateTime fechaDesde, OffsetDateTime fechaHasta, Pageable pageable);

    ConciliacionCajaResponse obtenerConciliacionPorId(UUID id);

    DashboardCajaResponse obtenerMetricasDashboard();
}
