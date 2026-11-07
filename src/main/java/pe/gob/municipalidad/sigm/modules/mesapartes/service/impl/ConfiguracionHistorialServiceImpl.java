package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.HistorialConfiguracionResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ConfiguracionHistorial;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCambioConfiguracion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoEntidadConfiguracion;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ConfiguracionHistorialRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConfiguracionHistorialService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfiguracionHistorialServiceImpl implements ConfiguracionHistorialService {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionHistorialServiceImpl.class);

    private final ConfiguracionHistorialRepository historialRepository;

    public ConfiguracionHistorialServiceImpl(ConfiguracionHistorialRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarCambio(
            TipoEntidadConfiguracion entidadTipo,
            Long entidadId,
            String codigoReferencia,
            TipoCambioConfiguracion tipoCambio,
            Integer versionAnterior,
            Integer versionNueva,
            String motivo,
            String cambiosJson,
            Long usuarioId,
            String username,
            String ipOrigen
    ) {
        try {
            ConfiguracionHistorial h = ConfiguracionHistorial.builder()
                    .entidadTipo(entidadTipo)
                    .entidadId(entidadId)
                    .codigoReferencia(codigoReferencia)
                    .tipoCambio(tipoCambio)
                    .versionAnterior(versionAnterior)
                    .versionNueva(versionNueva)
                    .motivo(motivo)
                    .cambiosJson(cambiosJson)
                    .usuarioId(usuarioId)
                    .username(username != null ? username : "SISTEMA")
                    .ipOrigen(ipOrigen)
                    .build();

            historialRepository.save(h);
            log.info("Auditoría de configuración registrada: [{}] ID: {}, Tipo: {}, Versión: {} -> {}",
                    entidadTipo, entidadId, tipoCambio, versionAnterior, versionNueva);
        } catch (Exception e) {
            log.error("Error al registrar auditoría de configuración: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistorialConfiguracionResponse> listarHistorial(TipoEntidadConfiguracion entidadTipo, Pageable pageable) {
        Page<ConfiguracionHistorial> pagina;
        if (entidadTipo != null) {
            pagina = historialRepository.findByEntidadTipoOrderByFechaCambioDesc(entidadTipo, pageable);
        } else {
            pagina = historialRepository.findAllByOrderByFechaCambioDesc(pageable);
        }
        return pagina.map(this::mapearResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialConfiguracionResponse> listarPorEntidad(TipoEntidadConfiguracion entidadTipo, Long entidadId) {
        return historialRepository.findByEntidadTipoAndEntidadIdOrderByFechaCambioDesc(entidadTipo, entidadId)
                .stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    private HistorialConfiguracionResponse mapearResponse(ConfiguracionHistorial h) {
        return HistorialConfiguracionResponse.builder()
                .id(h.getId())
                .entidadTipo(h.getEntidadTipo())
                .entidadId(h.getEntidadId())
                .codigoReferencia(h.getCodigoReferencia())
                .tipoCambio(h.getTipoCambio())
                .versionAnterior(h.getVersionAnterior())
                .versionNueva(h.getVersionNueva())
                .motivo(h.getMotivo())
                .cambiosJson(h.getCambiosJson())
                .username(h.getUsername())
                .ipOrigen(h.getIpOrigen())
                .fechaCambio(h.getFechaCambio())
                .build();
    }
}
