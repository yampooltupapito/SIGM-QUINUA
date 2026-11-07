package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.CatalogoTramitesService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConfiguracionHistorialService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CatalogoTramitesServiceImpl implements CatalogoTramitesService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoTramitesServiceImpl.class);

    private final TipoTramiteRepository tipoTramiteRepository;
    private final TramiteVersionRepository tramiteVersionRepository;
    private final RequisitoDocumentalRepository requisitoRepository;
    private final FormularioTramiteRepository formularioRepository;
    private final ExpedienteRepository expedienteRepository;
    private final OficinaRepository oficinaRepository;
    private final ConfiguracionHistorialService historialService;

    public CatalogoTramitesServiceImpl(
            TipoTramiteRepository tipoTramiteRepository,
            TramiteVersionRepository tramiteVersionRepository,
            RequisitoDocumentalRepository requisitoRepository,
            FormularioTramiteRepository formularioRepository,
            ExpedienteRepository expedienteRepository,
            OficinaRepository oficinaRepository,
            ConfiguracionHistorialService historialService
    ) {
        this.tipoTramiteRepository = tipoTramiteRepository;
        this.tramiteVersionRepository = tramiteVersionRepository;
        this.requisitoRepository = requisitoRepository;
        this.formularioRepository = formularioRepository;
        this.expedienteRepository = expedienteRepository;
        this.oficinaRepository = oficinaRepository;
        this.historialService = historialService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TramiteAdminResponse> listarTramites(EstadoCatalogoTramite estadoCatalogo, String query, Pageable pageable) {
        String searchPattern = (query != null && !query.trim().isBlank())
                ? "%" + query.trim().toLowerCase() + "%"
                : null;
        Page<TipoTramite> pagina = tipoTramiteRepository.buscarParaAdmin(estadoCatalogo, searchPattern, pageable);
        return pagina.map(this::mapearAdminResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TramiteAdminResponse obtenerPorId(Long id) {
        TipoTramite tramite = tipoTramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado con ID: " + id));
        return mapearAdminResponse(tramite);
    }

    @Override
    @Transactional
    public TramiteAdminResponse crearBorrador(GuardarTramiteRequest request, String username, String ipOrigen) {
        if (tipoTramiteRepository.existsByCodigoIgnoreCase(request.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un trámite registrado con el código: " + request.getCodigo());
        }

        Oficina oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                .orElseThrow(() -> new ResourceNotFoundException("Oficina no encontrada con ID: " + request.getOficinaResponsableId()));

        if (!"ACTIVO".equalsIgnoreCase(oficina.getEstado())) {
            throw new IllegalArgumentException("No se puede asociar un trámite a una oficina en estado inactivo: " + oficina.getNombre());
        }

        if (request.getPlazoDias() == null || request.getPlazoDias() <= 0) {
            throw new IllegalArgumentException("El plazo en días debe ser un valor positivo.");
        }

        TipoTramite tramite = TipoTramite.builder()
                .codigo(request.getCodigo().trim().toUpperCase())
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .codigoTupa(request.getCodigoTupa())
                .denominacionOficial(request.getDenominacionOficial() != null ? request.getDenominacionOficial().trim() : request.getNombre().trim())
                .baseNormativa(request.getBaseNormativa())
                .categoria(request.getCategoria() != null ? request.getCategoria().trim() : "GENERAL")
                .oficinaResponsable(oficina)
                .requiereDocumentos(Boolean.TRUE.equals(request.getRequiereDocumentos()))
                .permitePresentacionVirtual(Boolean.TRUE.equals(request.getPermitePresentacionVirtual()))
                .permitePresencial(Boolean.TRUE.equals(request.getPermitePresencial()))
                .plazoDias(request.getPlazoDias())
                .unidadPlazo(request.getUnidadPlazo() != null ? request.getUnidadPlazo() : UnidadPlazo.DIAS_HABILES)
                .versionActual(1)
                .estadoCatalogo(EstadoCatalogoTramite.BORRADOR)
                .requiereTasa(Boolean.TRUE.equals(request.getRequiereTasa()))
                .montoTasa(request.getMontoTasa() != null ? request.getMontoTasa() : BigDecimal.ZERO)
                .cuentaTasa(request.getCuentaTasa())
                .fechaVigenciaDesde(request.getFechaVigenciaDesde())
                .fechaVigenciaHasta(request.getFechaVigenciaHasta())
                .estado("ACTIVO")
                .createdBy(username)
                .build();

        TipoTramite guardado = tipoTramiteRepository.save(tramite);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.TRAMITE,
                guardado.getId(),
                guardado.getCodigo(),
                TipoCambioConfiguracion.CREACION,
                null,
                1,
                request.getMotivoCambio() != null ? request.getMotivoCambio() : "Creación inicial en borrador",
                null,
                null,
                username,
                ipOrigen
        );

        return mapearAdminResponse(guardado);
    }

    @Override
    @Transactional
    public TramiteAdminResponse editarBorrador(Long id, GuardarTramiteRequest request, String username, String ipOrigen) {
        TipoTramite tramite = tipoTramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado con ID: " + id));

        // Regla: No editar directamente trámites en estado PUBLICADO
        if (tramite.getEstadoCatalogo() == EstadoCatalogoTramite.PUBLICADO) {
            throw new IllegalStateException("No se puede editar directamente un procedimiento en estado PUBLICADO. Debe crear una nueva versión o suspenderlo.");
        }

        // Verificar unicidad de código si se intenta cambiar
        if (!tramite.getCodigo().equalsIgnoreCase(request.getCodigo())) {
            if (tipoTramiteRepository.existsByCodigoIgnoreCase(request.getCodigo())) {
                throw new IllegalArgumentException("Ya existe otro trámite con el código: " + request.getCodigo());
            }
            tramite.setCodigo(request.getCodigo().trim().toUpperCase());
        }

        Oficina oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                .orElseThrow(() -> new ResourceNotFoundException("Oficina no encontrada con ID: " + request.getOficinaResponsableId()));

        if (!"ACTIVO".equalsIgnoreCase(oficina.getEstado())) {
            throw new IllegalArgumentException("La oficina seleccionada está inactiva.");
        }

        tramite.setNombre(request.getNombre().trim());
        tramite.setDescripcion(request.getDescripcion());
        tramite.setCodigoTupa(request.getCodigoTupa());
        tramite.setDenominacionOficial(request.getDenominacionOficial() != null ? request.getDenominacionOficial().trim() : request.getNombre().trim());
        tramite.setBaseNormativa(request.getBaseNormativa());
        tramite.setCategoria(request.getCategoria());
        tramite.setOficinaResponsable(oficina);
        tramite.setRequiereDocumentos(Boolean.TRUE.equals(request.getRequiereDocumentos()));
        tramite.setPermitePresentacionVirtual(Boolean.TRUE.equals(request.getPermitePresentacionVirtual()));
        tramite.setPermitePresencial(Boolean.TRUE.equals(request.getPermitePresencial()));
        tramite.setPlazoDias(request.getPlazoDias());
        tramite.setUnidadPlazo(request.getUnidadPlazo());
        tramite.setRequiereTasa(Boolean.TRUE.equals(request.getRequiereTasa()));
        tramite.setMontoTasa(request.getMontoTasa());
        tramite.setCuentaTasa(request.getCuentaTasa());
        tramite.setFechaVigenciaDesde(request.getFechaVigenciaDesde());
        tramite.setFechaVigenciaHasta(request.getFechaVigenciaHasta());
        tramite.setUpdatedBy(username);

        TipoTramite actualizado = tipoTramiteRepository.save(tramite);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.TRAMITE,
                actualizado.getId(),
                actualizado.getCodigo(),
                TipoCambioConfiguracion.MODIFICACION,
                actualizado.getVersionActual(),
                actualizado.getVersionActual(),
                request.getMotivoCambio() != null ? request.getMotivoCambio() : "Actualización de borrador",
                null,
                null,
                username,
                ipOrigen
        );

        return mapearAdminResponse(actualizado);
    }

    @Override
    @Transactional
    public TramiteAdminResponse enviarARevision(Long id, AccionTransicionRequest request, String username, String ipOrigen) {
        TipoTramite tramite = tipoTramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado con ID: " + id));

        if (tramite.getEstadoCatalogo() != EstadoCatalogoTramite.BORRADOR) {
            throw new IllegalStateException("Solo los trámites en estado BORRADOR pueden enviarse a revisión.");
        }

        tramite.setEstadoCatalogo(EstadoCatalogoTramite.EN_REVISION);
        tramite.setUpdatedBy(username);
        TipoTramite guardado = tipoTramiteRepository.save(tramite);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.TRAMITE,
                guardado.getId(),
                guardado.getCodigo(),
                TipoCambioConfiguracion.ENVIADO_REVISION,
                guardado.getVersionActual(),
                guardado.getVersionActual(),
                request != null ? request.getMotivo() : "Envío a revisión técnica institucional",
                null,
                null,
                username,
                ipOrigen
        );

        return mapearAdminResponse(guardado);
    }

    @Override
    @Transactional
    public TramiteAdminResponse aprobar(Long id, AccionTransicionRequest request, String username, String ipOrigen) {
        TipoTramite tramite = tipoTramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado con ID: " + id));

        if (tramite.getEstadoCatalogo() != EstadoCatalogoTramite.EN_REVISION) {
            throw new IllegalStateException("Solo los trámites en estado EN_REVISION pueden ser aprobados.");
        }

        // Separación de funciones: El creador del borrador no debe ser quien apruebe si hay separación de funciones
        if (tramite.getCreatedBy() != null && tramite.getCreatedBy().equalsIgnoreCase(username)) {
            log.warn("Separación de funciones: usuario {} es tanto creador como aprobador del trámite {}", username, tramite.getCodigo());
        }

        tramite.setEstadoCatalogo(EstadoCatalogoTramite.APROBADO);
        tramite.setAprobadoPor(username);
        tramite.setFechaAprobacion(OffsetDateTime.now());
        tramite.setUpdatedBy(username);
        TipoTramite guardado = tipoTramiteRepository.save(tramite);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.TRAMITE,
                guardado.getId(),
                guardado.getCodigo(),
                TipoCambioConfiguracion.APROBACION,
                guardado.getVersionActual(),
                guardado.getVersionActual(),
                request != null ? request.getMotivo() : "Aprobación técnica favorable",
                null,
                null,
                username,
                ipOrigen
        );

        return mapearAdminResponse(guardado);
    }

    @Override
    @Transactional
    public TramiteAdminResponse publicar(Long id, AccionTransicionRequest request, String username, String ipOrigen) {
        TipoTramite tramite = tipoTramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado con ID: " + id));

        if (tramite.getEstadoCatalogo() != EstadoCatalogoTramite.APROBADO &&
            tramite.getEstadoCatalogo() != EstadoCatalogoTramite.BORRADOR) {
            throw new IllegalStateException("El trámite debe estar APROBADO o en BORRADOR validado para ser publicado.");
        }

        // 1. Validar que la oficina responsable esté activa
        if (tramite.getOficinaResponsable() == null || !"ACTIVO".equalsIgnoreCase(tramite.getOficinaResponsable().getEstado())) {
            throw new IllegalStateException("No se puede publicar: la oficina responsable está inactiva o no asignada.");
        }

        // 2. Validar plazos configurados
        if (tramite.getPlazoDias() == null || tramite.getPlazoDias() <= 0) {
            throw new IllegalStateException("No se puede publicar: el plazo legal configurado debe ser mayor a 0.");
        }
        if (tramite.getUnidadPlazo() == null) {
            throw new IllegalStateException("No se puede publicar: la unidad de plazo es obligatoria.");
        }

        // 3. Validar requisitos si aplica
        if (Boolean.TRUE.equals(tramite.getRequiereDocumentos())) {
            long reqCount = requisitoRepository.countByTipoTramiteIdAndObligatorioTrueAndEstado(tramite.getId(), "ACTIVO");
            if (reqCount == 0) {
                log.info("Publicación de trámite {} sin requisitos obligatorios activos.", tramite.getCodigo());
            }
        }

        int versionAnterior = tramite.getVersionActual() != null ? tramite.getVersionActual() : 1;
        int versionNueva = (tramite.getEstadoCatalogo() == EstadoCatalogoTramite.PUBLICADO) ? versionAnterior + 1 : versionAnterior;

        OffsetDateTime ahora = OffsetDateTime.now();
        tramite.setVersionActual(versionNueva);
        tramite.setEstadoCatalogo(EstadoCatalogoTramite.PUBLICADO);
        tramite.setPublicadoPor(username);
        tramite.setFechaPublicacion(ahora);
        if (tramite.getFechaVigenciaDesde() == null) {
            tramite.setFechaVigenciaDesde(ahora);
        }
        tramite.setUpdatedBy(username);
        TipoTramite guardado = tipoTramiteRepository.save(tramite);

        // Crear versión inmutable en mpv_tramite_versiones
        TramiteVersion tv = TramiteVersion.builder()
                .tipoTramite(guardado)
                .numeroVersion(versionNueva)
                .nombre(guardado.getNombre())
                .descripcion(guardado.getDescripcion())
                .codigoTupa(guardado.getCodigoTupa())
                .denominacionOficial(guardado.getDenominacionOficial())
                .baseNormativa(guardado.getBaseNormativa())
                .oficinaResponsable(guardado.getOficinaResponsable())
                .plazoDias(guardado.getPlazoDias())
                .unidadPlazo(guardado.getUnidadPlazo())
                .requiereTasa(guardado.getRequiereTasa())
                .montoTasa(guardado.getMontoTasa())
                .cuentaTasa(guardado.getCuentaTasa())
                .permiteVirtual(guardado.getPermitePresentacionVirtual())
                .permitePresencial(guardado.getPermitePresencial())
                .estado(EstadoCatalogoTramite.PUBLICADO)
                .fechaVigenciaDesde(guardado.getFechaVigenciaDesde())
                .fechaVigenciaHasta(guardado.getFechaVigenciaHasta())
                .motivoCambio(request != null ? request.getMotivo() : "Publicación oficial en portal ciudadano")
                .aprobadoPor(guardado.getAprobadoPor())
                .fechaAprobacion(guardado.getFechaAprobacion())
                .publicadoPor(username)
                .fechaPublicacion(ahora)
                .createdBy(username)
                .build();

        tramiteVersionRepository.save(tv);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.TRAMITE,
                guardado.getId(),
                guardado.getCodigo(),
                TipoCambioConfiguracion.PUBLICACION,
                versionAnterior,
                versionNueva,
                request != null ? request.getMotivo() : "Publicación oficial",
                null,
                null,
                username,
                ipOrigen
        );

        return mapearAdminResponse(guardado);
    }

    @Override
    @Transactional
    public TramiteAdminResponse suspender(Long id, AccionTransicionRequest request, String username, String ipOrigen) {
        TipoTramite tramite = tipoTramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado con ID: " + id));

        if (tramite.getEstadoCatalogo() != EstadoCatalogoTramite.PUBLICADO) {
            throw new IllegalStateException("Solo los trámites en estado PUBLICADO pueden ser suspendidos.");
        }

        tramite.setEstadoCatalogo(EstadoCatalogoTramite.SUSPENDIDO);
        tramite.setUpdatedBy(username);
        TipoTramite guardado = tipoTramiteRepository.save(tramite);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.TRAMITE,
                guardado.getId(),
                guardado.getCodigo(),
                TipoCambioConfiguracion.SUSPENSION,
                guardado.getVersionActual(),
                guardado.getVersionActual(),
                request != null ? request.getMotivo() : "Suspensión administrativa temporal",
                null,
                null,
                username,
                ipOrigen
        );

        return mapearAdminResponse(guardado);
    }

    @Override
    @Transactional
    public TramiteAdminResponse desactivar(Long id, AccionTransicionRequest request, String username, String ipOrigen) {
        TipoTramite tramite = tipoTramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado con ID: " + id));

        // Verificar expedientes activos relacionados
        long activos = expedienteRepository.countByTipoTramiteIdAndEstadoActualNotIn(
                id, Set.of(EstadoExpediente.ARCHIVADO, EstadoExpediente.ANULADO));
        if (activos > 0) {
            throw new IllegalStateException("No se puede desactivar el trámite porque tiene " + activos + " expedientes en curso. Debe concluir o derivar los expedientes antes.");
        }

        tramite.setEstadoCatalogo(EstadoCatalogoTramite.INACTIVO);
        tramite.setEstado("INACTIVO");
        tramite.setUpdatedBy(username);
        TipoTramite guardado = tipoTramiteRepository.save(tramite);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.TRAMITE,
                guardado.getId(),
                guardado.getCodigo(),
                TipoCambioConfiguracion.DESACTIVACION,
                guardado.getVersionActual(),
                guardado.getVersionActual(),
                request != null ? request.getMotivo() : "Inactivación definitiva de procedimiento",
                null,
                null,
                username,
                ipOrigen
        );

        return mapearAdminResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TramiteVersionResponse> listarVersiones(Long tipoTramiteId) {
        return tramiteVersionRepository.findByTipoTramiteIdOrderByNumeroVersionDesc(tipoTramiteId)
                .stream()
                .map(this::mapearVersionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TramiteVersionResponse obtenerVersion(Long tipoTramiteId, Integer numeroVersion) {
        TramiteVersion tv = tramiteVersionRepository.findByTipoTramiteIdAndNumeroVersion(tipoTramiteId, numeroVersion)
                .orElseThrow(() -> new ResourceNotFoundException("Versión " + numeroVersion + " no encontrada para el trámite ID: " + tipoTramiteId));
        return mapearVersionResponse(tv);
    }

    private TramiteAdminResponse mapearAdminResponse(TipoTramite t) {
        long totalExpedientes = expedienteRepository.countByTipoTramiteId(t.getId());
        List<RequisitoDocumental> reqs = requisitoRepository.findByTipoTramiteIdOrderByOrdenAsc(t.getId());
        boolean tieneForm = formularioRepository.findByTipoTramiteIdAndActivoTrue(t.getId()).isPresent();

        return TramiteAdminResponse.builder()
                .id(t.getId())
                .codigo(t.getCodigo())
                .nombre(t.getNombre())
                .descripcion(t.getDescripcion())
                .codigoTupa(t.getCodigoTupa())
                .denominacionOficial(t.getDenominacionOficial())
                .baseNormativa(t.getBaseNormativa())
                .categoria(t.getCategoria())
                .oficinaResponsableId(t.getOficinaResponsable() != null ? t.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(t.getOficinaResponsable() != null ? t.getOficinaResponsable().getNombre() : "Sin asignar")
                .requiereDocumentos(t.getRequiereDocumentos())
                .permitePresentacionVirtual(t.getPermitePresentacionVirtual())
                .permitePresencial(t.getPermitePresencial())
                .plazoDias(t.getPlazoDias())
                .unidadPlazo(t.getUnidadPlazo())
                .versionActual(t.getVersionActual())
                .estadoCatalogo(t.getEstadoCatalogo())
                .requiereTasa(t.getRequiereTasa())
                .montoTasa(t.getMontoTasa())
                .cuentaTasa(t.getCuentaTasa())
                .fechaVigenciaDesde(t.getFechaVigenciaDesde())
                .fechaVigenciaHasta(t.getFechaVigenciaHasta())
                .aprobadoPor(t.getAprobadoPor())
                .fechaAprobacion(t.getFechaAprobacion())
                .publicadoPor(t.getPublicadoPor())
                .fechaPublicacion(t.getFechaPublicacion())
                .estado(t.getEstado())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .totalExpedientesAsociados(totalExpedientes)
                .totalRequisitos(reqs.size())
                .tieneFormularioDinamico(tieneForm)
                .build();
    }

    private TramiteVersionResponse mapearVersionResponse(TramiteVersion tv) {
        return TramiteVersionResponse.builder()
                .id(tv.getId())
                .tipoTramiteId(tv.getTipoTramite().getId())
                .numeroVersion(tv.getNumeroVersion())
                .nombre(tv.getNombre())
                .descripcion(tv.getDescripcion())
                .codigoTupa(tv.getCodigoTupa())
                .denominacionOficial(tv.getDenominacionOficial())
                .baseNormativa(tv.getBaseNormativa())
                .oficinaResponsableNombre(tv.getOficinaResponsable() != null ? tv.getOficinaResponsable().getNombre() : "Sin asignar")
                .plazoDias(tv.getPlazoDias())
                .unidadPlazo(tv.getUnidadPlazo())
                .requiereTasa(tv.getRequiereTasa())
                .montoTasa(tv.getMontoTasa())
                .permiteVirtual(tv.getPermiteVirtual())
                .permitePresencial(tv.getPermitePresencial())
                .estado(tv.getEstado())
                .motivoCambio(tv.getMotivoCambio())
                .aprobadoPor(tv.getAprobadoPor())
                .fechaAprobacion(tv.getFechaAprobacion())
                .publicadoPor(tv.getPublicadoPor())
                .fechaPublicacion(tv.getFechaPublicacion())
                .createdAt(tv.getCreatedAt())
                .createdBy(tv.getCreatedBy())
                .build();
    }
}
