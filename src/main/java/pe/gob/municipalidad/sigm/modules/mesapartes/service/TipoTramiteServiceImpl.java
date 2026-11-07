package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.TipoTramiteRequest;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvDtos.TipoTramiteResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.TipoTramite;
import pe.gob.municipalidad.sigm.modules.mesapartes.mapper.MpvMapper;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.TipoTramiteRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TipoTramiteServiceImpl implements TipoTramiteService {

    private static final Logger log = LoggerFactory.getLogger(TipoTramiteServiceImpl.class);

    private final TipoTramiteRepository tipoTramiteRepository;
    private final OficinaRepository oficinaRepository;
    private final MpvMapper mpvMapper;

    public TipoTramiteServiceImpl(TipoTramiteRepository tipoTramiteRepository,
                                  OficinaRepository oficinaRepository,
                                  MpvMapper mpvMapper) {
        this.tipoTramiteRepository = tipoTramiteRepository;
        this.oficinaRepository = oficinaRepository;
        this.mpvMapper = mpvMapper;
    }

    @Override
    @Cacheable(value = "catalogoTiposTramite")
    @Transactional(readOnly = true)
    public List<TipoTramiteResponse> listarActivos() {
        return tipoTramiteRepository.findByEstadoOrderByNombreAsc("ACTIVO")
                .stream()
                .map(mpvMapper::toTipoTramiteResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoTramiteResponse> listarTodos() {
        return tipoTramiteRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(mpvMapper::toTipoTramiteResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TipoTramiteResponse obtenerPorId(Long id) {
        TipoTramite tramite = tipoTramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de trámite no encontrado con ID: " + id));
        return mpvMapper.toTipoTramiteResponse(tramite);
    }

    @Override
    @CacheEvict(value = "catalogoTiposTramite", allEntries = true)
    @Transactional
    public TipoTramiteResponse crearTipoTramite(TipoTramiteRequest request, String username) {
        if (tipoTramiteRepository.existsByCodigoIgnoreCase(request.getCodigo().trim())) {
            throw new BusinessException("Ya existe un tipo de trámite con el código: " + request.getCodigo().trim());
        }

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Oficina no encontrada con ID: " + request.getOficinaResponsableId()));
        }

        TipoTramite nuevo = TipoTramite.builder()
                .codigo(request.getCodigo().trim().toUpperCase())
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .oficinaResponsable(oficina)
                .requiereDocumentos(request.getRequiereDocumentos() != null ? request.getRequiereDocumentos() : true)
                .permitePresentacionVirtual(request.getPermitePresentacionVirtual() != null ? request.getPermitePresentacionVirtual() : true)
                .plazoDias(request.getPlazoDias() != null ? request.getPlazoDias() : 30)
                .unidadPlazo(request.getUnidadPlazo())
                .estado(request.getEstado() != null ? request.getEstado() : "ACTIVO")
                .build();

        TipoTramite guardado = tipoTramiteRepository.save(nuevo);
        log.info("Tipo de trámite creado: {} por {}", guardado.getCodigo(), username);
        return mpvMapper.toTipoTramiteResponse(guardado);
    }

    @Override
    @CacheEvict(value = "catalogoTiposTramite", allEntries = true)
    @Transactional
    public TipoTramiteResponse actualizarTipoTramite(Long id, TipoTramiteRequest request, String username) {
        TipoTramite tramite = tipoTramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de trámite no encontrado con ID: " + id));

        if (!tramite.getCodigo().equalsIgnoreCase(request.getCodigo().trim())
                && tipoTramiteRepository.existsByCodigoIgnoreCase(request.getCodigo().trim())) {
            throw new BusinessException("Ya existe otro tipo de trámite con el código: " + request.getCodigo().trim());
        }

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Oficina no encontrada con ID: " + request.getOficinaResponsableId()));
        }

        tramite.setCodigo(request.getCodigo().trim().toUpperCase());
        tramite.setNombre(request.getNombre().trim());
        tramite.setDescripcion(request.getDescripcion());
        tramite.setOficinaResponsable(oficina);
        tramite.setRequiereDocumentos(request.getRequiereDocumentos());
        tramite.setPermitePresentacionVirtual(request.getPermitePresentacionVirtual());
        tramite.setPlazoDias(request.getPlazoDias());
        tramite.setUnidadPlazo(request.getUnidadPlazo());
        if (request.getEstado() != null) {
            tramite.setEstado(request.getEstado());
        }

        TipoTramite guardado = tipoTramiteRepository.save(tramite);
        log.info("Tipo de trámite actualizado: {} por {}", guardado.getCodigo(), username);
        return mpvMapper.toTipoTramiteResponse(guardado);
    }

    @Override
    @CacheEvict(value = "catalogoTiposTramite", allEntries = true)
    @Transactional
    public void cambiarEstado(Long id, String estado, String username) {
        TipoTramite tramite = tipoTramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de trámite no encontrado con ID: " + id));
        tramite.setEstado(estado.toUpperCase());
        tipoTramiteRepository.save(tramite);
        log.info("Estado de tipo de trámite {} cambiado a {} por {}", id, estado, username);
    }
}
