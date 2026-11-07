package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mercados.dto.MercadosDtos.DashboardMercadosMetrics;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AsignacionPuestoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ComercianteRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.PuestoComercialRepository;

@Service
public class MercadosDashboardService {

    private final MercadoMunicipalRepository mercadoRepository;
    private final PuestoComercialRepository puestoRepository;
    private final ComercianteRepository comercianteRepository;
    private final AsignacionPuestoRepository asignacionRepository;

    public MercadosDashboardService(
            MercadoMunicipalRepository mercadoRepository,
            PuestoComercialRepository puestoRepository,
            ComercianteRepository comercianteRepository,
            AsignacionPuestoRepository asignacionRepository) {
        this.mercadoRepository = mercadoRepository;
        this.puestoRepository = puestoRepository;
        this.comercianteRepository = comercianteRepository;
        this.asignacionRepository = asignacionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardMercadosMetrics obtenerMetricas() {
        long totalMercados = mercadoRepository.count();
        long mercadosActivos = mercadoRepository.countByEstado(EstadoMercado.ACTIVO);

        long totalPuestos = puestoRepository.count();
        long puestosDisponibles = puestoRepository.countByEstado(EstadoPuesto.DISPONIBLE);
        long puestosOcupados = puestoRepository.countByEstado(EstadoPuesto.OCUPADO);
        long puestosMantenimiento = puestoRepository.countByEstado(EstadoPuesto.EN_MANTENIMIENTO);

        long totalComerciantes = comercianteRepository.count();
        long comerciantesActivos = comercianteRepository.countByEstado(EstadoComerciante.ACTIVO);

        long asignacionesVigentes = asignacionRepository.countByEstado(EstadoAsignacion.VIGENTE);

        return DashboardMercadosMetrics.builder()
                .totalMercados(totalMercados)
                .mercadosActivos(mercadosActivos)
                .totalPuestos(totalPuestos)
                .puestosDisponibles(puestosDisponibles)
                .puestosOcupados(puestosOcupados)
                .puestosMantenimiento(puestosMantenimiento)
                .totalComerciantes(totalComerciantes)
                .comerciantesActivos(comerciantesActivos)
                .asignacionesVigentes(asignacionesVigentes)
                .build();
    }
}
