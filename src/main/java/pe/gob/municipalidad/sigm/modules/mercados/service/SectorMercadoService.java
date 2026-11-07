package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.entity.SectorCambioEstado;
import pe.gob.municipalidad.sigm.modules.mercados.entity.SectorMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SectorMercadoService {

    private final SectorMercadoRepository sectorRepository;
    private final MercadoMunicipalRepository mercadoRepository;
    private final PabellonMercadoRepository pabellonRepository;
    private final PuestoComercialRepository puestoRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final SectorCambioEstadoRepository cambioEstadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public SectorMercadoService(
            SectorMercadoRepository sectorRepository,
            MercadoMunicipalRepository mercadoRepository,
            PabellonMercadoRepository pabellonRepository,
            PuestoComercialRepository puestoRepository,
            AsignacionPuestoRepository asignacionRepository,
            SectorCambioEstadoRepository cambioEstadoRepository,
            UsuarioRepository usuarioRepository,
            AuditoriaAdministrativaService auditoriaService) {
        this.sectorRepository = sectorRepository;
        this.mercadoRepository = mercadoRepository;
        this.pabellonRepository = pabellonRepository;
        this.puestoRepository = puestoRepository;
        this.asignacionRepository = asignacionRepository;
        this.cambioEstadoRepository = cambioEstadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public SectorResponse crearSector(SectorRequest request) {
        if (request.getMercadoId() == null) {
            throw new InvalidRequestException("El ID del mercado es obligatorio.");
        }
        if (request.getCodigo() == null || request.getCodigo().isBlank()) {
            throw new InvalidRequestException("El código del sector es obligatorio.");
        }
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new InvalidRequestException("El nombre del sector es obligatorio.");
        }

        MercadoMunicipal mercado = mercadoRepository.findById(request.getMercadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + request.getMercadoId()));

        String codigoNormalizado = request.getCodigo().trim().toUpperCase();
        if (sectorRepository.existsByMercadoIdAndCodigo(mercado.getId(), codigoNormalizado)) {
            throw new DuplicateResourceException("El código de sector '" + codigoNormalizado + "' ya existe en este mercado.");
        }

        SectorMercado sector = SectorMercado.builder()
                .mercado(mercado)
                .codigo(codigoNormalizado)
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null)
                .tipoSector(request.getTipoSector() != null && !request.getTipoSector().isBlank() ? request.getTipoSector().trim().toUpperCase() : "GENERAL")
                .categoriaComercial(request.getCategoriaComercial() != null && !request.getCategoriaComercial().isBlank() ? request.getCategoriaComercial().trim() : null)
                .estado(request.getEstado() != null && !request.getEstado().isBlank() ? request.getEstado().trim().toUpperCase() : "ACTIVO")
                .ordenVisual(request.getOrdenVisual() != null ? request.getOrdenVisual() : 0)
                .build();

        SectorMercado guardado = sectorRepository.save(sector);
        log.info("Sector creado: {} en mercado {}", guardado.getCodigo(), mercado.getCodigo());

        auditoriaService.registrar("CREAR_SECTOR_MERCADO", "mercados_sectores", null, "EXITO",
                "Creado sector " + guardado.getCodigo() + " en mercado " + mercado.getCodigo());

        return toResponse(guardado);
    }

    @Transactional
    public SectorResponse actualizarSector(UUID id, UUID mercadoId, SectorActualizacionRequest request) {
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new InvalidRequestException("El nombre del sector es obligatorio.");
        }

        SectorMercado sector = sectorRepository.findByIdAndMercadoId(id, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado con ID: " + id + " en el mercado especificado"));

        if (request.getVersion() != null && !request.getVersion().equals(sector.getVersion())) {
            throw new ConflictException("El sector ha sido modificado concurrentemente por otro usuario. Recargue la página para ver los cambios.");
        }

        sector.setNombre(request.getNombre().trim());
        sector.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        if (request.getTipoSector() != null && !request.getTipoSector().isBlank()) {
            sector.setTipoSector(request.getTipoSector().trim().toUpperCase());
        }
        if (request.getCategoriaComercial() != null) {
            sector.setCategoriaComercial(request.getCategoriaComercial().trim());
        }
        if (request.getOrdenVisual() != null) {
            sector.setOrdenVisual(request.getOrdenVisual());
        }

        try {
            SectorMercado actualizado = sectorRepository.saveAndFlush(sector);
            auditoriaService.registrar("EDITAR_SECTOR_MERCADO", "mercados_sectores", null, "EXITO",
                    "Actualizado sector " + actualizado.getCodigo() + " en mercado " + sector.getMercado().getCodigo());
            return toResponse(actualizado);
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException("Conflicto de concurrencia al actualizar el sector. Los datos fueron modificados por otra transacción.");
        }
    }

    // Sobrecarga para compatibilidad previa
    @Transactional
    public SectorResponse actualizarSector(UUID id, SectorRequest request) {
        SectorMercado sector = sectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado con ID: " + id));

        SectorActualizacionRequest actReq = SectorActualizacionRequest.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .tipoSector(request.getTipoSector())
                .categoriaComercial(request.getCategoriaComercial())
                .ordenVisual(request.getOrdenVisual())
                .version(sector.getVersion())
                .build();

        return actualizarSector(id, sector.getMercado().getId(), actReq);
    }

    @Transactional
    public SectorResponse cambiarEstado(UUID id, UUID mercadoId, CambioEstadoEstructuraRequest request) {
        if (request.getNuevoEstado() == null) {
            throw new InvalidRequestException("El nuevo estado es obligatorio.");
        }
        if (request.getMotivo() == null || request.getMotivo().isBlank()) {
            throw new InvalidRequestException("El motivo del cambio de estado es obligatorio.");
        }

        SectorMercado sector = sectorRepository.findByIdAndMercadoId(id, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado con ID: " + id + " en el mercado especificado"));

        EstadoMercado estadoAnterior;
        try {
            estadoAnterior = EstadoMercado.valueOf(sector.getEstado().toUpperCase());
        } catch (Exception e) {
            estadoAnterior = EstadoMercado.ACTIVO;
        }

        if (estadoAnterior == request.getNuevoEstado()) {
            throw new InvalidRequestException("El sector ya se encuentra en estado " + request.getNuevoEstado().getDescripcion() + ".");
        }

        // Validación y advertencia de dependencias activas (puestos ocupados / asignaciones vigentes)
        long puestosOcupados = puestoRepository.countBySectorIdAndEstado(sector.getId(), EstadoPuesto.OCUPADO);
        long asignacionesVigentes = asignacionRepository.countByPuestoSectorIdAndEstado(sector.getId(), EstadoAsignacion.VIGENTE);

        if ((request.getNuevoEstado() == EstadoMercado.INACTIVO || request.getNuevoEstado() == EstadoMercado.SUSPENDIDO)
                && (puestosOcupados > 0 || asignacionesVigentes > 0)) {
            log.warn("ADVERTENCIA OPERATIVA: Desactivando sector {} con {} puestos ocupados y {} asignaciones vigentes",
                    sector.getCodigo(), puestosOcupados, asignacionesVigentes);
        }

        sector.setEstado(request.getNuevoEstado().name());
        SectorMercado actualizado = sectorRepository.save(sector);

        // Registro de historial inmutable
        String username = getAuthenticatedUsername();
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        SectorCambioEstado historial = SectorCambioEstado.builder()
                .sector(actualizado)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(request.getNuevoEstado())
                .motivo(request.getMotivo().trim())
                .usuarioResponsable(usuario)
                .usuarioUsername(username)
                .build();
        cambioEstadoRepository.save(historial);

        auditoriaService.registrar("CAMBIO_ESTADO_SECTOR", "mercados_sectores", null, "EXITO",
                "Estado del sector " + actualizado.getCodigo() + " modificado de " + estadoAnterior + " a " + request.getNuevoEstado() +
                ". Motivo: " + request.getMotivo());

        return toResponse(actualizado);
    }

    @Transactional
    public void reordenarSectores(UUID mercadoId, List<ReordenarItemRequest> items) {
        if (!mercadoRepository.existsById(mercadoId)) {
            throw new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + mercadoId);
        }
        if (items == null || items.isEmpty()) {
            return;
        }

        for (ReordenarItemRequest item : items) {
            SectorMercado sector = sectorRepository.findByIdAndMercadoId(item.getId(), mercadoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sector " + item.getId() + " no pertenece al mercado indicado."));
            sector.setOrdenVisual(item.getNuevoOrden());
            sectorRepository.save(sector);
        }

        auditoriaService.registrar("REORDENAR_SECTORES", "mercados_sectores", null, "EXITO",
                "Reordenamiento de sectores ejecutado en mercado " + mercadoId);
    }

    @Transactional(readOnly = true)
    public Page<SectorResponse> listarSectores(UUID mercadoId, String termino, String estado, String categoria, Pageable pageable) {
        if (!mercadoRepository.existsById(mercadoId)) {
            throw new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + mercadoId);
        }

        String estadoFiltro = (estado != null && !estado.isBlank() && !estado.equalsIgnoreCase("TODOS")) ? estado.trim().toUpperCase() : null;
        String categoriaFiltro = (categoria != null && !categoria.isBlank() && !categoria.equalsIgnoreCase("TODAS")) ? categoria.trim() : null;
        String terminoFiltro = (termino != null && !termino.isBlank()) ? termino.trim() : null;

        Page<SectorMercado> pagina;
        if (terminoFiltro != null) {
            pagina = sectorRepository.filtrarConTermino(mercadoId, estadoFiltro, categoriaFiltro, terminoFiltro, pageable);
        } else {
            pagina = sectorRepository.filtrarSinTermino(mercadoId, estadoFiltro, categoriaFiltro, pageable);
        }

        return pagina.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<SectorResponse> listarPorMercado(UUID mercadoId) {
        if (!mercadoRepository.existsById(mercadoId)) {
            throw new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + mercadoId);
        }
        return sectorRepository.findByMercadoIdOrderByOrdenVisualAsc(mercadoId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SectorResponse obtenerPorId(UUID id) {
        SectorMercado sector = sectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado con ID: " + id));
        return toResponse(sector);
    }

    @Transactional(readOnly = true)
    public SectorResponse obtenerPorIdYMercado(UUID id, UUID mercadoId) {
        SectorMercado sector = sectorRepository.findByIdAndMercadoId(id, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado con ID: " + id + " en el mercado especificado"));
        return toResponse(sector);
    }

    @Transactional(readOnly = true)
    public List<HistorialCambioEstadoResponse> consultarHistorial(UUID sectorId, UUID mercadoId) {
        if (!sectorRepository.existsByIdAndMercadoId(sectorId, mercadoId)) {
            throw new ResourceNotFoundException("Sector no encontrado con ID: " + sectorId + " en el mercado especificado");
        }

        return cambioEstadoRepository.findBySectorIdOrderByFechaCambioDesc(sectorId)
                .stream()
                .map(h -> HistorialCambioEstadoResponse.builder()
                        .id(h.getId())
                        .estadoAnterior(h.getEstadoAnterior())
                        .estadoNuevo(h.getEstadoNuevo())
                        .motivo(h.getMotivo())
                        .usuarioResponsable(h.getUsuarioUsername())
                        .fechaCambio(h.getFechaCambio())
                        .build())
                .collect(Collectors.toList());
    }

    public SectorResponse toResponse(SectorMercado s) {
        long totalPabellones = pabellonRepository.countBySectorId(s.getId());
        long totalPuestos = puestoRepository.countBySectorId(s.getId());

        return SectorResponse.builder()
                .id(s.getId())
                .mercadoId(s.getMercado().getId())
                .mercadoNombre(s.getMercado().getNombre())
                .codigo(s.getCodigo())
                .nombre(s.getNombre())
                .descripcion(s.getDescripcion())
                .tipoSector(s.getTipoSector())
                .categoriaComercial(s.getCategoriaComercial())
                .estado(s.getEstado())
                .ordenVisual(s.getOrdenVisual())
                .totalPabellones(totalPabellones)
                .totalPuestos(totalPuestos)
                .version(s.getVersion())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "SISTEMA";
    }
}
