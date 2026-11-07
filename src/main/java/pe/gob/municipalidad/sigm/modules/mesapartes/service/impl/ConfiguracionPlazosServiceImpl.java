package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.TipoTramite;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCambioConfiguracion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoEntidadConfiguracion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.UnidadPlazo;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.TipoTramiteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.CatalogoTramitesService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConfiguracionHistorialService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConfiguracionPlazosService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfiguracionPlazosServiceImpl implements ConfiguracionPlazosService {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionPlazosServiceImpl.class);

    private final TipoTramiteRepository tipoTramiteRepository;
    private final CatalogoTramitesService catalogoService;
    private final ConfiguracionHistorialService historialService;

    public ConfiguracionPlazosServiceImpl(
            TipoTramiteRepository tipoTramiteRepository,
            CatalogoTramitesService catalogoService,
            ConfiguracionHistorialService historialService
    ) {
        this.tipoTramiteRepository = tipoTramiteRepository;
        this.catalogoService = catalogoService;
        this.historialService = historialService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TramiteAdminResponse> listarPlazosPorTramite() {
        return tipoTramiteRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(t -> catalogoService.obtenerPorId(t.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TramiteAdminResponse actualizarPlazo(
            Long tipoTramiteId,
            Integer plazoDias,
            UnidadPlazo unidadPlazo,
            String baseNormativa,
            String motivo,
            String username,
            String ipOrigen
    ) {
        if (plazoDias == null || plazoDias <= 0) {
            throw new IllegalArgumentException("El plazo en días debe ser un número entero positivo mayor a cero.");
        }
        if (plazoDias > 365) {
            throw new IllegalArgumentException("El plazo no puede exceder 365 días calendario.");
        }
        if (unidadPlazo == null) {
            throw new IllegalArgumentException("La unidad de plazo (DÍAS HÁBILES o DÍAS CALENDARIO) es obligatoria.");
        }

        TipoTramite tramite = tipoTramiteRepository.findById(tipoTramiteId)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado con ID: " + tipoTramiteId));

        int plazoAnterior = tramite.getPlazoDias() != null ? tramite.getPlazoDias() : 30;
        tramite.setPlazoDias(plazoDias);
        tramite.setUnidadPlazo(unidadPlazo);
        if (baseNormativa != null && !baseNormativa.trim().isBlank()) {
            tramite.setBaseNormativa(baseNormativa.trim());
        }
        tramite.setUpdatedBy(username);

        TipoTramite guardado = tipoTramiteRepository.save(tramite);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.PLAZO,
                guardado.getId(),
                guardado.getCodigo(),
                TipoCambioConfiguracion.MODIFICACION,
                plazoAnterior,
                plazoDias,
                motivo != null ? motivo : "Actualización de plazo legal (" + plazoDias + " " + unidadPlazo + ")",
                null,
                null,
                username,
                ipOrigen
        );

        return catalogoService.obtenerPorId(guardado.getId());
    }
}
