package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PabellonCambioEstado;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PabellonMercado;
import pe.gob.municipalidad.sigm.modules.mercados.entity.SectorMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PabellonMercadoService {

    private final PabellonMercadoRepository pabellonRepository;
    private final MercadoMunicipalRepository mercadoRepository;
    private final SectorMercadoRepository sectorRepository;
    private final PuestoComercialRepository puestoRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final PabellonCambioEstadoRepository cambioEstadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public PabellonMercadoService(
            PabellonMercadoRepository pabellonRepository,
            MercadoMunicipalRepository mercadoRepository,
            SectorMercadoRepository sectorRepository,
            PuestoComercialRepository puestoRepository,
            AsignacionPuestoRepository asignacionRepository,
            PabellonCambioEstadoRepository cambioEstadoRepository,
            UsuarioRepository usuarioRepository,
            AuditoriaAdministrativaService auditoriaService) {
        this.pabellonRepository = pabellonRepository;
        this.mercadoRepository = mercadoRepository;
        this.sectorRepository = sectorRepository;
        this.puestoRepository = puestoRepository;
        this.asignacionRepository = asignacionRepository;
        this.cambioEstadoRepository = cambioEstadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public PabellonResponse crearPabellon(PabellonRequest request) {
        if (request.getMercadoId() == null) {
            throw new InvalidRequestException("El ID del mercado es obligatorio.");
        }
        if (request.getCodigo() == null || request.getCodigo().isBlank()) {
            throw new InvalidRequestException("El código del pabellón es obligatorio.");
        }
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new InvalidRequestException("El nombre del pabellón es obligatorio.");
        }

        MercadoMunicipal mercado = mercadoRepository.findById(request.getMercadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + request.getMercadoId()));

        SectorMercado sector = null;
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado con ID: " + request.getSectorId()));

            // Validación estricta de coherencia: el sector debe pertenecer al mismo mercado
            if (!sector.getMercado().getId().equals(mercado.getId())) {
                throw new BusinessException("Incoherencia estructural: El sector especificado no pertenece al mercado indicado.");
            }
        }

        String codigoNormalizado = request.getCodigo().trim().toUpperCase();
        if (pabellonRepository.existsByMercadoIdAndCodigo(mercado.getId(), codigoNormalizado)) {
            throw new DuplicateResourceException("El código de pabellón '" + codigoNormalizado + "' ya existe en este mercado.");
        }

        PabellonMercado pabellon = PabellonMercado.builder()
                .mercado(mercado)
                .sector(sector)
                .codigo(codigoNormalizado)
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null)
                .nivel(request.getNivel() != null ? request.getNivel() : 1)
                .estado(request.getEstado() != null && !request.getEstado().isBlank() ? request.getEstado().trim().toUpperCase() : "ACTIVO")
                .ordenVisual(request.getOrdenVisual() != null ? request.getOrdenVisual() : 0)
                .build();

        PabellonMercado guardado = pabellonRepository.save(pabellon);
        log.info("Pabellón creado: {} en mercado {}", guardado.getCodigo(), mercado.getCodigo());

        auditoriaService.registrar("CREAR_PABELLON_MERCADO", "mercados_pabellones", null, "EXITO",
                "Creado pabellón " + guardado.getCodigo() + " en mercado " + mercado.getCodigo());

        return toResponse(guardado);
    }

    @Transactional
    public PabellonResponse actualizarPabellon(UUID id, UUID mercadoId, PabellonActualizacionRequest request) {
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new InvalidRequestException("El nombre del pabellón es obligatorio.");
        }

        PabellonMercado pabellon = pabellonRepository.findByIdAndMercadoId(id, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pabellón no encontrado con ID: " + id + " en el mercado especificado"));

        if (request.getVersion() != null && !request.getVersion().equals(pabellon.getVersion())) {
            throw new ConflictException("El pabellón ha sido modificado concurrentemente por otro usuario. Recargue la página para ver los cambios.");
        }

        SectorMercado sector = null;
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado con ID: " + request.getSectorId()));

            if (!sector.getMercado().getId().equals(mercadoId)) {
                throw new BusinessException("Incoherencia estructural: El sector especificado no pertenece al mercado del pabellón.");
            }
        }

        pabellon.setNombre(request.getNombre().trim());
        pabellon.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        pabellon.setSector(sector);
        if (request.getNivel() != null) {
            pabellon.setNivel(request.getNivel());
        }
        if (request.getOrdenVisual() != null) {
            pabellon.setOrdenVisual(request.getOrdenVisual());
        }

        try {
            PabellonMercado actualizado = pabellonRepository.saveAndFlush(pabellon);
            auditoriaService.registrar("EDITAR_PABELLON_MERCADO", "mercados_pabellones", null, "EXITO",
                    "Actualizado pabellón " + actualizado.getCodigo() + " en mercado " + pabellon.getMercado().getCodigo());
            return toResponse(actualizado);
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException("Conflicto de concurrencia al actualizar el pabellón. Los datos fueron modificados por otra transacción.");
        }
    }

    // Sobrecarga para compatibilidad previa
    @Transactional
    public PabellonResponse actualizarPabellon(UUID id, PabellonRequest request) {
        PabellonMercado pabellon = pabellonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pabellón no encontrado con ID: " + id));

        PabellonActualizacionRequest actReq = PabellonActualizacionRequest.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .sectorId(request.getSectorId())
                .nivel(request.getNivel())
                .ordenVisual(request.getOrdenVisual())
                .version(pabellon.getVersion())
                .build();

        return actualizarPabellon(id, pabellon.getMercado().getId(), actReq);
    }

    @Transactional
    public PabellonResponse cambiarEstado(UUID id, UUID mercadoId, CambioEstadoEstructuraRequest request) {
        if (request.getNuevoEstado() == null) {
            throw new InvalidRequestException("El nuevo estado es obligatorio.");
        }
        if (request.getMotivo() == null || request.getMotivo().isBlank()) {
            throw new InvalidRequestException("El motivo del cambio de estado es obligatorio.");
        }

        PabellonMercado pabellon = pabellonRepository.findByIdAndMercadoId(id, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pabellón no encontrado con ID: " + id + " en el mercado especificado"));

        EstadoMercado estadoAnterior;
        try {
            estadoAnterior = EstadoMercado.valueOf(pabellon.getEstado().toUpperCase());
        } catch (Exception e) {
            estadoAnterior = EstadoMercado.ACTIVO;
        }

        if (estadoAnterior == request.getNuevoEstado()) {
            throw new InvalidRequestException("El pabellón ya se encuentra en estado " + request.getNuevoEstado().getDescripcion() + ".");
        }

        long puestosOcupados = puestoRepository.countByPabellonIdAndEstado(pabellon.getId(), EstadoPuesto.OCUPADO);
        long asignacionesVigentes = asignacionRepository.countByPuestoPabellonIdAndEstado(pabellon.getId(), EstadoAsignacion.VIGENTE);

        if ((request.getNuevoEstado() == EstadoMercado.INACTIVO || request.getNuevoEstado() == EstadoMercado.SUSPENDIDO)
                && (puestosOcupados > 0 || asignacionesVigentes > 0)) {
            log.warn("ADVERTENCIA OPERATIVA: Desactivando pabellón {} con {} puestos ocupados y {} asignaciones vigentes",
                    pabellon.getCodigo(), puestosOcupados, asignacionesVigentes);
        }

        pabellon.setEstado(request.getNuevoEstado().name());
        PabellonMercado actualizado = pabellonRepository.save(pabellon);

        String username = getAuthenticatedUsername();
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        PabellonCambioEstado historial = PabellonCambioEstado.builder()
                .pabellon(actualizado)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(request.getNuevoEstado())
                .motivo(request.getMotivo().trim())
                .usuarioResponsable(usuario)
                .usuarioUsername(username)
                .build();
        cambioEstadoRepository.save(historial);

        auditoriaService.registrar("CAMBIO_ESTADO_PABELLON", "mercados_pabellones", null, "EXITO",
                "Estado del pabellón " + actualizado.getCodigo() + " modificado de " + estadoAnterior + " a " + request.getNuevoEstado() +
                ". Motivo: " + request.getMotivo());

        return toResponse(actualizado);
    }

    @Transactional
    public void reordenarPabellones(UUID mercadoId, List<ReordenarItemRequest> items) {
        if (!mercadoRepository.existsById(mercadoId)) {
            throw new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + mercadoId);
        }
        if (items == null || items.isEmpty()) {
            return;
        }

        for (ReordenarItemRequest item : items) {
            PabellonMercado pabellon = pabellonRepository.findByIdAndMercadoId(item.getId(), mercadoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pabellón " + item.getId() + " no pertenece al mercado indicado."));
            pabellon.setOrdenVisual(item.getNuevoOrden());
            pabellonRepository.save(pabellon);
        }

        auditoriaService.registrar("REORDENAR_PABELLONES", "mercados_pabellones", null, "EXITO",
                "Reordenamiento de pabellones ejecutado en mercado " + mercadoId);
    }

    @Transactional(readOnly = true)
    public Page<PabellonResponse> listarPabellones(UUID mercadoId, String termino, UUID sectorId, String estado, Integer nivel, Pageable pageable) {
        if (!mercadoRepository.existsById(mercadoId)) {
            throw new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + mercadoId);
        }

        String estadoFiltro = (estado != null && !estado.isBlank() && !estado.equalsIgnoreCase("TODOS")) ? estado.trim().toUpperCase() : null;
        String terminoFiltro = (termino != null && !termino.isBlank()) ? termino.trim() : null;

        Page<PabellonMercado> pagina;
        if (terminoFiltro != null) {
            pagina = pabellonRepository.filtrarConTermino(mercadoId, sectorId, estadoFiltro, nivel, terminoFiltro, pageable);
        } else {
            pagina = pabellonRepository.filtrarSinTermino(mercadoId, sectorId, estadoFiltro, nivel, pageable);
        }

        return pagina.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<PabellonResponse> listarPorMercado(UUID mercadoId) {
        if (!mercadoRepository.existsById(mercadoId)) {
            throw new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + mercadoId);
        }
        return pabellonRepository.findByMercadoIdOrderByOrdenVisualAsc(mercadoId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PabellonResponse> listarPorSector(UUID sectorId) {
        return pabellonRepository.findBySectorIdOrderByOrdenVisualAsc(sectorId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PabellonResponse obtenerPorId(UUID id) {
        PabellonMercado pabellon = pabellonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pabellón no encontrado con ID: " + id));
        return toResponse(pabellon);
    }

    @Transactional(readOnly = true)
    public PabellonResponse obtenerPorIdYMercado(UUID id, UUID mercadoId) {
        PabellonMercado pabellon = pabellonRepository.findByIdAndMercadoId(id, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pabellón no encontrado con ID: " + id + " en el mercado especificado"));
        return toResponse(pabellon);
    }

    @Transactional(readOnly = true)
    public List<HistorialCambioEstadoResponse> consultarHistorial(UUID pabellonId, UUID mercadoId) {
        if (!pabellonRepository.existsByIdAndMercadoId(pabellonId, mercadoId)) {
            throw new ResourceNotFoundException("Pabellón no encontrado con ID: " + pabellonId + " en el mercado especificado");
        }

        return cambioEstadoRepository.findByPabellonIdOrderByFechaCambioDesc(pabellonId)
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

    public PabellonResponse toResponse(PabellonMercado p) {
        long totalPuestos = puestoRepository.countByPabellonId(p.getId());

        return PabellonResponse.builder()
                .id(p.getId())
                .mercadoId(p.getMercado().getId())
                .mercadoNombre(p.getMercado().getNombre())
                .sectorId(p.getSector() != null ? p.getSector().getId() : null)
                .sectorNombre(p.getSector() != null ? p.getSector().getNombre() : null)
                .codigo(p.getCodigo())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .nivel(p.getNivel())
                .estado(p.getEstado())
                .ordenVisual(p.getOrdenVisual())
                .totalPuestos(totalPuestos)
                .version(p.getVersion())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
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
