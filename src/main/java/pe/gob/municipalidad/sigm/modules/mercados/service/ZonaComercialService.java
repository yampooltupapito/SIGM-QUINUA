package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ZonaRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ZonaResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ZonaComercial;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoZona;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ZonaComercialRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ZonaComercialService {

    private final ZonaComercialRepository zonaRepository;
    private final MercadoMunicipalRepository mercadoRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public ZonaComercialService(
            ZonaComercialRepository zonaRepository,
            MercadoMunicipalRepository mercadoRepository,
            AuditoriaAdministrativaService auditoriaService) {
        this.zonaRepository = zonaRepository;
        this.mercadoRepository = mercadoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public ZonaResponse crearZona(ZonaRequest request) {
        if (request.getMercadoId() == null) {
            throw new InvalidRequestException("El ID del mercado es obligatorio.");
        }
        if (request.getCodigo() == null || request.getCodigo().isBlank()) {
            throw new InvalidRequestException("El código de la zona es obligatorio.");
        }
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new InvalidRequestException("El nombre de la zona es obligatorio.");
        }
        if (request.getTipoZona() == null) {
            throw new InvalidRequestException("El tipo de zona es obligatorio.");
        }

        MercadoMunicipal mercado = mercadoRepository.findById(request.getMercadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + request.getMercadoId()));

        String codigoNormalizado = request.getCodigo().trim().toUpperCase();
        if (zonaRepository.existsByMercadoIdAndCodigo(mercado.getId(), codigoNormalizado)) {
            throw new DuplicateResourceException("El código de zona '" + codigoNormalizado + "' ya existe en este mercado.");
        }

        boolean esAsignable = request.getEsAsignable() != null ? request.getEsAsignable() : request.getTipoZona().isAsignableDefecto();
        EstadoMercado estado = request.getEstado() != null ? request.getEstado() : EstadoMercado.ACTIVO;

        ZonaComercial zona = ZonaComercial.builder()
                .mercado(mercado)
                .codigo(codigoNormalizado)
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null)
                .tipoZona(request.getTipoZona())
                .estado(estado)
                .esAsignable(esAsignable)
                .ordenVisual(request.getOrdenVisual() != null ? request.getOrdenVisual() : 0)
                .build();

        ZonaComercial guardada = zonaRepository.save(zona);
        log.info("Zona comercial creada: {} en mercado {}", guardada.getCodigo(), mercado.getCodigo());

        auditoriaService.registrar("CREAR_ZONA_COMERCIAL", "mercados_zonas", null, "EXITO",
                "Creada zona " + guardada.getCodigo() + " (" + guardada.getTipoZona() + ") en mercado " + mercado.getCodigo());

        return toResponse(guardada);
    }

    @Transactional
    public ZonaResponse actualizarZona(UUID id, UUID mercadoId, ZonaRequest request) {
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new InvalidRequestException("El nombre de la zona es obligatorio.");
        }

        ZonaComercial zona = zonaRepository.findByIdAndMercadoId(id, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con ID: " + id + " en el mercado especificado"));

        if (request.getVersion() != null && !request.getVersion().equals(zona.getVersion())) {
            throw new ConflictException("La zona comercial ha sido modificada por otro usuario. Recargue la página.");
        }

        zona.setNombre(request.getNombre().trim());
        zona.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        if (request.getTipoZona() != null) {
            zona.setTipoZona(request.getTipoZona());
        }
        if (request.getEsAsignable() != null) {
            zona.setEsAsignable(request.getEsAsignable());
        }
        if (request.getEstado() != null) {
            zona.setEstado(request.getEstado());
        }
        if (request.getOrdenVisual() != null) {
            zona.setOrdenVisual(request.getOrdenVisual());
        }

        try {
            ZonaComercial actualizada = zonaRepository.saveAndFlush(zona);
            auditoriaService.registrar("EDITAR_ZONA_COMERCIAL", "mercados_zonas", null, "EXITO",
                    "Actualizada zona " + actualizada.getCodigo() + " en mercado " + zona.getMercado().getCodigo());
            return toResponse(actualizada);
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException("Conflicto de concurrencia al actualizar la zona comercial.");
        }
    }

    @Transactional
    public ZonaResponse cambiarEstado(UUID id, UUID mercadoId, EstadoMercado nuevoEstado, String motivo) {
        if (nuevoEstado == null) {
            throw new InvalidRequestException("El nuevo estado es obligatorio.");
        }

        ZonaComercial zona = zonaRepository.findByIdAndMercadoId(id, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con ID: " + id + " en el mercado especificado"));

        EstadoMercado estadoAnterior = zona.getEstado();
        zona.setEstado(nuevoEstado);
        ZonaComercial guardada = zonaRepository.save(zona);

        auditoriaService.registrar("CAMBIO_ESTADO_ZONA", "mercados_zonas", null, "EXITO",
                "Estado de zona " + guardada.getCodigo() + " cambiado de " + estadoAnterior + " a " + nuevoEstado + ". Motivo: " + motivo);

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<ZonaResponse> listarPorMercado(UUID mercadoId) {
        if (!mercadoRepository.existsById(mercadoId)) {
            throw new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + mercadoId);
        }
        return zonaRepository.findByMercadoIdOrderByOrdenVisualAsc(mercadoId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ZonaResponse> listarPaginado(UUID mercadoId, String termino, TipoZona tipoZona, EstadoMercado estado, Boolean esAsignable, Pageable pageable) {
        if (!mercadoRepository.existsById(mercadoId)) {
            throw new ResourceNotFoundException("Mercado municipal no encontrado con ID: " + mercadoId);
        }

        String terminoFiltro = (termino != null && !termino.isBlank()) ? termino.trim() : null;
        Page<ZonaComercial> pagina;

        if (terminoFiltro != null) {
            pagina = zonaRepository.filtrarConTermino(mercadoId, estado, tipoZona, esAsignable, terminoFiltro, pageable);
        } else {
            pagina = zonaRepository.filtrarSinTermino(mercadoId, estado, tipoZona, esAsignable, pageable);
        }

        return pagina.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ZonaResponse obtenerPorIdYMercado(UUID id, UUID mercadoId) {
        ZonaComercial zona = zonaRepository.findByIdAndMercadoId(id, mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con ID: " + id + " en el mercado especificado"));
        return toResponse(zona);
    }

    public ZonaResponse toResponse(ZonaComercial z) {
        return ZonaResponse.builder()
                .id(z.getId())
                .mercadoId(z.getMercado().getId())
                .mercadoNombre(z.getMercado().getNombre())
                .codigo(z.getCodigo())
                .nombre(z.getNombre())
                .descripcion(z.getDescripcion())
                .tipoZona(z.getTipoZona())
                .tipoZonaDescripcion(z.getTipoZona() != null ? z.getTipoZona().getDescripcion() : null)
                .estado(z.getEstado())
                .esAsignable(z.getEsAsignable())
                .ordenVisual(z.getOrdenVisual())
                .version(z.getVersion())
                .createdAt(z.getCreatedAt())
                .updatedAt(z.getUpdatedAt())
                .build();
    }
}
