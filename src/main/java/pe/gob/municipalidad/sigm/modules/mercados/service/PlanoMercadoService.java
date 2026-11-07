package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPlano;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVersionPlano;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de administración de planos maestros de mercados municipales.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanoMercadoService {

    private final PlanoMercadoRepository planoRepository;
    private final VersionPlanoMercadoRepository versionRepository;
    private final MercadoMunicipalRepository mercadoRepository;
    private final AuditoriaPlanoMercadoRepository auditoriaRepository;

    /**
     * Crea un nuevo plano maestro para un mercado municipal e inicializa su versión 1 en BORRADOR.
     */
    @Transactional
    public PlanoMercadoResponse crearPlano(PlanoMercadoCreateRequest request, String username, UUID usuarioId) {
        log.info("Creando plano '{}' para mercado ID: {}", request.getNombre(), request.getMercadoId());

        MercadoMunicipal mercado = mercadoRepository.findById(request.getMercadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Mercado no encontrado con ID: " + request.getMercadoId()));

        if (planoRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Ya existe un plano con el código: " + request.getCodigo());
        }

        if (request.getAnchoBase().compareTo(BigDecimal.ZERO) <= 0 || request.getAltoBase().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Las dimensiones base del lienzo deben ser mayores a cero.");
        }

        PlanoMercado plano = PlanoMercado.builder()
                .mercado(mercado)
                .codigo(request.getCodigo().trim().toUpperCase())
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .anchoBase(request.getAnchoBase())
                .altoBase(request.getAltoBase())
                .unidadReferencia(request.getUnidadReferencia() != null ? request.getUnidadReferencia() : "PIXEL")
                .estado(EstadoPlano.ACTIVO)
                .build();

        plano = planoRepository.save(plano);

        // Crear automáticamente la versión 1 en estado BORRADOR
        VersionPlanoMercado version1 = VersionPlanoMercado.builder()
                .plano(plano)
                .numeroVersion(1)
                .estado(EstadoVersionPlano.BORRADOR)
                .descripcionCambios("Versión inicial del plano para configuración de distribución.")
                .usuarioCreadorId(usuarioId)
                .usuarioCreadorNombre(username)
                .build();

        version1 = versionRepository.save(version1);

        plano.setVersionActual(version1);
        planoRepository.save(plano);

        // Registrar auditoría
        auditoriaRepository.save(AuditoriaPlanoMercado.builder()
                .planoId(plano.getId())
                .versionPlanoId(version1.getId())
                .tipoEvento("CREACION_PLANO")
                .descripcion("Creación de plano maestro '" + plano.getNombre() + "' con versión inicial 1 en borrador")
                .usuarioId(usuarioId)
                .username(username)
                .build());

        return mapearAResponse(plano, version1);
    }

    /**
     * Obtiene el plano activo de un mercado municipal.
     */
    @Transactional(readOnly = true)
    public Optional<PlanoMercadoResponse> obtenerPlanoActivoPorMercado(UUID mercadoId) {
        return planoRepository.findActivoPorMercadoConDetalle(mercadoId)
                .map(p -> mapearAResponse(p, p.getVersionActual()));
    }

    /**
     * Obtiene un plano por su ID.
     */
    @Transactional(readOnly = true)
    public PlanoMercadoResponse obtenerPorId(UUID planoId) {
        PlanoMercado plano = planoRepository.findByIdConDetalle(planoId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano no encontrado con ID: " + planoId));
        return mapearAResponse(plano, plano.getVersionActual());
    }

    /**
     * Actualiza metadatos del plano (nombre, descripción, dimensiones base).
     */
    @Transactional
    public PlanoMercadoResponse actualizarPlano(UUID planoId, PlanoMercadoUpdateRequest request, String username, UUID usuarioId) {
        PlanoMercado plano = planoRepository.findByIdConDetalle(planoId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano no encontrado con ID: " + planoId));

        if (request.getAnchoBase().compareTo(BigDecimal.ZERO) <= 0 || request.getAltoBase().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Las dimensiones base deben ser mayores a cero.");
        }

        plano.setNombre(request.getNombre().trim());
        plano.setDescripcion(request.getDescripcion());
        plano.setAnchoBase(request.getAnchoBase());
        plano.setAltoBase(request.getAltoBase());

        plano = planoRepository.save(plano);

        auditoriaRepository.save(AuditoriaPlanoMercado.builder()
                .planoId(plano.getId())
                .tipoEvento("ACTUALIZACION_CONFIGURACION")
                .descripcion("Actualización de configuración base del plano: " + plano.getNombre())
                .usuarioId(usuarioId)
                .username(username)
                .build());

        return mapearAResponse(plano, plano.getVersionActual());
    }

    /**
     * Lista todos los planos paginados.
     */
    @Transactional(readOnly = true)
    public Page<PlanoMercadoResponse> listarPlanos(Pageable pageable) {
        return planoRepository.findAll(pageable)
                .map(p -> mapearAResponse(p, p.getVersionActual()));
    }

    /**
     * Lista todos los planos del sistema.
     */
    @Transactional(readOnly = true)
    public List<PlanoMercadoResponse> listarTodos() {
        return planoRepository.findAll().stream()
                .map(p -> mapearAResponse(p, p.getVersionActual()))
                .collect(Collectors.toList());
    }

    private PlanoMercadoResponse mapearAResponse(PlanoMercado plano, VersionPlanoMercado versionActual) {
        return PlanoMercadoResponse.builder()
                .id(plano.getId())
                .mercadoId(plano.getMercado() != null ? plano.getMercado().getId() : null)
                .mercadoNombre(plano.getMercado() != null ? plano.getMercado().getNombre() : null)
                .mercadoCodigo(plano.getMercado() != null ? plano.getMercado().getCodigo() : null)
                .codigo(plano.getCodigo())
                .nombre(plano.getNombre())
                .descripcion(plano.getDescripcion())
                .anchoBase(plano.getAnchoBase())
                .altoBase(plano.getAltoBase())
                .unidadReferencia(plano.getUnidadReferencia())
                .estado(plano.getEstado())
                .versionActualId(versionActual != null ? versionActual.getId() : null)
                .versionActualNumero(versionActual != null ? versionActual.getNumeroVersion() : null)
                .versionActualEstado(versionActual != null ? versionActual.getEstado().name() : null)
                .totalVersiones(plano.getVersiones() != null ? plano.getVersiones().size() : 1)
                .updatedAt(plano.getUpdatedAt())
                .build();
    }
}
