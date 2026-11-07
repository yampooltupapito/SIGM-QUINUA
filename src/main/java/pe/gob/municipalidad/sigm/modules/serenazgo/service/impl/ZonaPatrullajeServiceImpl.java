package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadOperativaZona;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEntidadTerritorial;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.GeoJsonValidatorService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.ZonaPatrullajeService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZonaPatrullajeServiceImpl implements ZonaPatrullajeService {

    private final ZonaPatrullajeRepository zonaRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialDelimitacionTerritorialRepository historialDelimitacionRepository;
    private final HistorialReasignacionZonaRepository historialReasignacionRepository;
    private final HistorialEstadoTerritorioRepository historialEstadoRepository;
    private final GeoJsonValidatorService geoJsonValidatorService;

    @Override
    @Transactional(readOnly = true)
    public Page<ZonaPatrullajeResponse> listarZonas(ZonaFilter filter, Pageable pageable) {
        List<ZonaPatrullaje> todas = zonaRepository.findAll();

        List<ZonaPatrullajeResponse> filtradas = todas.stream()
                .filter(z -> {
                    if (filter == null) return true;
                    if (filter.getQ() != null && !filter.getQ().trim().isEmpty()) {
                        String q = filter.getQ().trim().toLowerCase();
                        boolean matchCod = z.getCodigo() != null && z.getCodigo().toLowerCase().contains(q);
                        boolean matchNom = z.getNombre() != null && z.getNombre().toLowerCase().contains(q);
                        boolean matchDesc = z.getDescripcion() != null && z.getDescripcion().toLowerCase().contains(q);
                        boolean matchSec = z.getSector() != null && (
                                (z.getSector().getNombre() != null && z.getSector().getNombre().toLowerCase().contains(q)) ||
                                (z.getSector().getCodigo() != null && z.getSector().getCodigo().toLowerCase().contains(q))
                        );
                        if (!matchCod && !matchNom && !matchDesc && !matchSec) return false;
                    }
                    if (filter.getSectorId() != null) {
                        if (z.getSector() == null || !z.getSector().getId().equals(filter.getSectorId())) {
                            return false;
                        }
                    }
                    if (filter.getEstado() != null) {
                        if (z.getEstado() != filter.getEstado()) return false;
                    }
                    if (filter.getPrioridad() != null) {
                        if (z.getPrioridadOperativa() != filter.getPrioridad()) return false;
                    }
                    if (filter.getConDelimitacion() != null) {
                        if (!filter.getConDelimitacion().equals(z.getTieneDelimitacion())) return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparing((ZonaPatrullaje z) -> z.getSector() != null ? z.getSector().getCodigo() : "")
                        .thenComparing(ZonaPatrullaje::getCodigo, Comparator.nullsLast(String::compareTo)))
                .map(this::mapearResponse)
                .collect(Collectors.toList());

        if (pageable.isUnpaged()) {
            return new PageImpl<>(filtradas, pageable, filtradas.size());
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtradas.size());
        List<ZonaPatrullajeResponse> pageContent = (start < filtradas.size()) ? filtradas.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, filtradas.size());
    }

    @Override
    @Transactional(readOnly = true)
    public ZonaDetailResponse obtenerDetallePorId(UUID id) {
        ZonaPatrullaje z = zonaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Zona de patrullaje no encontrada con ID: " + id));

        List<HistorialReasignacionResponse> histReasig = historialReasignacionRepository
                .findByZonaIdOrderByCreatedAtDesc(id)
                .stream()
                .map(this::mapearHistorialReasignacion)
                .collect(Collectors.toList());

        List<HistorialDelimitacionResponse> histDelim = historialDelimitacionRepository
                .findByTipoEntidadAndEntidadIdOrderByNumeroVersionDesc(TipoEntidadTerritorial.ZONA, id)
                .stream()
                .map(this::mapearHistorialDelimitacion)
                .collect(Collectors.toList());

        List<HistorialEstadoTerritorioResponse> histEstados = historialEstadoRepository
                .findByTipoEntidadAndEntidadIdOrderByCreatedAtDesc(TipoEntidadTerritorial.ZONA, id)
                .stream()
                .map(this::mapearHistorialEstado)
                .collect(Collectors.toList());

        return ZonaDetailResponse.builder()
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
                .historialReasignaciones(histReasig)
                .historialDelimitaciones(histDelim)
                .historialEstados(histEstados)
                .build();
    }

    @Override
    @Transactional
    public ZonaPatrullajeResponse crearZona(ZonaCreateRequest request, String username) {
        log.info("Creando nueva zona de patrullaje por usuario: {}", username);

        // 1. Validar sector
        SectorPatrullaje sector = sectorRepository.findById(request.getSectorId())
                .orElseThrow(() -> new NoSuchElementException("El sector especificado no existe: " + request.getSectorId()));

        EstadoSectorPatrullaje estadoInicial = request.getEstado() != null ? request.getEstado() : EstadoSectorPatrullaje.ACTIVO;

        // Regla institucional: Una zona no puede quedar operativamente activa dentro de un sector inactivo
        if (estadoInicial == EstadoSectorPatrullaje.ACTIVO && sector.getEstado() != EstadoSectorPatrullaje.ACTIVO) {
            throw new IllegalStateException("No se puede registrar una zona activa en un sector que se encuentra " + sector.getEstado().getDescripcion());
        }

        // 2. Generar o validar código
        String codigoFinal = request.getCodigo();
        if (codigoFinal == null || codigoFinal.trim().isEmpty()) {
            Long seq = zonaRepository.obtenerSiguienteValorSecuencia();
            codigoFinal = String.format("ZON-%d-%04d", LocalDate.now().getYear(), seq);
        } else {
            codigoFinal = codigoFinal.trim().toUpperCase();
            if (zonaRepository.existsBySectorIdAndCodigo(sector.getId(), codigoFinal)) {
                throw new IllegalArgumentException("Ya existe una zona con el código: " + codigoFinal + " en el sector " + sector.getNombre());
            }
        }

        // 3. Validar nombre único en el sector
        if (zonaRepository.existsBySectorIdAndNombreIgnoreCase(sector.getId(), request.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe una zona con el nombre: " + request.getNombre().trim() + " en este sector");
        }

        // 4. Validar vigencia
        if (request.getFechaInicioVigencia() != null && request.getFechaFinVigencia() != null) {
            if (request.getFechaFinVigencia().isBefore(request.getFechaInicioVigencia())) {
                throw new IllegalArgumentException("La fecha fin de vigencia no puede ser anterior a la fecha de inicio");
            }
        }

        // 5. Geometría
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

        ZonaPatrullaje zona = ZonaPatrullaje.builder()
                .sector(sector)
                .codigo(codigoFinal)
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null)
                .estado(estadoInicial)
                .prioridadOperativa(request.getPrioridadOperativa() != null ? request.getPrioridadOperativa() : PrioridadOperativaZona.MEDIA)
                .fechaInicioVigencia(request.getFechaInicioVigencia() != null ? request.getFechaInicioVigencia() : LocalDate.now())
                .fechaFinVigencia(request.getFechaFinVigencia())
                .observaciones(request.getObservaciones())
                .geometriaGeojson(geoJsonSaneado)
                .tieneDelimitacion(tieneDelim)
                .createdBy(username)
                .build();

        zona = zonaRepository.save(zona);

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        // Historial de estado
        historialEstadoRepository.save(HistorialEstadoTerritorio.builder()
                .tipoEntidad(TipoEntidadTerritorial.ZONA)
                .entidadId(zona.getId())
                .estadoAnterior("INICIAL")
                .estadoNuevo(estadoInicial.name())
                .motivo("Alta inicial de zona operativa")
                .usuarioRegistro(usuario)
                .build());

        // Historial delimitación
        if (tieneDelim) {
            historialDelimitacionRepository.save(HistorialDelimitacionTerritorial.builder()
                    .tipoEntidad(TipoEntidadTerritorial.ZONA)
                    .entidadId(zona.getId())
                    .numeroVersion(1)
                    .geometriaGeojson(geoJsonSaneado)
                    .motivoCambio("Delimitación cartográfica inicial de la zona")
                    .usuarioRegistro(usuario)
                    .build());
        }

        log.info("Zona de patrullaje creada exitosamente: {} [{}] en sector {}", zona.getCodigo(), zona.getNombre(), sector.getCodigo());
        return mapearResponse(zona);
    }

    @Override
    @Transactional
    public ZonaPatrullajeResponse actualizarZona(UUID id, ZonaUpdateRequest request, String username) {
        log.info("Actualizando zona ID: {} por usuario: {}", id, username);

        ZonaPatrullaje zona = zonaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Zona de patrullaje no encontrada: " + id));

        // 1. Validar nombre único dentro del sector
        if (zonaRepository.existsBySectorIdAndNombreIgnoreCaseAndIdNot(zona.getSector().getId(), request.getNombre().trim(), id)) {
            throw new IllegalArgumentException("Ya existe otra zona con el nombre: " + request.getNombre().trim() + " en este sector");
        }

        // 2. Validar vigencia
        if (request.getFechaInicioVigencia() != null && request.getFechaFinVigencia() != null) {
            if (request.getFechaFinVigencia().isBefore(request.getFechaInicioVigencia())) {
                throw new IllegalArgumentException("La fecha fin de vigencia no puede ser anterior a la fecha de inicio");
            }
        }

        zona.setNombre(request.getNombre().trim());
        zona.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        if (request.getPrioridadOperativa() != null) {
            zona.setPrioridadOperativa(request.getPrioridadOperativa());
        }
        zona.setFechaInicioVigencia(request.getFechaInicioVigencia());
        zona.setFechaFinVigencia(request.getFechaFinVigencia());
        zona.setObservaciones(request.getObservaciones());
        zona.setUpdatedBy(username);

        zona = zonaRepository.save(zona);
        return mapearResponse(zona);
    }

    @Override
    @Transactional
    public ZonaPatrullajeResponse cambiarEstado(UUID id, CambioEstadoTerritorioRequest request, String username) {
        log.info("Cambiando estado de zona ID: {} a {} por {}", id, request.getNuevoEstado(), username);

        ZonaPatrullaje zona = zonaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Zona de patrullaje no encontrada: " + id));

        EstadoSectorPatrullaje anterior = zona.getEstado();
        EstadoSectorPatrullaje nuevo = request.getNuevoEstado();

        if (anterior == nuevo) {
            return mapearResponse(zona);
        }

        // Regla institucional: Una zona no puede quedar operativamente activa dentro de un sector inactivo
        if (nuevo == EstadoSectorPatrullaje.ACTIVO) {
            if (zona.getSector().getEstado() != EstadoSectorPatrullaje.ACTIVO) {
                throw new IllegalStateException("No se puede activar la zona porque el sector al que pertenece (" +
                        zona.getSector().getCodigo() + ") se encuentra " + zona.getSector().getEstado().getDescripcion());
            }
        }

        zona.setEstado(nuevo);
        zona.setUpdatedBy(username);
        zona = zonaRepository.save(zona);

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        historialEstadoRepository.save(HistorialEstadoTerritorio.builder()
                .tipoEntidad(TipoEntidadTerritorial.ZONA)
                .entidadId(zona.getId())
                .estadoAnterior(anterior.name())
                .estadoNuevo(nuevo.name())
                .motivo(request.getMotivo().trim())
                .usuarioRegistro(usuario)
                .build());

        return mapearResponse(zona);
    }

    @Override
    @Transactional
    public ZonaPatrullajeResponse reasignarSector(UUID id, ReasignarZonaRequest request, String username) {
        log.info("Reasignando zona ID: {} a nuevo sector ID: {} por {}", id, request.getNuevoSectorId(), username);

        ZonaPatrullaje zona = zonaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Zona de patrullaje no encontrada: " + id));

        SectorPatrullaje sectorNuevo = sectorRepository.findById(request.getNuevoSectorId())
                .orElseThrow(() -> new NoSuchElementException("El nuevo sector de destino no existe: " + request.getNuevoSectorId()));

        SectorPatrullaje sectorAnterior = zona.getSector();

        if (sectorAnterior.getId().equals(sectorNuevo.getId())) {
            throw new IllegalArgumentException("La zona ya pertenece actualmente a este sector");
        }

        // Si la zona está activa, el sector de destino debe estar activo
        if (zona.getEstado() == EstadoSectorPatrullaje.ACTIVO && sectorNuevo.getEstado() != EstadoSectorPatrullaje.ACTIVO) {
            throw new IllegalStateException("No se puede transferir una zona activa a un sector que se encuentra " + sectorNuevo.getEstado().getDescripcion());
        }

        // Validar unicidad en el nuevo sector
        if (zonaRepository.existsBySectorIdAndCodigo(sectorNuevo.getId(), zona.getCodigo())) {
            throw new IllegalArgumentException("Ya existe una zona con el código " + zona.getCodigo() + " en el sector destino " + sectorNuevo.getNombre());
        }

        zona.setSector(sectorNuevo);
        zona.setUpdatedBy(username);
        zona = zonaRepository.save(zona);

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        // Registro de historial de reasignación
        historialReasignacionRepository.save(HistorialReasignacionZona.builder()
                .zona(zona)
                .sectorAnterior(sectorAnterior)
                .sectorNuevo(sectorNuevo)
                .motivo(request.getMotivo().trim())
                .documentoReferencia(request.getDocumentoReferencia())
                .usuarioRegistro(usuario)
                .build());

        log.info("Zona {} reasignada exitosamente del sector {} al sector {}", zona.getCodigo(), sectorAnterior.getCodigo(), sectorNuevo.getCodigo());
        return mapearResponse(zona);
    }

    @Override
    @Transactional
    public ZonaPatrullajeResponse guardarDelimitacion(UUID id, DelimitacionGeoJsonRequest request, String username) {
        log.info("Guardando delimitación geográfica para zona ID: {} por {}", id, username);

        ZonaPatrullaje zona = zonaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Zona de patrullaje no encontrada: " + id));

        ValidacionGeoJsonResult v = geoJsonValidatorService.validar(request.getGeometriaGeojson());
        if (!v.isValido()) {
            throw new IllegalArgumentException("Geometría GeoJSON inválida: " + v.getMensajeError());
        }

        String geoJsonSaneado = request.getGeometriaGeojson().trim();
        zona.setGeometriaGeojson(geoJsonSaneado);
        zona.setTieneDelimitacion(true);
        zona.setUpdatedBy(username);
        zona = zonaRepository.save(zona);

        long countVersiones = historialDelimitacionRepository.countByTipoEntidadAndEntidadId(TipoEntidadTerritorial.ZONA, id);
        int siguienteVersion = (int) (countVersiones + 1);

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        historialDelimitacionRepository.save(HistorialDelimitacionTerritorial.builder()
                .tipoEntidad(TipoEntidadTerritorial.ZONA)
                .entidadId(zona.getId())
                .numeroVersion(siguienteVersion)
                .geometriaGeojson(geoJsonSaneado)
                .motivoCambio(request.getMotivo().trim())
                .documentoReferencia(request.getDocumentoReferencia())
                .usuarioRegistro(usuario)
                .build());

        return mapearResponse(zona);
    }

    @Override
    @Transactional
    public void archivarZona(UUID id, String motivo, String username) {
        cambiarEstado(id, CambioEstadoTerritorioRequest.builder()
                .nuevoEstado(EstadoSectorPatrullaje.ARCHIVADO)
                .motivo(motivo != null ? motivo : "Archivado administrativo de la zona")
                .build(), username);
    }

    private ZonaPatrullajeResponse mapearResponse(ZonaPatrullaje z) {
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

    private HistorialReasignacionResponse mapearHistorialReasignacion(HistorialReasignacionZona h) {
        return HistorialReasignacionResponse.builder()
                .id(h.getId())
                .zonaId(h.getZona().getId())
                .zonaCodigo(h.getZona().getCodigo())
                .zonaNombre(h.getZona().getNombre())
                .sectorAnteriorId(h.getSectorAnterior().getId())
                .sectorAnteriorNombre(h.getSectorAnterior().getNombre())
                .sectorNuevoId(h.getSectorNuevo().getId())
                .sectorNuevoNombre(h.getSectorNuevo().getNombre())
                .motivo(h.getMotivo())
                .documentoReferencia(h.getDocumentoReferencia())
                .usuarioRegistro(h.getUsuarioRegistro() != null ? h.getUsuarioRegistro().getUsername() : null)
                .createdAt(h.getCreatedAt())
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
