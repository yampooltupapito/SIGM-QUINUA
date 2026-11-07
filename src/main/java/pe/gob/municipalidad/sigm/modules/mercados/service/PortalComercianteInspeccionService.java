package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteInspeccionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.entity.InspeccionMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.InspeccionMercadoRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PortalComercianteInspeccionService {

    private final ComerciantePortalContextService contextService;
    private final InspeccionMercadoRepository inspeccionRepository;

    public PortalComercianteInspeccionService(
            ComerciantePortalContextService contextService,
            InspeccionMercadoRepository inspeccionRepository) {
        this.contextService = contextService;
        this.inspeccionRepository = inspeccionRepository;
    }

    @Transactional(readOnly = true)
    public Page<PortalComercianteInspeccionResponse> listarMisInspecciones(EstadoInspeccion estado, Pageable pageable) {
        Comerciante comerciante = contextService.getComercianteActivo();
        List<InspeccionMercado> todas = inspeccionRepository.findByComercianteIdOrderByFechaProgramadaDesc(comerciante.getId());

        List<PortalComercianteInspeccionResponse> filtradas = todas.stream()
                // No mostrar borradores internos
                .filter(i -> i.getEstado() != null)
                .filter(i -> estado == null || i.getEstado() == estado)
                .map(this::mapearInspeccion)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtradas.size());
        List<PortalComercianteInspeccionResponse> paged = start <= end ? filtradas.subList(start, end) : List.of();

        return new PageImpl<>(paged, pageable, filtradas.size());
    }

    @Transactional(readOnly = true)
    public PortalComercianteInspeccionResponse obtenerDetalleInspeccion(UUID inspeccionId) {
        Comerciante comerciante = contextService.getComercianteActivo();
        InspeccionMercado i = contextService.validarPertenenciaInspeccion(inspeccionId, comerciante);
        return mapearInspeccion(i);
    }

    private PortalComercianteInspeccionResponse mapearInspeccion(InspeccionMercado i) {
        List<String> observaciones = new ArrayList<>();
        if (i.getObservacionesGenerales() != null && !i.getObservacionesGenerales().isBlank()) {
            observaciones.add(i.getObservacionesGenerales());
        }

        LocalDate fechaReal = i.getFechaInicioReal() != null ? i.getFechaInicioReal().toLocalDate() : null;

        return PortalComercianteInspeccionResponse.builder()
                .inspeccionId(i.getId())
                .codigoInspeccion(i.getCodigoInspeccion())
                .tipoInspeccion(i.getTipoInspeccion())
                .mercadoNombre(i.getMercado() != null ? i.getMercado().getNombre() : "-")
                .puestoCodigo(i.getPuesto() != null ? i.getPuesto().getCodigo() : "-")
                .fechaProgramada(i.getFechaProgramada())
                .fechaEjecucion(fechaReal)
                .estado(i.getEstado())
                .estadoLabel(i.getEstado() != null ? i.getEstado().name() : "-")
                .calificacionFinal(i.getResultadoGeneral() != null ? i.getResultadoGeneral() : "-")
                .puntajeTotal(null)
                .cantidadHallazgosNotificados(observaciones.size())
                .cantidadMedidasPendientes(i.getEstado() == EstadoInspeccion.OBSERVADA ? 1 : 0)
                .observacionesNotificadas(observaciones)
                .build();
    }
}
