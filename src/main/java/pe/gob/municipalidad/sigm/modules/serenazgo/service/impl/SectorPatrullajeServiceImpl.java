package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.HistorialDelimitacionTerritorial;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.HistorialEstadoTerritorio;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.SectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.ZonaPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEntidadTerritorial;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.HistorialDelimitacionTerritorialRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.HistorialEstadoTerritorioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.SectorPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.ZonaPatrullajeRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.GeoJsonValidatorService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.SectorPatrullajeService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SectorPatrullajeServiceImpl implements SectorPatrullajeService {

    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final OficinaRepository oficinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialDelimitacionTerritorialRepository historialDelimitacionRepository;
    private final HistorialEstadoTerritorioRepository historialEstadoRepository;
    private final GeoJsonValidatorService geoJsonValidatorService;

    @Override
    @Transactional(readOnly = true)
    public Page<SectorResponse> listarSectores(SectorFilter filter, Pageable pageable) {
        List<SectorPatrullaje> todos = sectorRepository.findAll();

        List<SectorResponse> filtrados = todos.stream()
                .filter(s -> {
                    if (filter == null) return true;
                    if (filter.getQ() != null && !filter.getQ().trim().isEmpty()) {
                        String q = filter.getQ().trim().toLowerCase();
                        boolean matchCod = s.getCodigo() != null && s.getCodigo().toLowerCase().contains(q);
                        boolean matchNom = s.getNombre() != null && s.getNombre().toLowerCase().contains(q);
                        boolean matchDesc = s.getDescripcion() != null && s.getDescripcion().toLowerCase().contains(q);
                        if (!matchCod && !matchNom && !matchDesc) return false;
                    }
                    if (filter.getOficinaId() != null) {
                        if (s.getOficinaResponsable() == null || !s.getOficinaResponsable().getId().equals(filter.getOficinaId())) {
                            return false;
                        }
                    }
                    if (filter.getEstado() != null) {
                        if (s.getEstado() != filter.getEstado()) return false;
                    }
                    if (filter.getConDelimitacion() != null) {
                        if (!filter.getConDelimitacion().equals(s.getTieneDelimitacion())) return false;
                    }
                    if (Boolean.TRUE.equals(filter.getVigente())) {
                        LocalDate hoy = LocalDate.now();
                        if (s.getFechaInicioVigencia() != null && s.getFechaInicioVigencia().isAfter(hoy)) return false;
                        if (s.getFechaFinVigencia() != null && s.getFechaFinVigencia().isBefore(hoy)) return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(SectorPatrullaje::getCodigo, Comparator.nullsLast(String::compareTo)))
                .map(this::mapearResponse)
                .collect(Collectors.toList());

        if (pageable.isUnpaged()) {
            return new PageImpl<>(filtrados, pageable, filtrados.size());
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtrados.size());
        List<SectorResponse> pageContent = (start < filtrados.size()) ? filtrados.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, filtrados.size());
    }

    @Override
    @Transactional(readOnly = true)
    public SectorDetailResponse obtenerDetallePorId(UUID id) {
        SectorPatrullaje s = sectorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sector de patrullaje no encontrado con ID: " + id));

        List<ZonaPatrullajeResponse> zonas = s.getZonas() != null ?
                s.getZonas().stream()
                        .map(this::mapearZonaResponse)
                        .collect(Collectors.toList()) : List.of();

        List<HistorialDelimitacionResponse> histDelim = historialDelimitacionRepository
                .findByTipoEntidadAndEntidadIdOrderByNumeroVersionDesc(TipoEntidadTerritorial.SECTOR, id)
                .stream()
                .map(this::mapearHistorialDelimitacion)
                .collect(Collectors.toList());

        List<HistorialEstadoTerritorioResponse> histEstados = historialEstadoRepository
                .findByTipoEntidadAndEntidadIdOrderByCreatedAtDesc(TipoEntidadTerritorial.SECTOR, id)
                .stream()
                .map(this::mapearHistorialEstado)
                .collect(Collectors.toList());

        return SectorDetailResponse.builder()
                .id(s.getId())
                .codigo(s.getCodigo())
                .nombre(s.getNombre())
                .descripcion(s.getDescripcion())
                .colorHex(s.getColorHex())
                .estado(s.getEstado())
                .estadoDesc(s.getEstado().getDescripcion())
                .oficinaResponsableId(s.getOficinaResponsable() != null ? s.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(s.getOficinaResponsable() != null ? s.getOficinaResponsable().getNombre() : "SIN ASIGNAR")
                .fechaInicioVigencia(s.getFechaInicioVigencia())
                .fechaFinVigencia(s.getFechaFinVigencia())
                .observaciones(s.getObservaciones())
                .tieneDelimitacion(s.getTieneDelimitacion())
                .geometriaGeojson(s.getGeometriaGeojson())
                .version(s.getVersion())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .createdBy(s.getCreatedBy())
                .updatedBy(s.getUpdatedBy())
                .zonas(zonas)
                .historialDelimitaciones(histDelim)
                .historialEstados(histEstados)
                .build();
    }

    @Override
    @Transactional
    public SectorResponse crearSector(SectorCreateRequest request, String username) {
        log.info("Creando nuevo sector de patrullaje por usuario: {}", username);

        // 1. Validar nombre único
        if (sectorRepository.existsByNombreIgnoreCase(request.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe un sector registrado con el nombre: " + request.getNombre().trim());
        }

        // 2. Generar o validar código
        String codigoFinal = request.getCodigo();
        if (codigoFinal == null || codigoFinal.trim().isEmpty()) {
            Long seq = sectorRepository.obtenerSiguienteValorSecuencia();
            codigoFinal = String.format("SEC-%d-%04d", LocalDate.now().getYear(), seq);
        } else {
            codigoFinal = codigoFinal.trim().toUpperCase();
            if (sectorRepository.existsByCodigo(codigoFinal)) {
                throw new IllegalArgumentException("Ya existe un sector con el código: " + codigoFinal);
            }
        }

        // 3. Validar vigencia
        if (request.getFechaInicioVigencia() != null && request.getFechaFinVigencia() != null) {
            if (request.getFechaFinVigencia().isBefore(request.getFechaInicioVigencia())) {
                throw new IllegalArgumentException("La fecha fin de vigencia no puede ser anterior a la fecha de inicio");
            }
        }

        // 4. Validar oficina si se especifica
        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                    .orElseThrow(() -> new NoSuchElementException("La oficina especificada no existe"));
        }

        // 5. Validar geometría si se adjunta
        boolean tieneDelim = false;
        String geoJsonSaneado = null;
        if (request.getGeometriaGeojson() != null && !request.getGeometriaGeojson().trim().isEmpty()) {
            ValidacionGeoJsonResult v = geoJsonValidatorService.validar(request.getGeometriaGeojson().trim());
            if (!v.isValido()) {
                throw new IllegalArgumentException("Geometría GeoJSON inválida: " + v.getMensajeError());
            }
            tieneDelim = true;
            geoJsonSaneado = request.getGeometriaGeojson().trim();
        }

        EstadoSectorPatrullaje estadoInicial = request.getEstado() != null ? request.getEstado() : EstadoSectorPatrullaje.ACTIVO;

        SectorPatrullaje sector = SectorPatrullaje.builder()
                .codigo(codigoFinal)
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null)
                .colorHex(request.getColorHex() != null ? request.getColorHex().trim() : "#1d4ed8")
                .estado(estadoInicial)
                .oficinaResponsable(oficina)
                .fechaInicioVigencia(request.getFechaInicioVigencia() != null ? request.getFechaInicioVigencia() : LocalDate.now())
                .fechaFinVigencia(request.getFechaFinVigencia())
                .observaciones(request.getObservaciones())
                .geometriaGeojson(geoJsonSaneado)
                .tieneDelimitacion(tieneDelim)
                .createdBy(username)
                .build();

        sector = sectorRepository.save(sector);

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        // Registro de historial inicial de estado
        historialEstadoRepository.save(HistorialEstadoTerritorio.builder()
                .tipoEntidad(TipoEntidadTerritorial.SECTOR)
                .entidadId(sector.getId())
                .estadoAnterior("INICIAL")
                .estadoNuevo(estadoInicial.name())
                .motivo("Alta inicial del sector de patrullaje")
                .usuarioRegistro(usuario)
                .build());

        // Registro de historial de delimitación inicial si se proporcionó
        if (tieneDelim) {
            historialDelimitacionRepository.save(HistorialDelimitacionTerritorial.builder()
                    .tipoEntidad(TipoEntidadTerritorial.SECTOR)
                    .entidadId(sector.getId())
                    .numeroVersion(1)
                    .geometriaGeojson(geoJsonSaneado)
                    .motivoCambio("Delimitación cartográfica inicial del sector")
                    .usuarioRegistro(usuario)
                    .build());
        }

        log.info("Sector de patrullaje creado exitosamente: {} [{}]", sector.getCodigo(), sector.getNombre());
        return mapearResponse(sector);
    }

    @Override
    @Transactional
    public SectorResponse actualizarSector(UUID id, SectorUpdateRequest request, String username) {
        log.info("Actualizando sector ID: {} por usuario: {}", id, username);

        SectorPatrullaje sector = sectorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sector de patrullaje no encontrado: " + id));

        // 1. Validar nombre único excluyendo este sector
        if (sectorRepository.existsByNombreIgnoreCaseAndIdNot(request.getNombre().trim(), id)) {
            throw new IllegalArgumentException("Ya existe otro sector registrado con el nombre: " + request.getNombre().trim());
        }

        // 2. Validar vigencia
        if (request.getFechaInicioVigencia() != null && request.getFechaFinVigencia() != null) {
            if (request.getFechaFinVigencia().isBefore(request.getFechaInicioVigencia())) {
                throw new IllegalArgumentException("La fecha fin de vigencia no puede ser anterior a la fecha de inicio");
            }
        }

        // 3. Oficina
        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                    .orElseThrow(() -> new NoSuchElementException("La oficina especificada no existe"));
        }

        sector.setNombre(request.getNombre().trim());
        sector.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        if (request.getColorHex() != null && !request.getColorHex().trim().isEmpty()) {
            sector.setColorHex(request.getColorHex().trim());
        }
        sector.setOficinaResponsable(oficina);
        sector.setFechaInicioVigencia(request.getFechaInicioVigencia());
        sector.setFechaFinVigencia(request.getFechaFinVigencia());
        sector.setObservaciones(request.getObservaciones());
        sector.setUpdatedBy(username);

        sector = sectorRepository.save(sector);
        return mapearResponse(sector);
    }

    @Override
    @Transactional
    public SectorResponse cambiarEstado(UUID id, CambioEstadoTerritorioRequest request, String username) {
        log.info("Cambiando estado de sector ID: {} a {} por {}", id, request.getNuevoEstado(), username);

        SectorPatrullaje sector = sectorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sector de patrullaje no encontrado: " + id));

        EstadoSectorPatrullaje anterior = sector.getEstado();
        EstadoSectorPatrullaje nuevo = request.getNuevoEstado();

        if (anterior == nuevo) {
            return mapearResponse(sector);
        }

        // Regla institucional: Si un sector pasa a INACTIVO o ARCHIVADO, sus zonas no pueden quedar activas
        if (nuevo == EstadoSectorPatrullaje.INACTIVO || nuevo == EstadoSectorPatrullaje.ARCHIVADO) {
            List<ZonaPatrullaje> zonasDelSector = zonaRepository.findBySectorId(id);
            for (ZonaPatrullaje z : zonasDelSector) {
                if (z.getEstado() == EstadoSectorPatrullaje.ACTIVO) {
                    log.info("Desactivando automáticamente zona dependiente {} por pase a {} del sector {}", z.getCodigo(), nuevo, sector.getCodigo());
                    z.setEstado(nuevo);
                    z.setUpdatedBy(username);
                    zonaRepository.save(z);

                    // Historial de la zona
                    Usuario u = usuarioRepository.findByUsername(username).orElse(null);
                    historialEstadoRepository.save(HistorialEstadoTerritorio.builder()
                            .tipoEntidad(TipoEntidadTerritorial.ZONA)
                            .entidadId(z.getId())
                            .estadoAnterior(EstadoSectorPatrullaje.ACTIVO.name())
                            .estadoNuevo(nuevo.name())
                            .motivo("Desactivación en cascada por cambio de estado del sector padre: " + request.getMotivo())
                            .usuarioRegistro(u)
                            .build());
                }
            }
        }

        sector.setEstado(nuevo);
        sector.setUpdatedBy(username);
        sector = sectorRepository.save(sector);

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        historialEstadoRepository.save(HistorialEstadoTerritorio.builder()
                .tipoEntidad(TipoEntidadTerritorial.SECTOR)
                .entidadId(sector.getId())
                .estadoAnterior(anterior.name())
                .estadoNuevo(nuevo.name())
                .motivo(request.getMotivo().trim())
                .usuarioRegistro(usuario)
                .build());

        return mapearResponse(sector);
    }

    @Override
    @Transactional
    public SectorResponse guardarDelimitacion(UUID id, DelimitacionGeoJsonRequest request, String username) {
        log.info("Guardando delimitación geográfica para sector ID: {} por {}", id, username);

        SectorPatrullaje sector = sectorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sector de patrullaje no encontrado: " + id));

        ValidacionGeoJsonResult v = geoJsonValidatorService.validar(request.getGeometriaGeojson());
        if (!v.isValido()) {
            throw new IllegalArgumentException("Geometría GeoJSON inválida: " + v.getMensajeError());
        }

        String geoJsonSaneado = request.getGeometriaGeojson().trim();
        sector.setGeometriaGeojson(geoJsonSaneado);
        sector.setTieneDelimitacion(true);
        sector.setUpdatedBy(username);
        sector = sectorRepository.save(sector);

        // Determinar siguiente número de versión
        long countVersiones = historialDelimitacionRepository.countByTipoEntidadAndEntidadId(TipoEntidadTerritorial.SECTOR, id);
        int siguienteVersion = (int) (countVersiones + 1);

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        historialDelimitacionRepository.save(HistorialDelimitacionTerritorial.builder()
                .tipoEntidad(TipoEntidadTerritorial.SECTOR)
                .entidadId(sector.getId())
                .numeroVersion(siguienteVersion)
                .geometriaGeojson(geoJsonSaneado)
                .motivoCambio(request.getMotivo().trim())
                .documentoReferencia(request.getDocumentoReferencia())
                .usuarioRegistro(usuario)
                .build());

        return mapearResponse(sector);
    }

    @Override
    @Transactional
    public void archivarSector(UUID id, String motivo, String username) {
        cambiarEstado(id, CambioEstadoTerritorioRequest.builder()
                .nuevoEstado(EstadoSectorPatrullaje.ARCHIVADO)
                .motivo(motivo != null ? motivo : "Archivado administrativo del sector")
                .build(), username);
    }

    // =========================================================================
    // Métodos para compatibilidad hacia atrás
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<SectorPatrullajeResponse> listarSectoresActivos() {
        return sectorRepository.findByEstado(EstadoSectorPatrullaje.ACTIVO).stream()
                .map(this::mapearSectorConZonas)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SectorPatrullajeResponse obtenerSectorPorId(UUID sectorId) {
        SectorPatrullaje sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new NoSuchElementException("Sector de patrullaje no encontrado: " + sectorId));
        return mapearSectorConZonas(sector);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZonaPatrullajeResponse> listarZonasPorSector(UUID sectorId) {
        return zonaRepository.findBySectorIdAndEstado(sectorId, EstadoSectorPatrullaje.ACTIVO).stream()
                .map(this::mapearZonaResponse)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Mapeadores auxiliares
    // =========================================================================

    private SectorResponse mapearResponse(SectorPatrullaje s) {
        int totalZonas = s.getZonas() != null ? s.getZonas().size() : 0;
        List<ZonaPatrullajeResponse> zonas = s.getZonas() != null ?
                s.getZonas().stream().map(this::mapearZonaResponse).collect(Collectors.toList()) : List.of();

        return SectorResponse.builder()
                .id(s.getId())
                .codigo(s.getCodigo())
                .nombre(s.getNombre())
                .descripcion(s.getDescripcion())
                .colorHex(s.getColorHex())
                .estado(s.getEstado())
                .estadoDesc(s.getEstado() != null ? s.getEstado().getDescripcion() : "")
                .oficinaResponsableId(s.getOficinaResponsable() != null ? s.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(s.getOficinaResponsable() != null ? s.getOficinaResponsable().getNombre() : "SIN ASIGNAR")
                .fechaInicioVigencia(s.getFechaInicioVigencia())
                .fechaFinVigencia(s.getFechaFinVigencia())
                .observaciones(s.getObservaciones())
                .tieneDelimitacion(s.getTieneDelimitacion())
                .geometriaGeojson(s.getGeometriaGeojson())
                .totalZonas(totalZonas)
                .zonas(zonas)
                .version(s.getVersion())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .createdBy(s.getCreatedBy())
                .updatedBy(s.getUpdatedBy())
                .build();
    }

    private ZonaPatrullajeResponse mapearZonaResponse(ZonaPatrullaje z) {
        return ZonaPatrullajeResponse.builder()
                .id(z.getId())
                .sectorId(z.getSector() != null ? z.getSector().getId() : null)
                .sectorCodigo(z.getSector() != null ? z.getSector().getCodigo() : "")
                .sectorNombre(z.getSector() != null ? z.getSector().getNombre() : "")
                .sectorColorHex(z.getSector() != null ? z.getSector().getColorHex() : "#1d4ed8")
                .codigo(z.getCodigo())
                .nombre(z.getNombre())
                .descripcion(z.getDescripcion())
                .estado(z.getEstado())
                .estadoDesc(z.getEstado() != null ? z.getEstado().getDescripcion() : "")
                .prioridadOperativa(z.getPrioridadOperativa())
                .prioridadOperativaDesc(z.getPrioridadOperativa() != null ? z.getPrioridadOperativa().getDescripcion() : "")
                .fechaInicioVigencia(z.getFechaInicioVigencia())
                .fechaFinVigencia(z.getFechaFinVigencia())
                .observaciones(z.getObservaciones())
                .tieneDelimitacion(z.getTieneDelimitacion())
                .geometriaGeojson(z.getGeometriaGeojson())
                .version(z.getVersion())
                .createdAt(z.getCreatedAt())
                .updatedAt(z.getUpdatedAt())
                .createdBy(z.getCreatedBy())
                .updatedBy(z.getUpdatedBy())
                .build();
    }

    private SectorPatrullajeResponse mapearSectorConZonas(SectorPatrullaje sector) {
        List<ZonaPatrullajeResponse> zonas = sector.getZonas() != null ?
                sector.getZonas().stream()
                        .filter(z -> z.getEstado() == EstadoSectorPatrullaje.ACTIVO)
                        .map(this::mapearZonaResponse)
                        .collect(Collectors.toList()) : List.of();

        return SectorPatrullajeResponse.builder()
                .id(sector.getId())
                .codigo(sector.getCodigo())
                .nombre(sector.getNombre())
                .descripcion(sector.getDescripcion())
                .colorHex(sector.getColorHex())
                .estado(sector.getEstado().name())
                .totalZonas(zonas.size())
                .zonas(zonas)
                .build();
    }

    private HistorialDelimitacionResponse mapearHistorialDelimitacion(HistorialDelimitacionTerritorial h) {
        return HistorialDelimitacionResponse.builder()
                .id(h.getId())
                .tipoEntidad(h.getTipoEntidad())
                .entidadId(h.getEntidadId())
                .numeroVersion(h.getNumeroVersion())
                .geometriaGeojson(h.getGeometriaGeojson())
                .motivoCambio(h.getMotivoCambio())
                .documentoReferencia(h.getDocumentoReferencia())
                .usuarioRegistro(h.getUsuarioRegistro() != null ? h.getUsuarioRegistro().getUsername() : null)
                .createdAt(h.getCreatedAt())
                .build();
    }

    private HistorialEstadoTerritorioResponse mapearHistorialEstado(HistorialEstadoTerritorio h) {
        return HistorialEstadoTerritorioResponse.builder()
                .id(h.getId())
                .tipoEntidad(h.getTipoEntidad())
                .entidadId(h.getEntidadId())
                .estadoAnterior(h.getEstadoAnterior())
                .estadoNuevo(h.getEstadoNuevo())
                .motivo(h.getMotivo())
                .usuarioRegistro(h.getUsuarioRegistro() != null ? h.getUsuarioRegistro().getUsername() : null)
                .createdAt(h.getCreatedAt())
                .build();
    }
}
