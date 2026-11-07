package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ArbitrioFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoArbitrioCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ConfiguracionArbitrioCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.SimulacionArbitrioRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ServicioArbitrioMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConfiguracionArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoServicioArbitrio;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ArbitrioMunicipalService {

    // Catálogo de Servicios
    List<ServicioArbitrioMunicipal> listarServiciosActivos();
    ServicioArbitrioMunicipal obtenerServicioPorId(UUID id);

    // Configuraciones
    Page<ConfiguracionArbitrioSummaryDto> listarConfiguraciones(
            String query, Integer anio, UUID servicioId, TipoServicioArbitrio tipoServicio, EstadoConfiguracionArbitrio estado, Pageable pageable);
    ConfiguracionArbitrioDetailResponse obtenerConfiguracionPorId(UUID id);
    ConfiguracionArbitrioDetailResponse registrarConfiguracion(ConfiguracionArbitrioCreateRequest request, String usuario);
    ConfiguracionArbitrioDetailResponse enviarConfiguracionARevision(UUID id, String motivo, String usuario);
    ConfiguracionArbitrioDetailResponse aprobarConfiguracion(UUID id, String resolucionAprobacion, String usuario);
    ConfiguracionArbitrioDetailResponse activarConfiguracion(UUID id, String usuario);
    ConfiguracionArbitrioDetailResponse desactivarConfiguracion(UUID id, String motivo, String usuario);
    ConfiguracionArbitrioDetailResponse crearNuevaVersionConfiguracion(UUID id, ConfiguracionArbitrioCreateRequest request, String motivo, String usuario);
    List<ConfiguracionArbitrioSummaryDto> listarConfiguracionesActivas();

    default List<ServicioArbitrioMunicipal> listarServicios() {
        return listarServiciosActivos();
    }

    // Simulación y Cálculo
    ArbitrioDetailResponse simularCalculo(SimulacionArbitrioRequest request, String usuario);
    ArbitrioDetailResponse registrarCalculoPreliminar(CalculoArbitrioCreateRequest request, String usuario);

    default ArbitrioDetailResponse crearCalculoPreliminar(CalculoArbitrioCreateRequest request, String usuario) {
        return registrarCalculoPreliminar(request, usuario);
    }

    ArbitrioDetailResponse obtenerCalculoPorId(UUID id);
    ArbitrioDetailResponse obtenerCalculoPorCodigo(String codigo);
    Page<ArbitrioSummaryDto> listarCalculos(ArbitrioFilterDto filtro, Pageable pageable);

    // Ciclo de Vida del Cálculo
    ArbitrioDetailResponse enviarCalculoARevision(UUID id, String motivo, String usuario);
    ArbitrioDetailResponse registrarObservacionesCalculo(UUID id, String motivo, String usuario);
    ArbitrioDetailResponse aprobarCalculo(UUID id, String resolucionAprobacion, String motivo, String usuario);
    ArbitrioDetailResponse anularCalculo(UUID id, String motivo, String usuario);
    ArbitrioDetailResponse recalcularNuevaVersion(UUID id, String motivo, String usuario);

    ArbitrioDetailResponse aprobarCalculo(UUID id, String usuario);
    ArbitrioDetailResponse recalcular(UUID id, String motivo, String usuario);
    ArbitrioDetailResponse cambiarEstado(UUID id, pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CambioEstadoArbitrioRequest request, String usuario);

    // Historial y Estadísticas
    List<HistorialArbitrioResponse> obtenerHistorial(UUID calculoId);
    Map<String, Object> obtenerEstadisticas();
}
