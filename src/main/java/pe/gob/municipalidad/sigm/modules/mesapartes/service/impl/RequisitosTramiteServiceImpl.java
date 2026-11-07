package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.RequisitoDocumental;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.TipoTramite;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCambioConfiguracion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoEntidadConfiguracion;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.RequisitoDocumentalRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.TipoTramiteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConfiguracionHistorialService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.RequisitosTramiteService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RequisitosTramiteServiceImpl implements RequisitosTramiteService {

    private static final Logger log = LoggerFactory.getLogger(RequisitosTramiteServiceImpl.class);

    private final RequisitoDocumentalRepository requisitoRepository;
    private final TipoTramiteRepository tipoTramiteRepository;
    private final ConfiguracionHistorialService historialService;

    public RequisitosTramiteServiceImpl(
            RequisitoDocumentalRepository requisitoRepository,
            TipoTramiteRepository tipoTramiteRepository,
            ConfiguracionHistorialService historialService
    ) {
        this.requisitoRepository = requisitoRepository;
        this.tipoTramiteRepository = tipoTramiteRepository;
        this.historialService = historialService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequisitoAdminResponse> listarPorTramite(Long tipoTramiteId) {
        return requisitoRepository.findByTipoTramiteIdOrderByOrdenAsc(tipoTramiteId)
                .stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RequisitoAdminResponse obtenerPorId(Long id) {
        RequisitoDocumental req = requisitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requisito no encontrado con ID: " + id));
        return mapearResponse(req);
    }

    @Override
    @Transactional
    public RequisitoAdminResponse guardarRequisito(GuardarRequisitoRequest request, String username, String ipOrigen) {
        TipoTramite tramite = tipoTramiteRepository.findById(request.getTipoTramiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado con ID: " + request.getTipoTramiteId()));

        Optional<RequisitoDocumental> existe = requisitoRepository.findByTipoTramiteIdAndCodigo(
                tramite.getId(), request.getCodigo().trim().toUpperCase());
        if (existe.isPresent()) {
            throw new IllegalArgumentException("Ya existe un requisito con el código " + request.getCodigo() + " para este trámite.");
        }

        RequisitoDocumental req = RequisitoDocumental.builder()
                .tipoTramite(tramite)
                .codigo(request.getCodigo().trim().toUpperCase())
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .obligatorio(Boolean.TRUE.equals(request.getObligatorio()))
                .formatosPermitidos(request.getFormatosPermitidos() != null ? request.getFormatosPermitidos() : "application/pdf,image/jpeg,image/png")
                .tamanoMaximoMb(request.getTamanoMaximoMb() != null ? request.getTamanoMaximoMb() : 15)
                .orden(request.getOrden() != null ? request.getOrden() : 1)
                .estado(request.getEstado() != null ? request.getEstado() : "ACTIVO")
                .build();

        RequisitoDocumental guardado = requisitoRepository.save(req);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.REQUISITO,
                guardado.getId(),
                guardado.getCodigo(),
                TipoCambioConfiguracion.CREACION,
                null,
                1,
                "Registro de requisito documental para trámite " + tramite.getCodigo(),
                null,
                null,
                username,
                ipOrigen
        );

        return mapearResponse(guardado);
    }

    @Override
    @Transactional
    public RequisitoAdminResponse actualizarRequisito(Long id, GuardarRequisitoRequest request, String username, String ipOrigen) {
        RequisitoDocumental req = requisitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requisito no encontrado con ID: " + id));

        // Verificar código si cambió
        if (!req.getCodigo().equalsIgnoreCase(request.getCodigo())) {
            Optional<RequisitoDocumental> otro = requisitoRepository.findByTipoTramiteIdAndCodigo(
                    req.getTipoTramite().getId(), request.getCodigo().trim().toUpperCase());
            if (otro.isPresent() && !otro.get().getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otro requisito con el código: " + request.getCodigo());
            }
            req.setCodigo(request.getCodigo().trim().toUpperCase());
        }

        req.setNombre(request.getNombre().trim());
        req.setDescripcion(request.getDescripcion());
        req.setObligatorio(Boolean.TRUE.equals(request.getObligatorio()));
        req.setFormatosPermitidos(request.getFormatosPermitidos());
        req.setTamanoMaximoMb(request.getTamanoMaximoMb());
        req.setOrden(request.getOrden());
        if (request.getEstado() != null) {
            req.setEstado(request.getEstado());
        }

        RequisitoDocumental actualizado = requisitoRepository.save(req);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.REQUISITO,
                actualizado.getId(),
                actualizado.getCodigo(),
                TipoCambioConfiguracion.MODIFICACION,
                1,
                1,
                "Actualización de requisito documental",
                null,
                null,
                username,
                ipOrigen
        );

        return mapearResponse(actualizado);
    }

    @Override
    @Transactional
    public void cambiarEstadoRequisito(Long id, String nuevoEstado, String motivo, String username, String ipOrigen) {
        RequisitoDocumental req = requisitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requisito no encontrado con ID: " + id));

        req.setEstado(nuevoEstado != null ? nuevoEstado.toUpperCase() : "INACTIVO");
        requisitoRepository.save(req);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.REQUISITO,
                req.getId(),
                req.getCodigo(),
                TipoCambioConfiguracion.MODIFICACION,
                null,
                null,
                motivo != null ? motivo : "Cambio de estado a " + req.getEstado(),
                null,
                null,
                username,
                ipOrigen
        );
    }

    private RequisitoAdminResponse mapearResponse(RequisitoDocumental r) {
        return RequisitoAdminResponse.builder()
                .id(r.getId())
                .tipoTramiteId(r.getTipoTramite().getId())
                .codigo(r.getCodigo())
                .nombre(r.getNombre())
                .descripcion(r.getDescripcion())
                .obligatorio(r.getObligatorio())
                .formatosPermitidos(r.getFormatosPermitidos())
                .tamanoMaximoMb(r.getTamanoMaximoMb())
                .orden(r.getOrden())
                .estado(r.getEstado())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
