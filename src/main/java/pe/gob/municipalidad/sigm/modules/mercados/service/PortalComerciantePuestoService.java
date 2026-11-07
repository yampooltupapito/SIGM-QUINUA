package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteAsignacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComerciantePuestoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PuestoComercial;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AsignacionPuestoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.PlanoMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.PuestoComercialRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PortalComerciantePuestoService {

    private final ComerciantePortalContextService contextService;
    private final AsignacionPuestoRepository asignacionRepository;
    private final PuestoComercialRepository puestoRepository;
    private final PlanoMercadoRepository planoRepository;

    public PortalComerciantePuestoService(
            ComerciantePortalContextService contextService,
            AsignacionPuestoRepository asignacionRepository,
            PuestoComercialRepository puestoRepository,
            PlanoMercadoRepository planoRepository) {
        this.contextService = contextService;
        this.asignacionRepository = asignacionRepository;
        this.puestoRepository = puestoRepository;
        this.planoRepository = planoRepository;
    }

    @Transactional(readOnly = true)
    public List<PortalComerciantePuestoResponse> listarMisPuestos() {
        Comerciante comerciante = contextService.getComercianteActivo();
        List<AsignacionPuesto> asignaciones = asignacionRepository
                .findByComercianteIdOrderByFechaInicioDesc(comerciante.getId());

        return asignaciones.stream().map(a -> {
            PuestoComercial p = a.getPuesto();
            UUID planoId = planoRepository.findByMercadoId(p.getMercado().getId())
                    .map(pl -> pl.getId()).orElse(null);

            return PortalComerciantePuestoResponse.builder()
                    .puestoId(p.getId())
                    .codigoPuesto(p.getCodigo())
                    .numeroPuesto(p.getNumero())
                    .mercadoId(p.getMercado().getId())
                    .mercadoNombre(p.getMercado().getNombre())
                    .sectorNombre(p.getSector() != null ? p.getSector().getNombre() : "Sector General")
                    .pabellonNombre(p.getPabellon() != null ? p.getPabellon().getNombre() : "Pabellón General")
                    .tipoPuestoNombre(p.getTipoPuesto() != null ? p.getTipoPuesto().getNombre() : "Estándar")
                    .areaM2(p.getAreaMetrosCuadrados())
                    .estadoPuesto(p.getEstado())
                    .asignacionId(a.getId())
                    .codigoAsignacion(a.getCodigoAsignacion())
                    .estadoAsignacion(a.getEstado())
                    .fechaInicioAsignacion(a.getFechaInicio())
                    .fechaFinAsignacion(a.getFechaFin())
                    .planoMercadoId(planoId)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PortalComerciantePuestoResponse obtenerDetallePuesto(UUID puestoId) {
        Comerciante comerciante = contextService.getComercianteActivo();
        contextService.validarPertenenciaPuesto(puestoId, comerciante);

        PuestoComercial p = puestoRepository.findById(puestoId)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado: " + puestoId));

        AsignacionPuesto asig = asignacionRepository.findByComercianteIdOrderByFechaInicioDesc(comerciante.getId())
                .stream().filter(a -> a.getPuesto().getId().equals(puestoId)).findFirst().orElse(null);

        UUID planoId = planoRepository.findByMercadoId(p.getMercado().getId())
                .map(pl -> pl.getId()).orElse(null);

        return PortalComerciantePuestoResponse.builder()
                .puestoId(p.getId())
                .codigoPuesto(p.getCodigo())
                .numeroPuesto(p.getNumero())
                .mercadoId(p.getMercado().getId())
                .mercadoNombre(p.getMercado().getNombre())
                .sectorNombre(p.getSector() != null ? p.getSector().getNombre() : "Sector General")
                .pabellonNombre(p.getPabellon() != null ? p.getPabellon().getNombre() : "Pabellón General")
                .tipoPuestoNombre(p.getTipoPuesto() != null ? p.getTipoPuesto().getNombre() : "Estándar")
                .areaM2(p.getAreaMetrosCuadrados())
                .estadoPuesto(p.getEstado())
                .asignacionId(asig != null ? asig.getId() : null)
                .codigoAsignacion(asig != null ? asig.getCodigoAsignacion() : null)
                .estadoAsignacion(asig != null ? asig.getEstado() : null)
                .fechaInicioAsignacion(asig != null ? asig.getFechaInicio() : null)
                .fechaFinAsignacion(asig != null ? asig.getFechaFin() : null)
                .planoMercadoId(planoId)
                .build();
    }

    @Transactional(readOnly = true)
    public List<PortalComercianteAsignacionResponse> listarMisAsignaciones() {
        return listarMisAsignaciones(org.springframework.data.domain.PageRequest.of(0, 100)).getContent();
    }

    @Transactional(readOnly = true)
    public Page<PortalComercianteAsignacionResponse> listarMisAsignaciones(Pageable pageable) {
        Comerciante comerciante = contextService.getComercianteActivo();
        Page<AsignacionPuesto> page = asignacionRepository.findByComercianteId(comerciante.getId(), pageable);

        return page.map(a -> PortalComercianteAsignacionResponse.builder()
                .asignacionId(a.getId())
                .codigoAsignacion(a.getCodigoAsignacion())
                .puestoId(a.getPuesto().getId())
                .puestoCodigo(a.getPuesto().getCodigo())
                .mercadoNombre(a.getPuesto().getMercado().getNombre())
                .sectorNombre(a.getPuesto().getSector() != null ? a.getPuesto().getSector().getNombre() : "Sector General")
                .pabellonNombre(a.getPuesto().getPabellon() != null ? a.getPuesto().getPabellon().getNombre() : "Pabellón General")
                .fechaSolicitud(a.getFechaSolicitud())
                .fechaInicio(a.getFechaInicio())
                .fechaFin(a.getFechaFin())
                .estado(a.getEstado())
                .estadoLabel(a.getEstado() != null ? a.getEstado().getDescripcion() : "-")
                .tipoAsignacion(a.getTipoAsignacion())
                .vigente(a.getEstado() != null && a.getEstado().esActiva())
                .build());
    }
}
