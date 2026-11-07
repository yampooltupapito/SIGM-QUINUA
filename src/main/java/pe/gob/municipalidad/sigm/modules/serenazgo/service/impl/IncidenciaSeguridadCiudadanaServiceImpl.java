package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.IncidenciaSeguridadCiudadanaService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidenciaSeguridadCiudadanaServiceImpl implements IncidenciaSeguridadCiudadanaService {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FORMATO_FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final IncidenciaSeguridadCiudadanaRepository incidenciaRepository;
    private final TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final PersonalSerenazgoRepository personalRepository;
    private final ProgramacionServicioSerenazgoRepository programacionRepository;
    private final OficinaRepository oficinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final IncidenciaHistorialRepository historialRepository;
    private final IncidenciaDocumentoRepository documentoRepository;
    private final IncidenciaComentarioRepository comentarioRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<IncidenciaResponse> listarConFiltros(IncidenciaFilter filtro, Pageable pageable) {
        log.debug("Listando incidencias con filtros dinámicos...");

        Specification<IncidenciaSeguridadCiudadana> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro != null) {
                if (filtro.getEstado() != null) {
                    predicates.add(cb.equal(root.get("estado"), filtro.getEstado()));
                }
                if (filtro.getPrioridad() != null) {
                    predicates.add(cb.equal(root.get("prioridad"), filtro.getPrioridad()));
                }
                if (filtro.getTipoId() != null) {
                    predicates.add(cb.equal(root.get("tipoIncidencia").get("id"), filtro.getTipoId()));
                }
                if (filtro.getSectorId() != null) {
                    predicates.add(cb.equal(root.get("sector").get("id"), filtro.getSectorId()));
                }
                if (filtro.getZonaId() != null) {
                    predicates.add(cb.equal(root.get("zona").get("id"), filtro.getZonaId()));
                }
                if (filtro.getConfidencialidad() != null) {
                    predicates.add(cb.equal(root.get("nivelConfidencialidad"), filtro.getConfidencialidad()));
                }
                if (filtro.getDesde() != null) {
                    OffsetDateTime desde = filtro.getDesde().atStartOfDay(ZONA_PERU).toOffsetDateTime();
                    predicates.add(cb.greaterThanOrEqualTo(root.get("fechaReporte"), desde));
                }
                if (filtro.getHasta() != null) {
                    OffsetDateTime hasta = filtro.getHasta().atTime(LocalTime.MAX).atZone(ZONA_PERU).toOffsetDateTime();
                    predicates.add(cb.lessThanOrEqualTo(root.get("fechaReporte"), hasta));
                }
                if (StringUtils.hasText(filtro.getTermino())) {
                    String pattern = "%" + filtro.getTermino().trim().toLowerCase() + "%";
                    Predicate porCodigo = cb.like(cb.lower(root.get("codigo")), pattern);
                    Predicate porTitulo = cb.like(cb.lower(root.get("titulo")), pattern);
                    Predicate porDesc = cb.like(cb.lower(root.get("descripcion")), pattern);
                    Predicate porUbic = cb.like(cb.lower(root.get("ubicacionReferencia")), pattern);
                    predicates.add(cb.or(porCodigo, porTitulo, porDesc, porUbic));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<IncidenciaSeguridadCiudadana> pagina = incidenciaRepository.findAll(spec, pageable);
        return pagina.map(this::mapearResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public IncidenciaDetailResponse obtenerDetallePorId(UUID id, boolean puedeVerDatosSensibles) {
        IncidenciaSeguridadCiudadana inc = incidenciaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Incidencia no encontrada con ID: " + id));

        List<IncidenciaHistorialResponse> hist = historialRepository.findByIncidenciaIdOrderByCreatedAtAsc(id).stream()
                .map(this::mapearHistorialResponse)
                .collect(Collectors.toList());

        List<IncidenciaDocumentoResponse> docs = documentoRepository.findByIncidenciaIdOrderByCreatedAtDesc(id).stream()
                .map(this::mapearDocumentoResponse)
                .collect(Collectors.toList());

        List<IncidenciaComentarioResponse> coms = comentarioRepository.findByIncidenciaIdOrderByCreatedAtAsc(id).stream()
                .map(this::mapearComentarioResponse)
                .collect(Collectors.toList());

        // Enmascarar información personal si es anónimo o el operador no tiene permisos suficientes
        String nombreRep = inc.getReportanteNombre();
        String telfRep = inc.getReportanteTelefono();
        String dirRep = inc.getReportanteDireccion();

        if (!puedeVerDatosSensibles) {
            nombreRep = "[RESERVADO / CONFIDENCIAL]";
            telfRep = "[OCULTO]";
            dirRep = "[RESERVADO / CONFIDENCIAL]";
        } else if (Boolean.TRUE.equals(inc.getReportanteAnonimo())) {
            nombreRep = "[CIUDADANO ANÓNIMO]";
            telfRep = enmascararTelefono(telfRep);
            dirRep = "[DATOS PROTEGIDOS]";
        }

        return IncidenciaDetailResponse.builder()
                .id(inc.getId())
                .codigo(inc.getCodigo())
                .titulo(inc.getTitulo())
                .descripcion(inc.getDescripcion())
                .tipoId(inc.getTipoIncidencia().getId())
                .tipoNombre(inc.getTipoIncidencia().getNombre())
                .tipoCodigo(inc.getTipoIncidencia().getCodigo())
                .categoriaDesc(inc.getTipoIncidencia().getCategoria() != null ? inc.getTipoIncidencia().getCategoria().getDescripcion() : "General")
                .prioridad(inc.getPrioridad())
                .prioridadDesc(inc.getPrioridad().getDescripcion())
                .estado(inc.getEstado())
                .estadoDesc(inc.getEstado().getDescripcion())
                .origenHecho(inc.getOrigenHecho())
                .origenHechoDesc(inc.getOrigenHecho().getDescripcion())
                .canalReporte(inc.getCanalReporte())
                .canalReporteDesc(inc.getCanalReporte().getDescripcion())
                .sectorId(inc.getSector() != null ? inc.getSector().getId() : null)
                .sectorNombre(inc.getSector() != null ? inc.getSector().getNombre() : "Sin asignar")
                .sectorCodigo(inc.getSector() != null ? inc.getSector().getCodigo() : null)
                .sectorColorHex(inc.getSector() != null ? inc.getSector().getColorHex() : "#3b82f6")
                .zonaId(inc.getZona() != null ? inc.getZona().getId() : null)
                .zonaNombre(inc.getZona() != null ? inc.getZona().getNombre() : null)
                .zonaCodigo(inc.getZona() != null ? inc.getZona().getCodigo() : null)
                .ubicacionReferencia(inc.getUbicacionReferencia())
                .latitud(inc.getLatitud())
                .longitud(inc.getLongitud())
                .oficinaResponsableId(inc.getOficinaResponsable() != null ? inc.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(inc.getOficinaResponsable() != null ? inc.getOficinaResponsable().getNombre() : "Seguridad Ciudadana Quinua")
                .usuarioRegistroNombre(inc.getUsuarioRegistro() != null ? inc.getUsuarioRegistro().getUsername() : "Sistema")
                .responsableId(inc.getResponsable() != null ? inc.getResponsable().getId() : null)
                .responsableNombre(inc.getResponsable() != null && inc.getResponsable().getPersona() != null ? inc.getResponsable().getPersona().getNombreCompleto() : "Por asignar")
                .personalReportaId(inc.getPersonalReporta() != null ? inc.getPersonalReporta().getId() : null)
                .personalReportaNombre(inc.getPersonalReporta() != null && inc.getPersonalReporta().getPersona() != null ? inc.getPersonalReporta().getPersona().getNombreCompleto() : null)
                .programacionServicioId(inc.getProgramacionServicio() != null ? inc.getProgramacionServicio().getId() : null)
                .programacionCodigo(inc.getProgramacionServicio() != null ? inc.getProgramacionServicio().getCodigo() : null)
                .nivelConfidencialidad(inc.getNivelConfidencialidad())
                .reportanteAnonimo(inc.getReportanteAnonimo())
                .reportanteNombre(nombreRep)
                .reportanteTelefono(telfRep)
                .reportanteDireccion(dirRep)
                .observacionesInternas(inc.getObservacionesInternas())
                .resultadoAtencion(inc.getResultadoAtencion())
                .motivoCierre(inc.getMotivoCierre())
                .motivoDescarte(inc.getMotivoDescarte())
                .fechaCierre(inc.getFechaCierre())
                .fechaReporte(inc.getFechaReporte())
                .fechaOcurrencia(inc.getFechaOcurrencia())
                .version(inc.getVersion())
                .createdAt(inc.getCreatedAt())
                .updatedAt(inc.getUpdatedAt())
                .createdBy(inc.getCreatedBy())
                .updatedBy(inc.getUpdatedBy())
                .historial(hist)
                .documentos(docs)
                .comentarios(coms)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public IncidenciaResponse obtenerPorCodigo(String codigo) {
        return incidenciaRepository.findByCodigo(codigo)
                .map(this::mapearResponse)
                .orElseThrow(() -> new NoSuchElementException("Incidencia no encontrada con código: " + codigo));
    }

    @Override
    @Transactional
    public IncidenciaResponse registrarIncidencia(IncidenciaCreateRequest request, String username) {
        log.info("Registrando nueva incidencia por usuario: {}", username);

        TipoIncidenciaSeguridad tipo = tipoIncidenciaRepository.findById(request.getTipoIncidenciaId())
                .orElseThrow(() -> new NoSuchElementException("Tipo de incidencia no encontrado con ID: " + request.getTipoIncidenciaId()));

        SectorPatrullaje sector = null;
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId())
                    .orElseThrow(() -> new NoSuchElementException("Sector no encontrado con ID: " + request.getSectorId()));
        }

        ZonaPatrullaje zona = null;
        if (request.getZonaId() != null) {
            zona = zonaRepository.findById(request.getZonaId())
                    .orElseThrow(() -> new NoSuchElementException("Zona no encontrada con ID: " + request.getZonaId()));
            if (sector != null && zona.getSector() != null && !zona.getSector().getId().equals(sector.getId())) {
                throw new IllegalStateException("La zona seleccionada no pertenece al sector indicado: " + sector.getNombre());
            }
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Usuario no autenticado: " + username));

        PersonalSerenazgo personalReporta = null;
        if (request.getPersonalReportaId() != null) {
            personalReporta = personalRepository.findById(request.getPersonalReportaId()).orElse(null);
        }

        ProgramacionServicioSerenazgo prog = null;
        if (request.getProgramacionServicioId() != null) {
            prog = programacionRepository.findById(request.getProgramacionServicioId()).orElse(null);
        }

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId()).orElse(null);
        }

        String codigoGenerado = generarCodigoIncidencia();

        IncidenciaSeguridadCiudadana inc = IncidenciaSeguridadCiudadana.builder()
                .codigo(codigoGenerado)
                .titulo(request.getTitulo() != null && !request.getTitulo().trim().isEmpty() ? request.getTitulo().trim() : ("Incidencia - " + tipo.getNombre()))
                .tipoIncidencia(tipo)
                .prioridad(request.getPrioridad() != null ? request.getPrioridad() : PrioridadIncidenciaSeguridad.MEDIA)
                .descripcion(request.getDescripcion().trim())
                .fechaReporte(OffsetDateTime.now(ZONA_PERU))
                .fechaOcurrencia(request.getFechaHoraOcurrencia() != null ? request.getFechaHoraOcurrencia() : OffsetDateTime.now(ZONA_PERU))
                .origenHecho(request.getOrigenHecho() != null ? request.getOrigenHecho() : OrigenHechoIncidencia.HECHO_REPORTADO)
                .canalReporte(request.getCanalReporte() != null ? request.getCanalReporte() : CanalReporteIncidencia.LLAMADA_OPERADOR)
                .sector(sector)
                .zona(zona)
                .ubicacionReferencia(request.getUbicacionReferencia().trim())
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .estado(EstadoIncidenciaSeguridad.REGISTRADA)
                .oficinaResponsable(oficina)
                .usuarioRegistro(usuario)
                .personalReporta(personalReporta)
                .programacionServicio(prog)
                .nivelConfidencialidad(request.getNivelConfidencialidad() != null ? request.getNivelConfidencialidad() : NivelConfidencialidad.ORDINARIO)
                .reportanteAnonimo(Boolean.TRUE.equals(request.getReportanteAnonimo()))
                .reportanteNombre(Boolean.TRUE.equals(request.getReportanteAnonimo()) ? null : request.getReportanteNombre())
                .reportanteTelefono(Boolean.TRUE.equals(request.getReportanteAnonimo()) ? null : request.getReportanteTelefono())
                .reportanteDireccion(Boolean.TRUE.equals(request.getReportanteAnonimo()) ? null : request.getReportanteDireccion())
                .observacionesInternas(request.getObservacionesInternas())
                .createdBy(username)
                .build();

        inc = incidenciaRepository.save(inc);

        // Registrar bitácora inicial
        historialRepository.save(IncidenciaHistorial.builder()
                .incidencia(inc)
                .tipoEvento(TipoEventoHistorialIncidencia.REGISTRO)
                .estadoNuevo(EstadoIncidenciaSeguridad.REGISTRADA.name())
                .prioridadNueva(inc.getPrioridad().name())
                .motivo("Recepción y registro inicial de la incidencia comunicada")
                .usuario(usuario)
                .build());

        // Si se agregó observación interna, registrar como comentario inicial
        if (request.getObservacionesInternas() != null && !request.getObservacionesInternas().trim().isEmpty()) {
            comentarioRepository.save(IncidenciaComentario.builder()
                    .incidencia(inc)
                    .comentario(request.getObservacionesInternas().trim())
                    .esInterno(true)
                    .usuario(usuario)
                    .build());
        }

        log.info("Incidencia registrada con código: {}", inc.getCodigo());
        return mapearResponse(inc);
    }

    @Override
    @Transactional
    public IncidenciaResponse actualizarIncidencia(UUID id, IncidenciaUpdateRequest request, String username) {
        log.info("Actualizando incidencia ID: {} por usuario: {}", id, username);

        IncidenciaSeguridadCiudadana inc = incidenciaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Incidencia no encontrada con ID: " + id));

        if (inc.getEstado() == EstadoIncidenciaSeguridad.CERRADA || inc.getEstado() == EstadoIncidenciaSeguridad.DESCARTADA) {
            throw new IllegalStateException("No se puede modificar una incidencia que ya ha sido cerrada o descartada");
        }

        TipoIncidenciaSeguridad tipo = tipoIncidenciaRepository.findById(request.getTipoIncidenciaId())
                .orElseThrow(() -> new NoSuchElementException("Tipo no encontrado con ID: " + request.getTipoIncidenciaId()));

        SectorPatrullaje sector = null;
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId())
                    .orElseThrow(() -> new NoSuchElementException("Sector no encontrado con ID: " + request.getSectorId()));
        }

        ZonaPatrullaje zona = null;
        if (request.getZonaId() != null) {
            zona = zonaRepository.findById(request.getZonaId())
                    .orElseThrow(() -> new NoSuchElementException("Zona no encontrada con ID: " + request.getZonaId()));
            if (sector != null && zona.getSector() != null && !zona.getSector().getId().equals(sector.getId())) {
                throw new IllegalStateException("La zona indicada no pertenece al sector seleccionado");
            }
        }

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        inc.setTitulo(request.getTitulo().trim());
        inc.setTipoIncidencia(tipo);
        inc.setDescripcion(request.getDescripcion().trim());
        if (request.getFechaHoraOcurrencia() != null) {
            inc.setFechaOcurrencia(request.getFechaHoraOcurrencia());
        }
        if (request.getOrigenHecho() != null) {
            inc.setOrigenHecho(request.getOrigenHecho());
        }
        if (request.getCanalReporte() != null) {
            inc.setCanalReporte(request.getCanalReporte());
        }
        inc.setSector(sector);
        inc.setZona(zona);
        inc.setUbicacionReferencia(request.getUbicacionReferencia().trim());
        inc.setLatitud(request.getLatitud());
        inc.setLongitud(request.getLongitud());

        if (request.getOficinaResponsableId() != null) {
            inc.setOficinaResponsable(oficinaRepository.findById(request.getOficinaResponsableId()).orElse(null));
        }
        if (request.getPersonalReportaId() != null) {
            inc.setPersonalReporta(personalRepository.findById(request.getPersonalReportaId()).orElse(null));
        }
        if (request.getProgramacionServicioId() != null) {
            inc.setProgramacionServicio(programacionRepository.findById(request.getProgramacionServicioId()).orElse(null));
        }
        if (request.getNivelConfidencialidad() != null) {
            inc.setNivelConfidencialidad(request.getNivelConfidencialidad());
        }
        if (request.getReportanteAnonimo() != null) {
            inc.setReportanteAnonimo(request.getReportanteAnonimo());
            if (Boolean.TRUE.equals(request.getReportanteAnonimo())) {
                inc.setReportanteNombre(null);
                inc.setReportanteTelefono(null);
                inc.setReportanteDireccion(null);
            } else {
                inc.setReportanteNombre(request.getReportanteNombre());
                inc.setReportanteTelefono(request.getReportanteTelefono());
                inc.setReportanteDireccion(request.getReportanteDireccion());
            }
        }
        if (request.getObservacionesInternas() != null) {
            inc.setObservacionesInternas(request.getObservacionesInternas().trim());
        }

        inc.setUpdatedBy(username);
        inc = incidenciaRepository.save(inc);

        historialRepository.save(IncidenciaHistorial.builder()
                .incidencia(inc)
                .tipoEvento(TipoEventoHistorialIncidencia.ACTUALIZACION)
                .motivo(request.getMotivoModificacion() != null ? request.getMotivoModificacion().trim() : "Actualización de datos informativos de la incidencia")
                .usuario(usuario)
                .build());

        return mapearResponse(inc);
    }

    @Override
    @Transactional
    public IncidenciaResponse cambiarEstado(UUID id, CambioEstadoIncidenciaRequest request, String username) {
        log.info("Cambiando estado de incidencia ID: {} a {} por: {}", id, request.getNuevoEstado(), username);

        IncidenciaSeguridadCiudadana inc = incidenciaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Incidencia no encontrada con ID: " + id));

        if (inc.getEstado() == request.getNuevoEstado()) {
            return mapearResponse(inc);
        }

        if (inc.getEstado() == EstadoIncidenciaSeguridad.CERRADA) {
            throw new IllegalStateException("Una incidencia cerrada formalmente no puede ser reabierta sin procedimiento de reapertura");
        }

        EstadoIncidenciaSeguridad anterior = inc.getEstado();
        inc.setEstado(request.getNuevoEstado());
        inc.setUpdatedBy(username);

        if (request.getNuevoEstado() == EstadoIncidenciaSeguridad.CERRADA) {
            inc.setFechaCierre(OffsetDateTime.now(ZONA_PERU));
            inc.setMotivoCierre(request.getMotivo());
        } else if (request.getNuevoEstado() == EstadoIncidenciaSeguridad.DESCARTADA) {
            inc.setMotivoDescarte(request.getMotivo());
        }

        inc = incidenciaRepository.save(inc);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(IncidenciaHistorial.builder()
                .incidencia(inc)
                .tipoEvento(TipoEventoHistorialIncidencia.CAMBIO_ESTADO)
                .estadoAnterior(anterior.name())
                .estadoNuevo(request.getNuevoEstado().name())
                .motivo(request.getMotivo().trim())
                .usuario(u)
                .build());

        return mapearResponse(inc);
    }

    @Override
    @Transactional
    public IncidenciaResponse cambiarPrioridad(UUID id, CambioPrioridadIncidenciaRequest request, String username) {
        log.info("Cambiando prioridad de incidencia ID: {} a {} por: {}", id, request.getNuevaPrioridad(), username);

        IncidenciaSeguridadCiudadana inc = incidenciaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Incidencia no encontrada con ID: " + id));

        if (inc.getPrioridad() == request.getNuevaPrioridad()) {
            return mapearResponse(inc);
        }

        PrioridadIncidenciaSeguridad anterior = inc.getPrioridad();
        inc.setPrioridad(request.getNuevaPrioridad());
        inc.setUpdatedBy(username);
        inc = incidenciaRepository.save(inc);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(IncidenciaHistorial.builder()
                .incidencia(inc)
                .tipoEvento(TipoEventoHistorialIncidencia.CAMBIO_PRIORIDAD)
                .prioridadAnterior(anterior.name())
                .prioridadNueva(request.getNuevaPrioridad().name())
                .motivo(request.getJustificacion().trim())
                .usuario(u)
                .build());

        return mapearResponse(inc);
    }

    @Override
    @Transactional
    public IncidenciaResponse cerrarIncidencia(UUID id, CierreIncidenciaRequest request, String username) {
        log.info("Cerrando administrativamente la incidencia ID: {} por: {}", id, username);

        IncidenciaSeguridadCiudadana inc = incidenciaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Incidencia no encontrada con ID: " + id));

        if (inc.getEstado() == EstadoIncidenciaSeguridad.CERRADA) {
            return mapearResponse(inc);
        }

        EstadoIncidenciaSeguridad anterior = inc.getEstado();
        inc.setEstado(EstadoIncidenciaSeguridad.CERRADA);
        inc.setFechaCierre(OffsetDateTime.now(ZONA_PERU));
        inc.setMotivoCierre(request.getMotivoCierre().trim());
        inc.setResultadoAtencion(request.getResultadoAtencion().trim());
        inc.setUpdatedBy(username);
        inc = incidenciaRepository.save(inc);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(IncidenciaHistorial.builder()
                .incidencia(inc)
                .tipoEvento(TipoEventoHistorialIncidencia.CIERRE_ADMINISTRATIVO)
                .estadoAnterior(anterior.name())
                .estadoNuevo(EstadoIncidenciaSeguridad.CERRADA.name())
                .motivo("Cierre administrativo: " + request.getMotivoCierre().trim() + ". Resultado: " + request.getResultadoAtencion().trim())
                .usuario(u)
                .build());

        return mapearResponse(inc);
    }

    @Override
    @Transactional
    public IncidenciaResponse descartarIncidencia(UUID id, DescarteIncidenciaRequest request, String username) {
        log.info("Descartando incidencia ID: {} por: {}", id, username);

        IncidenciaSeguridadCiudadana inc = incidenciaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Incidencia no encontrada con ID: " + id));

        EstadoIncidenciaSeguridad anterior = inc.getEstado();
        inc.setEstado(EstadoIncidenciaSeguridad.DESCARTADA);
        inc.setMotivoDescarte(request.getMotivoDescarte().trim());
        inc.setUpdatedBy(username);
        inc = incidenciaRepository.save(inc);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(IncidenciaHistorial.builder()
                .incidencia(inc)
                .tipoEvento(TipoEventoHistorialIncidencia.DESCARTE)
                .estadoAnterior(anterior.name())
                .estadoNuevo(EstadoIncidenciaSeguridad.DESCARTADA.name())
                .motivo("Descarte administrativo: " + request.getMotivoDescarte().trim())
                .usuario(u)
                .build());

        return mapearResponse(inc);
    }

    @Override
    @Transactional
    public IncidenciaComentarioResponse agregarComentario(UUID id, AgregarComentarioRequest request, String username) {
        IncidenciaSeguridadCiudadana inc = incidenciaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Incidencia no encontrada con ID: " + id));

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);

        IncidenciaComentario com = IncidenciaComentario.builder()
                .incidencia(inc)
                .comentario(request.getComentario().trim())
                .esInterno(Boolean.TRUE.equals(request.getEsInterno()))
                .usuario(u)
                .build();

        com = comentarioRepository.save(com);

        historialRepository.save(IncidenciaHistorial.builder()
                .incidencia(inc)
                .tipoEvento(TipoEventoHistorialIncidencia.COMENTARIO_AGREGADO)
                .motivo("Nueva nota de seguimiento registrada: " + (request.getComentario().length() > 60 ? request.getComentario().substring(0, 57) + "..." : request.getComentario()))
                .usuario(u)
                .build());

        return mapearComentarioResponse(com);
    }

    @Override
    @Transactional
    public IncidenciaDocumentoResponse adjuntarDocumento(UUID id, String nombreArchivo, String mime, byte[] contenido, String descripcion, boolean esFoto, String username) {
        IncidenciaSeguridadCiudadana inc = incidenciaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Incidencia no encontrada con ID: " + id));

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);

        String hash = calcularSha256(contenido);
        String rutaSegura = "/storage/serenazgo/incidencias/" + inc.getCodigo() + "/" + UUID.randomUUID() + "_" + nombreArchivo;

        IncidenciaDocumento doc = IncidenciaDocumento.builder()
                .incidencia(inc)
                .nombreArchivo(nombreArchivo)
                .tipoMime(mime != null ? mime : "application/octet-stream")
                .tamanoBytes((long) contenido.length)
                .rutaArchivo(rutaSegura)
                .hashSha256(hash)
                .descripcion(descripcion != null ? descripcion.trim() : null)
                .esFotografia(esFoto || (mime != null && mime.startsWith("image/")))
                .usuario(u)
                .build();

        doc = documentoRepository.save(doc);

        historialRepository.save(IncidenciaHistorial.builder()
                .incidencia(inc)
                .tipoEvento(TipoEventoHistorialIncidencia.ADJUNCION_DOCUMENTO)
                .motivo("Archivo adjuntado: " + nombreArchivo + " (" + (contenido.length / 1024) + " KB)")
                .usuario(u)
                .build());

        return mapearDocumentoResponse(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PosibleDuplicadoResponse> buscarPosiblesDuplicados(UUID tipoId, UUID sectorId, OffsetDateTime fecha, UUID excluirId) {
        if (tipoId == null) return Collections.emptyList();

        OffsetDateTime base = fecha != null ? fecha : OffsetDateTime.now(ZONA_PERU);
        OffsetDateTime desde = base.minusHours(24);
        OffsetDateTime hasta = base.plusHours(24);

        Specification<IncidenciaSeguridadCiudadana> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tipoIncidencia").get("id"), tipoId));
            predicates.add(cb.greaterThanOrEqualTo(root.get("fechaReporte"), desde));
            predicates.add(cb.lessThanOrEqualTo(root.get("fechaReporte"), hasta));
            if (sectorId != null) {
                predicates.add(cb.equal(root.get("sector").get("id"), sectorId));
            }
            if (excluirId != null) {
                predicates.add(cb.notEqual(root.get("id"), excluirId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<IncidenciaSeguridadCiudadana> posibles = incidenciaRepository.findAll(spec);

        return posibles.stream().map(p -> PosibleDuplicadoResponse.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .titulo(p.getTitulo())
                .tipoNombre(p.getTipoIncidencia().getNombre())
                .sectorNombre(p.getSector() != null ? p.getSector().getNombre() : "General")
                .ubicacionReferencia(p.getUbicacionReferencia())
                .estado(p.getEstado())
                .prioridad(p.getPrioridad())
                .fechaReporte(p.getFechaReporte())
                .motivoAdvertencia("Mismo tipo de hecho y sector en una ventana de 24 horas")
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardIncidenciasResponse obtenerDashboardIncidencias() {
        OffsetDateTime ahora = OffsetDateTime.now(ZONA_PERU);
        OffsetDateTime inicioHoy = ahora.toLocalDate().atStartOfDay(ZONA_PERU).toOffsetDateTime();
        OffsetDateTime inicioSemana = ahora.minusDays(ahora.getDayOfWeek().getValue() - 1L).toLocalDate().atStartOfDay(ZONA_PERU).toOffsetDateTime();
        OffsetDateTime inicioMes = ahora.toLocalDate().withDayOfMonth(1).atStartOfDay(ZONA_PERU).toOffsetDateTime();

        return DashboardIncidenciasResponse.builder()
                .totalIncidencias(incidenciaRepository.count())
                .registradas(incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.REGISTRADA))
                .enEvaluacion(incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.EN_EVALUACION))
                .asignadas(incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.ASIGNADA))
                .enAtencion(incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.EN_ATENCION))
                .resueltas(incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.RESUELTA))
                .cerradas(incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.CERRADA))
                .descartadas(incidenciaRepository.countByEstado(EstadoIncidenciaSeguridad.DESCARTADA))
                .prioridadBaja(incidenciaRepository.countByPrioridad(PrioridadIncidenciaSeguridad.BAJA))
                .prioridadMedia(incidenciaRepository.countByPrioridad(PrioridadIncidenciaSeguridad.MEDIA))
                .prioridadAlta(incidenciaRepository.countByPrioridad(PrioridadIncidenciaSeguridad.ALTA))
                .prioridadCritica(incidenciaRepository.countByPrioridad(PrioridadIncidenciaSeguridad.CRITICA))
                .hoy(incidenciaRepository.countByFechaReporteBetween(inicioHoy, ahora))
                .estaSemana(incidenciaRepository.countByFechaReporteBetween(inicioSemana, ahora))
                .esteMes(incidenciaRepository.countByFechaReporteBetween(inicioMes, ahora))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarFichaPdf(UUID id) {
        IncidenciaDetailResponse inc = obtenerDetallePorId(id, false);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Tipografías
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.NORMAL, new java.awt.Color(16, 24, 39));
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, new java.awt.Color(71, 85, 105));
            Font fontSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL, new java.awt.Color(30, 41, 59));
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.NORMAL, java.awt.Color.WHITE);
            Font fontBody = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, java.awt.Color.BLACK);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.NORMAL, java.awt.Color.BLACK);

            // Encabezado
            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(pHeader);

            Paragraph pSub = new Paragraph("Gerencia de Seguridad Ciudadana — Serenazgo Municipal", fontSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            document.add(pSub);

            Paragraph pDoc = new Paragraph("FICHA TÉCNICA DE INCIDENCIA Nº " + inc.getCodigo(), fontTitulo);
            pDoc.setAlignment(Element.ALIGN_CENTER);
            pDoc.setSpacingBefore(8);
            pDoc.setSpacingAfter(12);
            document.add(pDoc);

            // Tabla de Datos Principales
            PdfPTable tGral = new PdfPTable(4);
            tGral.setWidthPercentage(100);
            tGral.setWidths(new float[]{20, 30, 20, 30});

            agregarCeldaClaveValor(tGral, "Código:", inc.getCodigo(), fontBold, fontBody);
            agregarCeldaClaveValor(tGral, "Estado:", inc.getEstadoDesc(), fontBold, fontBody);
            agregarCeldaClaveValor(tGral, "Prioridad:", inc.getPrioridadDesc(), fontBold, fontBody);
            agregarCeldaClaveValor(tGral, "Canal Reporte:", inc.getCanalReporteDesc(), fontBold, fontBody);
            agregarCeldaClaveValor(tGral, "Tipo de Hecho:", inc.getTipoNombre(), fontBold, fontBody);
            agregarCeldaClaveValor(tGral, "Fecha Ocurrencia:", inc.getFechaOcurrencia() != null ? inc.getFechaOcurrencia().format(FORMATO_FECHA_HORA) : "No especificada", fontBold, fontBody);
            agregarCeldaClaveValor(tGral, "Sector Patrullaje:", inc.getSectorNombre(), fontBold, fontBody);
            agregarCeldaClaveValor(tGral, "Zona:", inc.getZonaNombre() != null ? inc.getZonaNombre() : "Todo el sector", fontBold, fontBody);
            agregarCeldaClaveValor(tGral, "Ubicación:", inc.getUbicacionReferencia(), fontBold, fontBody);
            agregarCeldaClaveValor(tGral, "Registrador:", inc.getUsuarioRegistroNombre(), fontBold, fontBody);

            document.add(tGral);

            // Descripción de Hechos
            Paragraph pDescTit = new Paragraph("DESCRIPCIÓN DE LOS HECHOS COMUNICADOS U OBSERVADOS", fontSeccion);
            pDescTit.setSpacingBefore(10);
            pDescTit.setSpacingAfter(4);
            document.add(pDescTit);

            PdfPTable tDesc = new PdfPTable(1);
            tDesc.setWidthPercentage(100);
            PdfPCell cDesc = new PdfPCell(new Phrase(inc.getDescripcion(), fontBody));
            cDesc.setPadding(6);
            cDesc.setBackgroundColor(new java.awt.Color(248, 250, 252));
            tDesc.addCell(cDesc);
            document.add(tDesc);

            // Datos del Reportante
            Paragraph pRepTit = new Paragraph("DATOS DEL REPORTANTE Y CONFIDENCIALIDAD", fontSeccion);
            pRepTit.setSpacingBefore(10);
            pRepTit.setSpacingAfter(4);
            document.add(pRepTit);

            PdfPTable tRep = new PdfPTable(4);
            tRep.setWidthPercentage(100);
            tRep.setWidths(new float[]{20, 30, 20, 30});
            agregarCeldaClaveValor(tRep, "Condición:", Boolean.TRUE.equals(inc.getReportanteAnonimo()) ? "ANÓNIMO" : "IDENTIFICADO", fontBold, fontBody);
            agregarCeldaClaveValor(tRep, "Nombre Reportante:", inc.getReportanteNombre(), fontBold, fontBody);
            agregarCeldaClaveValor(tRep, "Teléfono:", inc.getReportanteTelefono(), fontBold, fontBody);
            agregarCeldaClaveValor(tRep, "Nivel Acceso:", inc.getNivelConfidencialidad().getDescripcion(), fontBold, fontBody);
            document.add(tRep);

            // Historial Cronológico
            Paragraph pHistTit = new Paragraph("LÍNEA DE TIEMPO Y ACTUACIONES ADMINISTRATIVAS", fontSeccion);
            pHistTit.setSpacingBefore(10);
            pHistTit.setSpacingAfter(4);
            document.add(pHistTit);

            PdfPTable tHist = new PdfPTable(4);
            tHist.setWidthPercentage(100);
            tHist.setWidths(new float[]{22, 22, 42, 14});

            agregarCeldaHeader(tHist, "Fecha / Hora", fontHeader);
            agregarCeldaHeader(tHist, "Evento", fontHeader);
            agregarCeldaHeader(tHist, "Motivo / Actuación", fontHeader);
            agregarCeldaHeader(tHist, "Usuario", fontHeader);

            if (inc.getHistorial().isEmpty()) {
                PdfPCell cVac = new PdfPCell(new Phrase("Sin movimientos registrados", fontBody));
                cVac.setColspan(4);
                cVac.setPadding(4);
                tHist.addCell(cVac);
            } else {
                for (IncidenciaHistorialResponse h : inc.getHistorial()) {
                    tHist.addCell(crearCelda(h.getCreatedAt().format(FORMATO_FECHA_HORA), fontBody, Element.ALIGN_CENTER));
                    tHist.addCell(crearCelda(h.getTipoEventoDesc(), fontBody, Element.ALIGN_LEFT));
                    tHist.addCell(crearCelda(h.getMotivo(), fontBody, Element.ALIGN_LEFT));
                    tHist.addCell(crearCelda(h.getUsuarioNombre(), fontBody, Element.ALIGN_CENTER));
                }
            }
            document.add(tHist);

            // Cierre Administrativo
            if (inc.getEstado() == EstadoIncidenciaSeguridad.CERRADA) {
                Paragraph pCierreTit = new Paragraph("CONSTANCIA DE CIERRE ADMINISTRATIVO", fontSeccion);
                pCierreTit.setSpacingBefore(10);
                pCierreTit.setSpacingAfter(4);
                document.add(pCierreTit);

                PdfPTable tCierre = new PdfPTable(2);
                tCierre.setWidthPercentage(100);
                tCierre.setWidths(new float[]{30, 70});
                agregarCeldaClaveValor(tCierre, "Fecha Cierre:", inc.getFechaCierre() != null ? inc.getFechaCierre().format(FORMATO_FECHA_HORA) : "-", fontBold, fontBody);
                agregarCeldaClaveValor(tCierre, "Motivo Cierre:", inc.getMotivoCierre(), fontBold, fontBody);
                agregarCeldaClaveValor(tCierre, "Resultado Atención:", inc.getResultadoAtencion(), fontBold, fontBody);
                document.add(tCierre);
            }

            // Cláusula Legal Institucional
            Paragraph pLegal = new Paragraph(
                    "NOTA LEGAL: El Serenazgo municipal no posee facultades policiales, judiciales ni fiscales. El presente documento registra " +
                            "hechos comunicados u observados en el ejercicio de las competencias municipales de seguridad ciudadana conforme a la Ley N° 27933 y " +
                            "Ley N° 31297. No determina culpabilidades penales ni reemplaza atestados policiales.",
                    FontFactory.getFont(FontFactory.HELVETICA, 7, Font.ITALIC, new java.awt.Color(100, 116, 139))
            );
            pLegal.setSpacingBefore(12);
            document.add(pLegal);

            // Firmas
            PdfPTable tFirma = new PdfPTable(2);
            tFirma.setWidthPercentage(100);
            tFirma.setSpacingBefore(30);

            PdfPCell cFirma1 = new PdfPCell(new Phrase("\n\n_____________________________________\nOPERADOR DE CENTRAL / REGISTRADOR\nSerenazgo Municipal de Quinua", fontBody));
            cFirma1.setBorder(Rectangle.NO_BORDER);
            cFirma1.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell cFirma2 = new PdfPCell(new Phrase("\n\n_____________________________________\nSUPERVISOR DE SEGURIDAD CIUDADANA\nGerencia de Servicios Municipales", fontBody));
            cFirma2.setBorder(Rectangle.NO_BORDER);
            cFirma2.setHorizontalAlignment(Element.ALIGN_CENTER);

            tFirma.addCell(cFirma1);
            tFirma.addCell(cFirma2);
            document.add(tFirma);

            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Error al generar PDF de incidencia ID: {}", id, e);
            throw new RuntimeException("Error en la compilación del reporte PDF", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarIncidenciasExcel(LocalDate desde, LocalDate hasta) {
        OffsetDateTime fDesde = desde != null ? desde.atStartOfDay(ZONA_PERU).toOffsetDateTime() : null;
        OffsetDateTime fHasta = hasta != null ? hasta.atTime(LocalTime.MAX).atZone(ZONA_PERU).toOffsetDateTime() : null;

        Specification<IncidenciaSeguridadCiudadana> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (fDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaReporte"), fDesde));
            }
            if (fHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaReporte"), fHasta));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<IncidenciaSeguridadCiudadana> lista = incidenciaRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "fechaReporte"));

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Incidencias Serenazgo");

            // Estilos
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));

            String[] columnas = {
                    "Código", "Fecha Reporte", "Fecha Ocurrencia", "Título", "Tipo",
                    "Prioridad", "Estado", "Sector", "Zona", "Ubicación",
                    "Latitud", "Longitud", "Canal", "Reportante (Condición)"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (IncidenciaSeguridadCiudadana inc : lista) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(sanitizar(inc.getCodigo()));
                row.createCell(1).setCellValue(inc.getFechaReporte().format(FORMATO_FECHA_HORA));
                row.createCell(2).setCellValue(inc.getFechaOcurrencia() != null ? inc.getFechaOcurrencia().format(FORMATO_FECHA_HORA) : "");
                row.createCell(3).setCellValue(sanitizar(inc.getTitulo()));
                row.createCell(4).setCellValue(sanitizar(inc.getTipoIncidencia().getNombre()));
                row.createCell(5).setCellValue(inc.getPrioridad().name());
                row.createCell(6).setCellValue(inc.getEstado().name());
                row.createCell(7).setCellValue(inc.getSector() != null ? sanitizar(inc.getSector().getNombre()) : "Sin Sector");
                row.createCell(8).setCellValue(inc.getZona() != null ? sanitizar(inc.getZona().getNombre()) : "");
                row.createCell(9).setCellValue(sanitizar(inc.getUbicacionReferencia()));
                row.createCell(10).setCellValue(inc.getLatitud() != null ? inc.getLatitud().doubleValue() : 0.0);
                row.createCell(11).setCellValue(inc.getLongitud() != null ? inc.getLongitud().doubleValue() : 0.0);
                row.createCell(12).setCellValue(inc.getCanalReporte().getDescripcion());
                row.createCell(13).setCellValue(Boolean.TRUE.equals(inc.getReportanteAnonimo()) ? "ANÓNIMO" : "IDENTIFICADO (PROTEGIDO)");
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error al exportar libro Excel de incidencias", e);
            throw new RuntimeException("Error en la generación de reporte Excel", e);
        }
    }

    // ==============================================================================
    // Métodos Utilitarios y de Mapeo
    // ==============================================================================

    private String generarCodigoIncidencia() {
        int anio = LocalDate.now(ZONA_PERU).getYear();
        Long correlativo = incidenciaRepository.obtenerSiguienteValorSecuencia();
        return String.format("INC-%d-%05d", anio, correlativo);
    }

    private IncidenciaResponse mapearResponse(IncidenciaSeguridadCiudadana inc) {
        int docsCount = inc.getDocumentos() != null && !inc.getDocumentos().isEmpty() ? inc.getDocumentos().size() : (int) documentoRepository.countByIncidenciaId(inc.getId());

        return IncidenciaResponse.builder()
                .id(inc.getId())
                .codigo(inc.getCodigo())
                .titulo(inc.getTitulo())
                .descripcion(inc.getDescripcion())
                .tipoId(inc.getTipoIncidencia().getId())
                .tipoNombre(inc.getTipoIncidencia().getNombre())
                .tipoCodigo(inc.getTipoIncidencia().getCodigo())
                .prioridad(inc.getPrioridad())
                .prioridadDesc(inc.getPrioridad().getDescripcion())
                .estado(inc.getEstado())
                .estadoDesc(inc.getEstado().getDescripcion())
                .canalReporte(inc.getCanalReporte())
                .canalReporteDesc(inc.getCanalReporte().getDescripcion())
                .sectorId(inc.getSector() != null ? inc.getSector().getId() : null)
                .sectorNombre(inc.getSector() != null ? inc.getSector().getNombre() : "General")
                .sectorCodigo(inc.getSector() != null ? inc.getSector().getCodigo() : null)
                .zonaId(inc.getZona() != null ? inc.getZona().getId() : null)
                .zonaNombre(inc.getZona() != null ? inc.getZona().getNombre() : null)
                .ubicacionReferencia(inc.getUbicacionReferencia())
                .latitud(inc.getLatitud())
                .longitud(inc.getLongitud())
                .nivelConfidencialidad(inc.getNivelConfidencialidad())
                .reportanteAnonimo(inc.getReportanteAnonimo())
                .reportanteNombre(Boolean.TRUE.equals(inc.getReportanteAnonimo()) ? "[ANÓNIMO]" : enmascararTexto(inc.getReportanteNombre()))
                .reportanteTelefono(enmascararTelefono(inc.getReportanteTelefono()))
                .fechaReporte(inc.getFechaReporte())
                .fechaOcurrencia(inc.getFechaOcurrencia())
                .totalDocumentos(docsCount)
                .totalComentarios(inc.getComentarios() != null ? inc.getComentarios().size() : 0)
                .version(inc.getVersion())
                .createdAt(inc.getCreatedAt())
                .build();
    }

    private IncidenciaHistorialResponse mapearHistorialResponse(IncidenciaHistorial h) {
        return IncidenciaHistorialResponse.builder()
                .id(h.getId())
                .tipoEvento(h.getTipoEvento())
                .tipoEventoDesc(h.getTipoEvento().getDescripcion())
                .estadoAnterior(h.getEstadoAnterior())
                .estadoNuevo(h.getEstadoNuevo())
                .prioridadAnterior(h.getPrioridadAnterior())
                .prioridadNueva(h.getPrioridadNueva())
                .motivo(h.getMotivo())
                .usuarioNombre(h.getUsuario() != null ? h.getUsuario().getUsername() : "Sistema")
                .detallesJson(h.getDetallesJson())
                .createdAt(h.getCreatedAt())
                .build();
    }

    private IncidenciaDocumentoResponse mapearDocumentoResponse(IncidenciaDocumento d) {
        long kb = d.getTamanoBytes() / 1024;
        String tamano = kb > 1024 ? String.format("%.1f MB", (kb / 1024.0)) : String.format("%d KB", kb);

        return IncidenciaDocumentoResponse.builder()
                .id(d.getId())
                .nombreArchivo(d.getNombreArchivo())
                .tipoMime(d.getTipoMime())
                .tamanoBytes(d.getTamanoBytes())
                .tamanoFormateado(tamano)
                .rutaArchivo(d.getRutaArchivo())
                .hashSha256(d.getHashSha256())
                .descripcion(d.getDescripcion())
                .esFotografia(d.getEsFotografia())
                .documentoId(d.getDocumento() != null ? d.getDocumento().getId() : null)
                .usuarioNombre(d.getUsuario() != null ? d.getUsuario().getUsername() : "Sistema")
                .createdAt(d.getCreatedAt())
                .build();
    }

    private IncidenciaComentarioResponse mapearComentarioResponse(IncidenciaComentario c) {
        return IncidenciaComentarioResponse.builder()
                .id(c.getId())
                .comentario(c.getComentario())
                .esInterno(c.getEsInterno())
                .usuarioNombre(c.getUsuario() != null ? c.getUsuario().getUsername() : "Sistema")
                .createdAt(c.getCreatedAt())
                .build();
    }

    private String enmascararTexto(String txt) {
        if (txt == null || txt.trim().isEmpty()) return "No registrado";
        if (txt.length() <= 3) return "***";
        return txt.charAt(0) + "***" + txt.charAt(txt.length() - 1);
    }

    private String enmascararTelefono(String telf) {
        if (telf == null || telf.trim().isEmpty()) return "-";
        if (telf.length() <= 4) return "****";
        return telf.substring(0, 2) + "****" + telf.substring(telf.length() - 2);
    }

    private String calcularSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    private void agregarCeldaClaveValor(PdfPTable table, String clave, String valor, Font fontBold, Font fontBody) {
        PdfPCell cClave = new PdfPCell(new Phrase(clave, fontBold));
        cClave.setBackgroundColor(new java.awt.Color(241, 245, 249));
        cClave.setPadding(4);
        table.addCell(cClave);

        PdfPCell cVal = new PdfPCell(new Phrase(valor != null ? valor : "-", fontBody));
        cVal.setPadding(4);
        table.addCell(cVal);
    }

    private void agregarCeldaHeader(PdfPTable table, String texto, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(texto, font));
        c.setBackgroundColor(new java.awt.Color(30, 41, 59));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setPadding(4);
        table.addCell(c);
    }

    private PdfPCell crearCelda(String texto, Font font, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "", font));
        c.setHorizontalAlignment(alineacion);
        c.setPadding(4);
        return c;
    }

    private String sanitizar(String val) {
        if (val == null) return "";
        if (val.startsWith("=") || val.startsWith("+") || val.startsWith("-") || val.startsWith("@")) {
            return "'" + val;
        }
        return val;
    }
}
