package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mercados.dto.IncidenciaCreateRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.IncidenciaResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteIncidenciaRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteIncidenciaResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.entity.IncidenciaMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.OrigenIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PrioridadIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.repository.IncidenciaMercadoRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PortalComercianteIncidenciaService {

    private final ComerciantePortalContextService contextService;
    private final IncidenciaMercadoRepository incidenciaRepository;
    private final IncidenciaMercadoService incidenciaMercadoService;

    public PortalComercianteIncidenciaService(
            ComerciantePortalContextService contextService,
            IncidenciaMercadoRepository incidenciaRepository,
            IncidenciaMercadoService incidenciaMercadoService) {
        this.contextService = contextService;
        this.incidenciaRepository = incidenciaRepository;
        this.incidenciaMercadoService = incidenciaMercadoService;
    }

    @Transactional(readOnly = true)
    public Page<PortalComercianteIncidenciaResponse> listarMisIncidencias(EstadoIncidencia estado, Pageable pageable) {
        Comerciante comerciante = contextService.getComercianteActivo();
        List<IncidenciaMercado> todas = incidenciaRepository.findByComercianteIdOrderByFechaReporteDesc(comerciante.getId());

        List<PortalComercianteIncidenciaResponse> filtradas = todas.stream()
                .filter(i -> estado == null || i.getEstado() == estado)
                .map(this::mapearIncidencia)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtradas.size());
        List<PortalComercianteIncidenciaResponse> paged = start <= end ? filtradas.subList(start, end) : List.of();

        return new PageImpl<>(paged, pageable, filtradas.size());
    }

    @Transactional(readOnly = true)
    public PortalComercianteIncidenciaResponse obtenerDetalleIncidencia(UUID incidenciaId) {
        Comerciante comerciante = contextService.getComercianteActivo();
        IncidenciaMercado i = contextService.validarPertenenciaIncidencia(incidenciaId, comerciante);
        return mapearIncidencia(i);
    }

    @Transactional
    public IncidenciaResponse reportarIncidencia(PortalComercianteIncidenciaRequest req) {
        Usuario usuario = contextService.getUsuarioAutenticado();
        Comerciante comerciante = contextService.getComercianteActivo();

        if (req.getPuestoId() != null) {
            contextService.validarPertenenciaPuesto(req.getPuestoId(), comerciante);
        }

        IncidenciaCreateRequest createReq = IncidenciaCreateRequest.builder()
                .mercadoId(req.getMercadoId())
                .puestoId(req.getPuestoId())
                .comercianteId(comerciante.getId())
                .tipoIncidenciaId(req.getTipoIncidenciaId())
                .origen(OrigenIncidencia.COMUNICACION_CIUDADANA)
                .descripcion(req.getDescripcion())
                .fechaOcurrencia(OffsetDateTime.now())
                .prioridad(PrioridadIncidencia.MEDIA)
                .reportanteNombre(comerciante.getNombreOTitular())
                .reportanteContacto(comerciante.getTelefonoContacto() != null ? comerciante.getTelefonoContacto() : comerciante.getCorreoContacto())
                .esAnonimo(false)
                .build();

        return incidenciaMercadoService.registrar(createReq, usuario.getUsername());
    }

    private PortalComercianteIncidenciaResponse mapearIncidencia(IncidenciaMercado i) {
        return PortalComercianteIncidenciaResponse.builder()
                .incidenciaId(i.getId())
                .codigoIncidencia(i.getCodigoIncidencia())
                .tipoNombre(i.getTipoIncidencia() != null ? i.getTipoIncidencia().getNombre() : "INCIDENCIA GENERAL")
                .mercadoNombre(i.getMercado() != null ? i.getMercado().getNombre() : "-")
                .puestoCodigo(i.getPuesto() != null ? i.getPuesto().getCodigo() : "-")
                .fechaReporte(i.getFechaReporte())
                .estado(i.getEstado())
                .estadoDescripcion(i.getEstado() != null ? i.getEstado().name() : "-")
                .prioridad(i.getPrioridad())
                .prioridadDescripcion(i.getPrioridad() != null ? i.getPrioridad().name() : "-")
                .descripcion(i.getDescripcion())
                .resolucion(i.getMotivoResolucion())
                .build();
    }
}
