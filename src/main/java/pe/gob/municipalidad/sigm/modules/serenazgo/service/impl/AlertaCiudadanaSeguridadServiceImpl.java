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
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos;
import pe.gob.municipalidad.sigm.common.notification.service.EmailService;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.AlertaCiudadanaSeguridadService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.IncidenciaSeguridadCiudadanaService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertaCiudadanaSeguridadServiceImpl implements AlertaCiudadanaSeguridadService {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String PIN_CARACTERES = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AlertaCiudadanaSeguridadRepository alertaRepository;
    private final AlertaHistorialRepository historialRepository;
    private final AlertaDocumentoRepository documentoRepository;
    private final AlertaSolicitudInfoRepository solicitudInfoRepository;
    private final TipoIncidenciaSeguridadRepository tipoIncidenciaRepository;
    private final SectorPatrullajeRepository sectorRepository;
    private final ZonaPatrullajeRepository zonaRepository;
    private final UsuarioRepository usuarioRepository;
    private final OficinaRepository oficinaRepository;
    private final IncidenciaSeguridadCiudadanaRepository incidenciaRepository;
    private final IncidenciaSeguridadCiudadanaService incidenciaService;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaAdministrativaService auditoriaService;
    private final EmailService emailService;

    // =========================================================================
    // 1. PORTAL CIUDADANO
    // =========================================================================

    @Override
    @Transactional
    public AlertaConfirmacionResponse registrarAlertaCiudadana(AlertaRegistroCiudadanoRequest request,
                                                             List<MultipartFile> archivos,
                                                             String usernameAutenticado,
                                                             String ipCliente) {
        log.info("Registrando reporte ciudadano de seguridad desde IP: {}", ipCliente);

        // 1. Tipo de reporte
        TipoIncidenciaSeguridad tipo = tipoIncidenciaRepository.findById(request.getTipoReporteId())
                .orElseThrow(() -> new IllegalArgumentException("El tipo de reporte seleccionado no existe"));

        // 2. Correlativo oficial e inmutable ALT-YYYY-00000
        Long secuencia = alertaRepository.obtenerSiguienteValorSecuencia();
        String codigo = String.format("ALT-%d-%05d", OffsetDateTime.now(ZONA_PERU).getYear(), secuencia);

        // 3. Generar PIN de seguimiento seguro (6 caracteres) y hash
        String pinClaro = generarPinAleatorio(6);
        String pinHash = passwordEncoder.encode(pinClaro);

        // 4. Usuario reportante (si está autenticado)
        Usuario usuarioReportante = null;
        if (StringUtils.hasText(usernameAutenticado) && !"anonymousUser".equals(usernameAutenticado)) {
            usuarioReportante = usuarioRepository.findByUsername(usernameAutenticado).orElse(null);
        }

        // 5. Geografía municipal (Sector / Zona)
        SectorPatrullaje sector = null;
        if (request.getSectorId() != null) {
            sector = sectorRepository.findById(request.getSectorId()).orElse(null);
        }
        ZonaPatrullaje zona = null;
        if (request.getZonaId() != null) {
            zona = zonaRepository.findById(request.getZonaId()).orElse(null);
        }

        // 6. Construir y persistir entidad
        AlertaCiudadanaSeguridad alerta = AlertaCiudadanaSeguridad.builder()
                .codigo(codigo)
                .titulo(sanitizar(request.getTitulo()))
                .tipoReporte(tipo)
                .canalOrigen(CanalAlertaCiudadana.PORTAL_CIUDADANO)
                .descripcion(sanitizar(request.getDescripcion()))
                .fechaRecepcion(OffsetDateTime.now(ZONA_PERU))
                .fechaOcurrencia(request.getFechaHoraOcurrencia())
                .prioridad(tipo.getPrioridadSugerida() != null ? tipo.getPrioridadSugerida() : PrioridadIncidenciaSeguridad.MEDIA)
                .ubicacionReferencia(sanitizar(request.getUbicacionReferencia()))
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .sector(sector)
                .zona(zona)
                .estado(EstadoAlertaCiudadana.RECIBIDA)
                .reportanteUsuario(usuarioReportante)
                .reportanteAnonimo(Boolean.TRUE.equals(request.getReportanteAnonimo()))
                .reportanteNombre(Boolean.TRUE.equals(request.getReportanteAnonimo()) ? null : sanitizar(request.getReportanteNombre()))
                .reportanteEmail(Boolean.TRUE.equals(request.getReportanteAnonimo()) ? null : sanitizar(request.getReportanteEmail()))
                .reportanteTelefonoContacto(Boolean.TRUE.equals(request.getReportanteAnonimo()) ? null : sanitizar(request.getReportanteTelefonoContacto()))
                .claveConsultaHash(pinHash)
                .nivelConfidencialidad(Boolean.TRUE.equals(request.getReportanteAnonimo()) ? NivelConfidencialidad.RESTRINGIDO : NivelConfidencialidad.ORDINARIO)
                .createdBy(usuarioReportante != null ? usuarioReportante.getUsername() : "CIUDADANO_WEB")
                .build();

        alerta = alertaRepository.save(alerta);

        // 7. Registrar evento inicial en historial
        historialRepository.save(AlertaHistorial.builder()
                .alerta(alerta)
                .tipoEvento(TipoEventoHistorialAlerta.RECEPCION)
                .estadoAnterior(null)
                .estadoNuevo(EstadoAlertaCiudadana.RECIBIDA.name())
                .motivo("Recepción del reporte mediante el Portal Ciudadano. Registro automático con código " + codigo)
                .esVisibleCiudadano(true)
                .usuario(usuarioReportante)
                .build());

        // 8. Procesar archivos adjuntos autorizados
        procesarAdjuntos(alerta, archivos);

        // 9. Pista de auditoría central
        auditoriaService.registrar("REGISTRO_ALERTA_CIUDADANA", "serenazgo_alertas_ciudadanas", alerta.getId(),
                "EXITO", "Reporte recibido con código: " + codigo + (Boolean.TRUE.equals(alerta.getReportanteAnonimo()) ? " [ANÓNIMO]" : ""));

        // 10. Notificación por correo electrónico si el ciudadano proporcionó correo
        enviarCorreoRecepcionSiCorresponde(alerta, pinClaro);

        return AlertaConfirmacionResponse.builder()
                .id(alerta.getId())
                .codigo(alerta.getCodigo())
                .pinConsulta(pinClaro)
                .fechaRecepcion(alerta.getFechaRecepcion())
                .estado(alerta.getEstado())
                .estadoDesc(alerta.getEstado().getDescripcion())
                .mensaje("Su reporte ha sido recibido satisfactoriamente por la Central de Serenazgo. Conserve su código y PIN de seguimiento para consultas posteriores.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AlertaDetailResponse consultarSeguimiento(String codigo, String pinConsulta, String usernameAutenticado) {
        if (!StringUtils.hasText(codigo)) {
            throw new IllegalArgumentException("Debe ingresar el código de seguimiento de su reporte");
        }

        AlertaCiudadanaSeguridad alerta = alertaRepository.findByCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ningún reporte con el código proporcionado. Verifique e intente nuevamente."));

        // Comprobación de autorización:
        // Si el usuario está autenticado y es el dueño del reporte, se concede acceso.
        boolean esTitularAutenticado = false;
        if (StringUtils.hasText(usernameAutenticado) && !"anonymousUser".equals(usernameAutenticado) && alerta.getReportanteUsuario() != null) {
            esTitularAutenticado = usernameAutenticado.equals(alerta.getReportanteUsuario().getUsername());
        }

        // Si no es el titular autenticado, debe proveer un PIN válido
        if (!esTitularAutenticado) {
            if (!StringUtils.hasText(pinConsulta)) {
                throw new AccessDeniedException("Para consultar este reporte requiere ingresar su PIN de seguridad de 6 dígitos.");
            }
            if (alerta.getClaveConsultaHash() == null || !passwordEncoder.matches(pinConsulta.trim().toUpperCase(), alerta.getClaveConsultaHash())) {
                throw new AccessDeniedException("El PIN de seguridad proporcionado es incorrecto.");
            }
        }

        // Mapear respuesta con sanitización y filtrado estricto para vista ciudadana
        return mapearDetalleParaCiudadano(alerta);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertaResponse> listarMisReportes(String username, Pageable pageable) {
        if (!StringUtils.hasText(username)) {
            return Page.empty();
        }
        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        if (u == null) {
            return Page.empty();
        }

        return alertaRepository.findByReportanteUsuarioIdOrderByFechaRecepcionDesc(u.getId(), pageable)
                .map(this::mapearResumen);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertaDetailResponse obtenerMiReporte(UUID id, String username) {
        AlertaCiudadanaSeguridad alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reporte no encontrado con ID: " + id));

        if (alerta.getReportanteUsuario() == null || !alerta.getReportanteUsuario().getUsername().equals(username)) {
            throw new AccessDeniedException("No tiene permiso para acceder a este reporte ciudadano.");
        }

        return mapearDetalleParaCiudadano(alerta);
    }

    @Override
    @Transactional
    public void responderSolicitudInformacion(AlertaRespuestaCiudadanoRequest request, String usernameAutenticado) {
        if (!StringUtils.hasText(request.getCodigo()) || !StringUtils.hasText(request.getRespuesta())) {
            throw new IllegalArgumentException("Código y respuesta son obligatorios");
        }

        AlertaCiudadanaSeguridad alerta = alertaRepository.findByCodigo(request.getCodigo().trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado"));

        // Autorización
        boolean autorizado = false;
        if (StringUtils.hasText(usernameAutenticado) && !"anonymousUser".equals(usernameAutenticado) && alerta.getReportanteUsuario() != null) {
            autorizado = usernameAutenticado.equals(alerta.getReportanteUsuario().getUsername());
        }
        if (!autorizado) {
            if (!StringUtils.hasText(request.getPinConsulta()) || alerta.getClaveConsultaHash() == null ||
                    !passwordEncoder.matches(request.getPinConsulta().trim().toUpperCase(), alerta.getClaveConsultaHash())) {
                throw new AccessDeniedException("Credenciales de seguimiento inválidas para responder a la solicitud.");
            }
        }

        // Buscar solicitud pendiente
        AlertaSolicitudInfo solPendiente = null;
        if (request.getSolicitudId() != null) {
            solPendiente = solicitudInfoRepository.findById(request.getSolicitudId()).orElse(null);
        }
        if (solPendiente == null) {
            solPendiente = solicitudInfoRepository.findFirstByAlertaIdAndEstadoOrderByFechaSolicitudDesc(alerta.getId(), EstadoSolicitudInfoAlerta.PENDIENTE).orElse(null);
        }

        if (solPendiente != null) {
            solPendiente.setRespuestaCiudadano(sanitizar(request.getRespuesta()));
            solPendiente.setFechaRespuesta(OffsetDateTime.now(ZONA_PERU));
            solPendiente.setEstado(EstadoSolicitudInfoAlerta.RESPONDIDA);
            solicitudInfoRepository.save(solPendiente);
        }

        // Transición de estado de la alerta: retorna a EN_REVISION
        EstadoAlertaCiudadana ant = alerta.getEstado();
        alerta.setEstado(EstadoAlertaCiudadana.EN_REVISION);
        alerta.setUpdatedBy("CIUDADANO_RESPUESTA");
        alertaRepository.save(alerta);

        // Historial
        historialRepository.save(AlertaHistorial.builder()
                .alerta(alerta)
                .tipoEvento(TipoEventoHistorialAlerta.RESPUESTA_CIUDADANO)
                .estadoAnterior(ant.name())
                .estadoNuevo(EstadoAlertaCiudadana.EN_REVISION.name())
                .motivo("El ciudadano aportó información complementaria: " + sanitizar(request.getRespuesta()))
                .esVisibleCiudadano(true)
                .build());

        auditoriaService.registrar("RESPUESTA_CIUDADANO_ALERTA", "serenazgo_alertas_ciudadanas", alerta.getId(),
                "EXITO", "Respuesta complementaria recibida para alerta " + alerta.getCodigo());
    }

    // =========================================================================
    // 2. GESTIÓN ADMINISTRATIVA (OPERADOR / SUPERVISOR)
    // =========================================================================

    @Override
    @Transactional
    public AlertaConfirmacionResponse registrarAlertaAdmin(AlertaRegistroAdminRequest request, List<MultipartFile> archivos, String username) {
        log.info("Operador {} registrando reporte por canal {}", username, request.getCanalOrigen());

        TipoIncidenciaSeguridad tipo = tipoIncidenciaRepository.findById(request.getTipoReporteId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de reporte no encontrado"));

        Long secuencia = alertaRepository.obtenerSiguienteValorSecuencia();
        String codigo = String.format("ALT-%d-%05d", OffsetDateTime.now(ZONA_PERU).getYear(), secuencia);

        String pinClaro = generarPinAleatorio(6);
        String pinHash = passwordEncoder.encode(pinClaro);

        Usuario op = usuarioRepository.findByUsername(username).orElse(null);
        SectorPatrullaje sector = request.getSectorId() != null ? sectorRepository.findById(request.getSectorId()).orElse(null) : null;
        ZonaPatrullaje zona = request.getZonaId() != null ? zonaRepository.findById(request.getZonaId()).orElse(null) : null;

        AlertaCiudadanaSeguridad alerta = AlertaCiudadanaSeguridad.builder()
                .codigo(codigo)
                .titulo(sanitizar(request.getTitulo()))
                .tipoReporte(tipo)
                .canalOrigen(request.getCanalOrigen())
                .descripcion(sanitizar(request.getDescripcion()))
                .fechaRecepcion(OffsetDateTime.now(ZONA_PERU))
                .fechaOcurrencia(request.getFechaHoraOcurrencia())
                .prioridad(request.getPrioridad())
                .ubicacionReferencia(sanitizar(request.getUbicacionReferencia()))
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .sector(sector)
                .zona(zona)
                .estado(EstadoAlertaCiudadana.EN_REVISION)
                .reportanteAnonimo(Boolean.TRUE.equals(request.getReportanteAnonimo()))
                .reportanteNombre(Boolean.TRUE.equals(request.getReportanteAnonimo()) ? null : sanitizar(request.getReportanteNombre()))
                .reportanteEmail(Boolean.TRUE.equals(request.getReportanteAnonimo()) ? null : sanitizar(request.getReportanteEmail()))
                .reportanteTelefonoContacto(Boolean.TRUE.equals(request.getReportanteAnonimo()) ? null : sanitizar(request.getReportanteTelefonoContacto()))
                .claveConsultaHash(pinHash)
                .nivelConfidencialidad(request.getNivelConfidencialidad())
                .observacionesInternas(sanitizar(request.getObservacionesInternas()))
                .createdBy(username)
                .build();

        alerta = alertaRepository.save(alerta);

        historialRepository.save(AlertaHistorial.builder()
                .alerta(alerta)
                .tipoEvento(TipoEventoHistorialAlerta.RECEPCION)
                .estadoNuevo(EstadoAlertaCiudadana.EN_REVISION.name())
                .motivo("Registro administrativo por operador de central vía " + request.getCanalOrigen().getDescripcion())
                .esVisibleCiudadano(true)
                .usuario(op)
                .build());

        procesarAdjuntos(alerta, archivos);

        auditoriaService.registrar("REGISTRO_ADMIN_ALERTA", "serenazgo_alertas_ciudadanas", alerta.getId(),
                "EXITO", "Registro administrativo por operador: " + username);

        return AlertaConfirmacionResponse.builder()
                .id(alerta.getId())
                .codigo(alerta.getCodigo())
                .pinConsulta(pinClaro)
                .fechaRecepcion(alerta.getFechaRecepcion())
                .estado(alerta.getEstado())
                .estadoDesc(alerta.getEstado().getDescripcion())
                .mensaje("Alerta ciudadana registrada exitosamente con código " + codigo)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertaResponse> listarConFiltrosAdmin(AlertaFilter filtro, Pageable pageable) {
        log.debug("Listando alertas con filtros administrativos...");

        Specification<AlertaCiudadanaSeguridad> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro != null) {
                if (filtro.getEstado() != null) {
                    predicates.add(cb.equal(root.get("estado"), filtro.getEstado()));
                }
                if (filtro.getPrioridad() != null) {
                    predicates.add(cb.equal(root.get("prioridad"), filtro.getPrioridad()));
                }
                if (filtro.getTipoId() != null) {
                    predicates.add(cb.equal(root.get("tipoReporte").get("id"), filtro.getTipoId()));
                }
                if (filtro.getCanal() != null) {
                    predicates.add(cb.equal(root.get("canalOrigen"), filtro.getCanal()));
                }
                if (filtro.getSectorId() != null) {
                    predicates.add(cb.equal(root.get("sector").get("id"), filtro.getSectorId()));
                }
                if (filtro.getZonaId() != null) {
                    predicates.add(cb.equal(root.get("zona").get("id"), filtro.getZonaId()));
                }
                if (filtro.getDesde() != null) {
                    OffsetDateTime desde = filtro.getDesde().atStartOfDay(ZONA_PERU).toOffsetDateTime();
                    predicates.add(cb.greaterThanOrEqualTo(root.get("fechaRecepcion"), desde));
                }
                if (filtro.getHasta() != null) {
                    OffsetDateTime hasta = filtro.getHasta().atTime(LocalTime.MAX).atZone(ZONA_PERU).toOffsetDateTime();
                    predicates.add(cb.lessThanOrEqualTo(root.get("fechaRecepcion"), hasta));
                }
                if (Boolean.TRUE.equals(filtro.getVinculada())) {
                    predicates.add(cb.isNotNull(root.get("incidencia")));
                } else if (Boolean.FALSE.equals(filtro.getVinculada())) {
                    predicates.add(cb.isNull(root.get("incidencia")));
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

        return alertaRepository.findAll(spec, pageable).map(this::mapearResumen);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertaDetailResponse obtenerDetalleAdmin(UUID id, boolean puedeVerDatosSensibles) {
        AlertaCiudadanaSeguridad alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alerta no encontrada con ID: " + id));

        List<AlertaHistorialResponse> hist = historialRepository.findByAlertaIdOrderByCreatedAtAsc(id).stream()
                .map(this::mapearHistorial)
                .collect(Collectors.toList());

        List<AlertaDocumentoResponse> docs = documentoRepository.findByAlertaIdOrderByCreatedAtDesc(id).stream()
                .map(this::mapearDocumento)
                .collect(Collectors.toList());

        List<AlertaSolicitudInfoResponse> sols = solicitudInfoRepository.findByAlertaIdOrderByFechaSolicitudDesc(id).stream()
                .map(this::mapearSolicitud)
                .collect(Collectors.toList());

        String nom = alerta.getReportanteNombre();
        String mail = alerta.getReportanteEmail();
        String tel = alerta.getReportanteTelefonoContacto();

        if (!puedeVerDatosSensibles) {
            nom = "[RESERVADO / CONFIDENCIAL]";
            mail = "[OCULTO]";
            tel = "[OCULTO]";
        } else if (Boolean.TRUE.equals(alerta.getReportanteAnonimo())) {
            nom = "[CIUDADANO ANÓNIMO]";
            mail = "[NO PROVISTO]";
            tel = enmascarar(tel);
        }

        return AlertaDetailResponse.builder()
                .id(alerta.getId())
                .codigo(alerta.getCodigo())
                .titulo(alerta.getTitulo())
                .tipoId(alerta.getTipoReporte() != null ? alerta.getTipoReporte().getId() : null)
                .tipoNombre(alerta.getTipoReporte() != null ? alerta.getTipoReporte().getNombre() : "Sin clasificar")
                .tipoCodigo(alerta.getTipoReporte() != null ? alerta.getTipoReporte().getCodigo() : "")
                .canalOrigen(alerta.getCanalOrigen())
                .canalOrigenDesc(alerta.getCanalOrigen().getDescripcion())
                .descripcion(alerta.getDescripcion())
                .fechaRecepcion(alerta.getFechaRecepcion())
                .fechaOcurrencia(alerta.getFechaOcurrencia())
                .prioridad(alerta.getPrioridad())
                .prioridadDesc(alerta.getPrioridad().getDescripcion())
                .ubicacionReferencia(alerta.getUbicacionReferencia())
                .latitud(alerta.getLatitud())
                .longitud(alerta.getLongitud())
                .sectorId(alerta.getSector() != null ? alerta.getSector().getId() : null)
                .sectorNombre(alerta.getSector() != null ? alerta.getSector().getNombre() : "Sin Sector")
                .zonaId(alerta.getZona() != null ? alerta.getZona().getId() : null)
                .zonaNombre(alerta.getZona() != null ? alerta.getZona().getNombre() : "")
                .estado(alerta.getEstado())
                .estadoDesc(alerta.getEstado().getDescripcion())
                .reportanteUsuarioId(alerta.getReportanteUsuario() != null ? alerta.getReportanteUsuario().getId() : null)
                .reportanteAnonimo(alerta.getReportanteAnonimo())
                .reportanteNombre(nom)
                .reportanteEmail(mail)
                .reportanteTelefono(tel)
                .incidenciaId(alerta.getIncidencia() != null ? alerta.getIncidencia().getId() : null)
                .incidenciaCodigo(alerta.getIncidencia() != null ? alerta.getIncidencia().getCodigo() : null)
                .incidenciaTitulo(alerta.getIncidencia() != null ? alerta.getIncidencia().getTitulo() : null)
                .oficinaResponsableId(alerta.getOficinaResponsable() != null ? alerta.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(alerta.getOficinaResponsable() != null ? alerta.getOficinaResponsable().getNombre() : "Central de Serenazgo")
                .nivelConfidencialidad(alerta.getNivelConfidencialidad())
                .observacionesInternas(alerta.getObservacionesInternas())
                .motivoCierre(alerta.getMotivoCierre())
                .resultadoCiudadano(alerta.getResultadoCiudadano())
                .motivoDescarte(alerta.getMotivoDescarte())
                .version(alerta.getVersion())
                .createdAt(alerta.getCreatedAt())
                .updatedAt(alerta.getUpdatedAt())
                .historial(hist)
                .documentos(docs)
                .solicitudesInfo(sols)
                .build();
    }

    @Override
    @Transactional
    public AlertaResponse clasificarAlerta(UUID id, AlertaClasificarRequest request, String username) {
        log.info("Clasificando alerta ID {} a nuevo tipo por {}", id, username);

        AlertaCiudadanaSeguridad alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alerta no encontrada"));

        TipoIncidenciaSeguridad nuevoTipo = tipoIncidenciaRepository.findById(request.getNuevoTipoReporteId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de incidencia no válido"));

        alerta.setTipoReporte(nuevoTipo);
        if (alerta.getEstado() == EstadoAlertaCiudadana.RECIBIDA || alerta.getEstado() == EstadoAlertaCiudadana.PENDIENTE) {
            alerta.setEstado(EstadoAlertaCiudadana.CLASIFICADA);
        }
        alerta.setUpdatedBy(username);
        alerta = alertaRepository.save(alerta);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(AlertaHistorial.builder()
                .alerta(alerta)
                .tipoEvento(TipoEventoHistorialAlerta.CLASIFICACION)
                .estadoNuevo(alerta.getEstado().name())
                .motivo("Clasificación a '" + nuevoTipo.getNombre() + "'. Motivo: " + request.getMotivo().trim())
                .esVisibleCiudadano(true)
                .usuario(u)
                .build());

        auditoriaService.registrar("CLASIFICAR_ALERTA", "serenazgo_alertas_ciudadanas", alerta.getId(),
                "EXITO", "Tipo: " + nuevoTipo.getNombre() + " por " + username);

        return mapearResumen(alerta);
    }

    @Override
    @Transactional
    public AlertaResponse cambiarPrioridad(UUID id, AlertaPrioridadRequest request, String username) {
        AlertaCiudadanaSeguridad alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alerta no encontrada"));

        PrioridadIncidenciaSeguridad ant = alerta.getPrioridad();
        alerta.setPrioridad(request.getNuevaPrioridad());
        alerta.setUpdatedBy(username);
        alerta = alertaRepository.save(alerta);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(AlertaHistorial.builder()
                .alerta(alerta)
                .tipoEvento(TipoEventoHistorialAlerta.CAMBIO_PRIORIDAD)
                .estadoAnterior(ant.name())
                .estadoNuevo(request.getNuevaPrioridad().name())
                .motivo("Ajuste de prioridad a " + request.getNuevaPrioridad().getDescripcion() + ". Justificación: " + request.getJustificacion().trim())
                .esVisibleCiudadano(false) // Las prioridades operativas internas no se exponen al reportante
                .usuario(u)
                .build());

        auditoriaService.registrar("CAMBIO_PRIORIDAD_ALERTA", "serenazgo_alertas_ciudadanas", alerta.getId(),
                "EXITO", "Nueva prioridad: " + request.getNuevaPrioridad());

        return mapearResumen(alerta);
    }

    @Override
    @Transactional
    public AlertaResponse cambiarEstado(UUID id, AlertaCambioEstadoRequest request, String username) {
        AlertaCiudadanaSeguridad alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alerta no encontrada"));

        EstadoAlertaCiudadana ant = alerta.getEstado();
        alerta.setEstado(request.getNuevoEstado());
        alerta.setUpdatedBy(username);
        alerta = alertaRepository.save(alerta);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(AlertaHistorial.builder()
                .alerta(alerta)
                .tipoEvento(TipoEventoHistorialAlerta.CAMBIO_ESTADO)
                .estadoAnterior(ant.name())
                .estadoNuevo(request.getNuevoEstado().name())
                .motivo(request.getMotivo().trim())
                .esVisibleCiudadano(Boolean.TRUE.equals(request.getEsVisibleCiudadano()))
                .usuario(u)
                .build());

        auditoriaService.registrar("CAMBIO_ESTADO_ALERTA", "serenazgo_alertas_ciudadanas", alerta.getId(),
                "EXITO", "De " + ant + " a " + request.getNuevoEstado());

        return mapearResumen(alerta);
    }

    @Override
    @Transactional
    public AlertaSolicitudInfoResponse solicitarInformacion(UUID id, AlertaSolicitarInfoRequest request, String username) {
        AlertaCiudadanaSeguridad alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alerta no encontrada"));

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);

        AlertaSolicitudInfo solicitud = AlertaSolicitudInfo.builder()
                .alerta(alerta)
                .solicitud(sanitizar(request.getSolicitud()))
                .fechaSolicitud(OffsetDateTime.now(ZONA_PERU))
                .usuarioSolicita(u)
                .estado(EstadoSolicitudInfoAlerta.PENDIENTE)
                .build();

        solicitud = solicitudInfoRepository.save(solicitud);

        EstadoAlertaCiudadana ant = alerta.getEstado();
        alerta.setEstado(EstadoAlertaCiudadana.REQUIERE_INFORMACION);
        alerta.setUpdatedBy(username);
        alerta = alertaRepository.save(alerta);

        historialRepository.save(AlertaHistorial.builder()
                .alerta(alerta)
                .tipoEvento(TipoEventoHistorialAlerta.SOLICITUD_INFORMACION)
                .estadoAnterior(ant.name())
                .estadoNuevo(EstadoAlertaCiudadana.REQUIERE_INFORMACION.name())
                .motivo("Requerimiento al ciudadano: " + sanitizar(request.getSolicitud()))
                .esVisibleCiudadano(true)
                .usuario(u)
                .build());

        auditoriaService.registrar("SOLICITUD_INFO_ALERTA", "serenazgo_alertas_ciudadanas", alerta.getId(),
                "EXITO", "Solicitud de información complementaria generada");

        // Notificar por correo si hay email
        if (StringUtils.hasText(alerta.getReportanteEmail())) {
            try {
                emailService.encolarCorreo(NotificationDtos.EmailRequestDto.builder()
                        .destinatarioCorreo(alerta.getReportanteEmail())
                        .destinatarioNombre(alerta.getReportanteNombre() != null ? alerta.getReportanteNombre() : "Ciudadano")
                        .plantillaCodigo("NOTIFICACION_SISTEMA")
                        .asuntoPersonalizado("SIGM-QUINUA: Se requiere información complementaria para su reporte " + alerta.getCodigo())
                        .variables(Map.of(
                                "codigo", alerta.getCodigo(),
                                "mensaje", request.getSolicitud()
                        ))
                        .tipo("ALERTA")
                        .build());
            } catch (Exception e) {
                log.warn("No se pudo encolar correo de solicitud de info para {}: {}", alerta.getCodigo(), e.getMessage());
            }
        }

        return mapearSolicitud(solicitud);
    }

    @Override
    @Transactional
    public AlertaResponse vincularConIncidencia(UUID id, AlertaVincularIncidenciaRequest request, String username) {
        AlertaCiudadanaSeguridad alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alerta no encontrada"));

        IncidenciaSeguridadCiudadana incidencia;

        if (Boolean.TRUE.equals(request.getCrearNuevaIncidencia())) {
            // Crear formalmente la incidencia en el subsistema del Prompt 41
            IncidenciaCreateRequest incReq = IncidenciaCreateRequest.builder()
                    .titulo(alerta.getTitulo())
                    .tipoIncidenciaId(alerta.getTipoReporte() != null ? alerta.getTipoReporte().getId() : null)
                    .prioridad(alerta.getPrioridad())
                    .descripcion("[Reportado por Ciudadano — Alerta " + alerta.getCodigo() + "]\n" + alerta.getDescripcion())
                    .fechaHoraOcurrencia(alerta.getFechaOcurrencia() != null ? alerta.getFechaOcurrencia() : alerta.getFechaRecepcion())
                    .origenHecho(OrigenHechoIncidencia.HECHO_REPORTADO)
                    .canalReporte(CanalReporteIncidencia.ALERTA_CIUDADANA)
                    .ubicacionReferencia(alerta.getUbicacionReferencia())
                    .latitud(alerta.getLatitud())
                    .longitud(alerta.getLongitud())
                    .sectorId(alerta.getSector() != null ? alerta.getSector().getId() : null)
                    .zonaId(alerta.getZona() != null ? alerta.getZona().getId() : null)
                    .reportanteAnonimo(alerta.getReportanteAnonimo())
                    .reportanteNombre(alerta.getReportanteNombre())
                    .reportanteTelefono(alerta.getReportanteTelefonoContacto())
                    .nivelConfidencialidad(alerta.getNivelConfidencialidad())
                    .build();

            IncidenciaResponse incRes = incidenciaService.registrarIncidencia(incReq, username);
            incidencia = incidenciaRepository.findById(incRes.getId())
                    .orElseThrow(() -> new IllegalStateException("Fallo al recuperar la incidencia creada"));
        } else {
            if (request.getIncidenciaId() == null) {
                throw new IllegalArgumentException("Debe seleccionar una incidencia existente o marcar crear nueva");
            }
            incidencia = incidenciaRepository.findById(request.getIncidenciaId())
                    .orElseThrow(() -> new IllegalArgumentException("La incidencia seleccionada no existe"));
        }

        alerta.setIncidencia(incidencia);
        EstadoAlertaCiudadana ant = alerta.getEstado();
        alerta.setEstado(EstadoAlertaCiudadana.VINCULADA);
        alerta.setUpdatedBy(username);
        alerta = alertaRepository.save(alerta);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(AlertaHistorial.builder()
                .alerta(alerta)
                .tipoEvento(TipoEventoHistorialAlerta.VINCULACION_INCIDENCIA)
                .estadoAnterior(ant.name())
                .estadoNuevo(EstadoAlertaCiudadana.VINCULADA.name())
                .motivo("Vinculada formalmente con la Incidencia " + incidencia.getCodigo() + ". " + request.getMotivo().trim())
                .esVisibleCiudadano(true)
                .usuario(u)
                .build());

        auditoriaService.registrar("VINCULAR_ALERTA_INCIDENCIA", "serenazgo_alertas_ciudadanas", alerta.getId(),
                "EXITO", "Alerta " + alerta.getCodigo() + " vinculada a Incidencia " + incidencia.getCodigo());

        return mapearResumen(alerta);
    }

    @Override
    @Transactional
    public AlertaResponse cerrarAlerta(UUID id, AlertaCierreRequest request, String username) {
        AlertaCiudadanaSeguridad alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alerta no encontrada"));

        EstadoAlertaCiudadana ant = alerta.getEstado();
        alerta.setEstado(EstadoAlertaCiudadana.CERRADA);
        alerta.setMotivoCierre(sanitizar(request.getMotivo()));
        alerta.setResultadoCiudadano(sanitizar(request.getResultadoCiudadano()));
        alerta.setUpdatedBy(username);
        alerta = alertaRepository.save(alerta);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(AlertaHistorial.builder()
                .alerta(alerta)
                .tipoEvento(TipoEventoHistorialAlerta.CIERRE_ADMINISTRATIVO)
                .estadoAnterior(ant.name())
                .estadoNuevo(EstadoAlertaCiudadana.CERRADA.name())
                .motivo("Cierre administrativo: " + sanitizar(request.getMotivo()) +
                        (StringUtils.hasText(request.getResultadoCiudadano()) ? " | Resultado informado: " + sanitizar(request.getResultadoCiudadano()) : ""))
                .esVisibleCiudadano(true)
                .usuario(u)
                .build());

        auditoriaService.registrar("CIERRE_ADMIN_ALERTA", "serenazgo_alertas_ciudadanas", alerta.getId(),
                "EXITO", "Cierre administrativo registrado para alerta " + alerta.getCodigo());

        return mapearResumen(alerta);
    }

    @Override
    @Transactional
    public AlertaResponse descartarAlerta(UUID id, AlertaDescarteRequest request, String username) {
        AlertaCiudadanaSeguridad alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alerta no encontrada"));

        EstadoAlertaCiudadana ant = alerta.getEstado();
        alerta.setEstado(EstadoAlertaCiudadana.DESCARTADA);
        alerta.setMotivoDescarte(sanitizar(request.getMotivoDescarte()));
        alerta.setUpdatedBy(username);
        alerta = alertaRepository.save(alerta);

        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        historialRepository.save(AlertaHistorial.builder()
                .alerta(alerta)
                .tipoEvento(TipoEventoHistorialAlerta.DESCARTE)
                .estadoAnterior(ant.name())
                .estadoNuevo(EstadoAlertaCiudadana.DESCARTADA.name())
                .motivo("Reporte descartado por: " + sanitizar(request.getMotivoDescarte()))
                .esVisibleCiudadano(true)
                .usuario(u)
                .build());

        auditoriaService.registrar("DESCARTE_ALERTA", "serenazgo_alertas_ciudadanas", alerta.getId(),
                "EXITO", "Alerta " + alerta.getCodigo() + " descartada");

        return mapearResumen(alerta);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardAlertasResponse obtenerDashboardAlertas() {
        OffsetDateTime ahora = OffsetDateTime.now(ZONA_PERU);
        OffsetDateTime inicioHoy = ahora.toLocalDate().atStartOfDay(ZONA_PERU).toOffsetDateTime();
        OffsetDateTime inicioSemana = ahora.minusDays(7);
        OffsetDateTime inicioMes = ahora.withDayOfMonth(1).toLocalDate().atStartOfDay(ZONA_PERU).toOffsetDateTime();

        long tot = alertaRepository.count();
        long rec = alertaRepository.countByEstado(EstadoAlertaCiudadana.RECIBIDA);
        long pen = alertaRepository.countByEstado(EstadoAlertaCiudadana.PENDIENTE);
        long rev = alertaRepository.countByEstado(EstadoAlertaCiudadana.EN_REVISION);
        long reqInf = alertaRepository.countByEstado(EstadoAlertaCiudadana.REQUIERE_INFORMACION);
        long cla = alertaRepository.countByEstado(EstadoAlertaCiudadana.CLASIFICADA);
        long vin = alertaRepository.countByEstado(EstadoAlertaCiudadana.VINCULADA) + alertaRepository.countByEstado(EstadoAlertaCiudadana.CONVERTIDA_EN_INCIDENCIA);
        long cer = alertaRepository.countByEstado(EstadoAlertaCiudadana.CERRADA);
        long des = alertaRepository.countByEstado(EstadoAlertaCiudadana.DESCARTADA);

        long pBaja = alertaRepository.countByPrioridad(PrioridadIncidenciaSeguridad.BAJA);
        long pMed = alertaRepository.countByPrioridad(PrioridadIncidenciaSeguridad.MEDIA);
        long pAlta = alertaRepository.countByPrioridad(PrioridadIncidenciaSeguridad.ALTA);
        long pCrit = alertaRepository.countByPrioridad(PrioridadIncidenciaSeguridad.CRITICA);

        long hoy = alertaRepository.countByFechaRecepcionBetween(inicioHoy, ahora);
        long sem = alertaRepository.countByFechaRecepcionBetween(inicioSemana, ahora);
        long mes = alertaRepository.countByFechaRecepcionBetween(inicioMes, ahora);

        return DashboardAlertasResponse.builder()
                .totalAlertas(tot)
                .recibidas(rec)
                .pendientes(pen)
                .enRevision(rev)
                .requierenInformacion(reqInf)
                .clasificadas(cla)
                .vinculadas(vin)
                .cerradas(cer)
                .descartadas(des)
                .prioridadBaja(pBaja)
                .prioridadMedia(pMed)
                .prioridadAlta(pAlta)
                .prioridadCritica(pCrit)
                .hoy(hoy)
                .estaSemana(sem)
                .esteMes(mes)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarAlertasExcel(LocalDate desde, LocalDate hasta) {
        OffsetDateTime fDesde = desde != null ? desde.atStartOfDay(ZONA_PERU).toOffsetDateTime() : null;
        OffsetDateTime fHasta = hasta != null ? hasta.atTime(LocalTime.MAX).atZone(ZONA_PERU).toOffsetDateTime() : null;

        Specification<AlertaCiudadanaSeguridad> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (fDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaRecepcion"), fDesde));
            }
            if (fHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaRecepcion"), fHasta));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<AlertaCiudadanaSeguridad> lista = alertaRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "fechaRecepcion"));

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Alertas Ciudadanas");

            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] columnas = {
                    "Código", "Fecha Recepción", "Título", "Tipo Reporte", "Canal",
                    "Prioridad", "Estado", "Sector", "Zona", "Ubicación",
                    "Condición Reportante", "Incidencia Vinculada"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (AlertaCiudadanaSeguridad a : lista) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(a.getCodigo());
                row.createCell(1).setCellValue(a.getFechaRecepcion().format(FORMATO_FECHA_HORA));
                row.createCell(2).setCellValue(a.getTitulo());
                row.createCell(3).setCellValue(a.getTipoReporte() != null ? a.getTipoReporte().getNombre() : "General");
                row.createCell(4).setCellValue(a.getCanalOrigen().getDescripcion());
                row.createCell(5).setCellValue(a.getPrioridad().name());
                row.createCell(6).setCellValue(a.getEstado().getDescripcion());
                row.createCell(7).setCellValue(a.getSector() != null ? a.getSector().getNombre() : "Sin Sector");
                row.createCell(8).setCellValue(a.getZona() != null ? a.getZona().getNombre() : "");
                row.createCell(9).setCellValue(a.getUbicacionReferencia());
                row.createCell(10).setCellValue(Boolean.TRUE.equals(a.getReportanteAnonimo()) ? "ANÓNIMO" : "IDENTIFICADO");
                row.createCell(11).setCellValue(a.getIncidencia() != null ? a.getIncidencia().getCodigo() : "No vinculada");
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error al exportar alertas en Excel", e);
            throw new RuntimeException("Error al generar planilla Excel", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarFichaPdf(UUID id) {
        AlertaDetailResponse alerta = obtenerDetalleAdmin(id, false);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.NORMAL, new java.awt.Color(16, 24, 39));
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, new java.awt.Color(71, 85, 105));
            Font fontSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL, new java.awt.Color(30, 41, 59));
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.NORMAL, java.awt.Color.WHITE);
            Font fontBody = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, java.awt.Color.BLACK);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.NORMAL, java.awt.Color.BLACK);

            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(pHeader);

            Paragraph pSub = new Paragraph("Gerencia de Seguridad Ciudadana — Serenazgo Municipal", fontSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            document.add(pSub);

            Paragraph pDoc = new Paragraph("HOJA DE REPORTE Y ALERTA CIUDADANA Nº " + alerta.getCodigo(), fontTitulo);
            pDoc.setAlignment(Element.ALIGN_CENTER);
            pDoc.setSpacingBefore(8);
            pDoc.setSpacingAfter(12);
            document.add(pDoc);

            // Tabla general
            PdfPTable tGral = new PdfPTable(4);
            tGral.setWidthPercentage(100);
            tGral.setWidths(new float[]{20, 30, 20, 30});

            agregarCelda(tGral, "Código:", alerta.getCodigo(), fontBold, fontBody);
            agregarCelda(tGral, "Estado:", alerta.getEstadoDesc(), fontBold, fontBody);
            agregarCelda(tGral, "Canal:", alerta.getCanalOrigenDesc(), fontBold, fontBody);
            agregarCelda(tGral, "Prioridad:", alerta.getPrioridadDesc(), fontBold, fontBody);
            agregarCelda(tGral, "F. Recepción:", alerta.getFechaRecepcion().format(FORMATO_FECHA_HORA), fontBold, fontBody);
            agregarCelda(tGral, "F. Ocurrencia:", alerta.getFechaOcurrencia() != null ? alerta.getFechaOcurrencia().format(FORMATO_FECHA_HORA) : "No especificada", fontBold, fontBody);
            agregarCelda(tGral, "Sector:", alerta.getSectorNombre(), fontBold, fontBody);
            agregarCelda(tGral, "Zona:", alerta.getZonaNombre(), fontBold, fontBody);
            document.add(tGral);

            // Descripción
            Paragraph pDescTit = new Paragraph("DESCRIPCIÓN DE LOS HECHOS COMUNICADOS POR EL CIUDADANO", fontSeccion);
            pDescTit.setSpacingBefore(10);
            pDescTit.setSpacingAfter(4);
            document.add(pDescTit);

            PdfPTable tDesc = new PdfPTable(1);
            tDesc.setWidthPercentage(100);
            PdfPCell cDesc = new PdfPCell(new Phrase(alerta.getDescripcion(), fontBody));
            cDesc.setPadding(6);
            cDesc.setBackgroundColor(new java.awt.Color(248, 250, 252));
            tDesc.addCell(cDesc);
            document.add(tDesc);

            // Localización
            Paragraph pUbicTit = new Paragraph("UBICACIÓN Y REFERENCIAS GEOGRÁFICAS", fontSeccion);
            pUbicTit.setSpacingBefore(10);
            pUbicTit.setSpacingAfter(4);
            document.add(pUbicTit);

            PdfPTable tUbic = new PdfPTable(2);
            tUbic.setWidthPercentage(100);
            tUbic.setWidths(new float[]{30, 70});
            agregarCelda(tUbic, "Referencia Textual:", alerta.getUbicacionReferencia(), fontBold, fontBody);
            agregarCelda(tUbic, "Coordenadas GPS:", (alerta.getLatitud() != null ? alerta.getLatitud() + ", " + alerta.getLongitud() : "No registradas"), fontBold, fontBody);
            document.add(tUbic);

            // Historial
            Paragraph pHistTit = new Paragraph("TRAZABILIDAD Y ACTUACIONES ADMINISTRATIVAS", fontSeccion);
            pHistTit.setSpacingBefore(10);
            pHistTit.setSpacingAfter(4);
            document.add(pHistTit);

            PdfPTable tHist = new PdfPTable(4);
            tHist.setWidthPercentage(100);
            tHist.setWidths(new float[]{22, 22, 42, 14});
            agregarHeader(tHist, "Fecha", fontHeader);
            agregarHeader(tHist, "Evento", fontHeader);
            agregarHeader(tHist, "Detalle / Actuación", fontHeader);
            agregarHeader(tHist, "Usuario", fontHeader);

            for (AlertaHistorialResponse h : alerta.getHistorial()) {
                tHist.addCell(crearCeldaSimple(h.getCreatedAt().format(FORMATO_FECHA_HORA), fontBody));
                tHist.addCell(crearCeldaSimple(h.getTipoEventoDesc(), fontBody));
                tHist.addCell(crearCeldaSimple(h.getMotivo(), fontBody));
                tHist.addCell(crearCeldaSimple(h.getUsuarioNombre() != null ? h.getUsuarioNombre() : "CIUDADANO", fontBody));
            }
            document.add(tHist);

            // Cláusula legal
            Paragraph pLegal = new Paragraph(
                    "DESLINDE LEGAL: Las alertas ciudadanas son comunicaciones vecinales pendientes de evaluación municipal conforme a la Ley N° 27933 y " +
                            "Ley N° 31297. Este documento tiene fines de control y archivo administrativo. No constituye confirmación judicial ni policial de delitos ni sustituye atestados de la PNP.",
                    FontFactory.getFont(FontFactory.HELVETICA, 7, Font.ITALIC, new java.awt.Color(100, 116, 139))
            );
            pLegal.setSpacingBefore(14);
            document.add(pLegal);

            // Firmas
            PdfPTable tFirma = new PdfPTable(2);
            tFirma.setWidthPercentage(100);
            tFirma.setSpacingBefore(30);

            PdfPCell cF1 = new PdfPCell(new Phrase("\n\n_____________________________________\nOPERADOR DE CENTRAL\nSerenazgo Municipal de Quinua", fontBody));
            cF1.setBorder(Rectangle.NO_BORDER);
            cF1.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell cF2 = new PdfPCell(new Phrase("\n\n_____________________________________\nSUPERVISOR DE SEGURIDAD CIUDADANA\nGerencia de Servicios Municipales", fontBody));
            cF2.setBorder(Rectangle.NO_BORDER);
            cF2.setHorizontalAlignment(Element.ALIGN_CENTER);

            tFirma.addCell(cF1);
            tFirma.addCell(cF2);
            document.add(tFirma);

            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Error al generar PDF de alerta {}", id, e);
            throw new RuntimeException("Error en la compilación del reporte PDF", e);
        }
    }

    // =========================================================================
    // 3. MÉTODOS AUXILIARES Y PRIVADOS
    // =========================================================================

    private void procesarAdjuntos(AlertaCiudadanaSeguridad alerta, List<MultipartFile> archivos) {
        if (archivos == null || archivos.isEmpty()) return;

        for (MultipartFile file : archivos) {
            if (file == null || file.isEmpty()) continue;

            String originalName = file.getOriginalFilename();
            String nombreLimpio = sanitizar(originalName != null ? originalName : "archivo_" + UUID.randomUUID());
            String mime = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
            long size = file.getSize();

            if (size > 10 * 1024 * 1024) {
                log.warn("Archivo {} descartado por superar los 10MB", nombreLimpio);
                continue;
            }

            try {
                byte[] bytes = file.getBytes();
                String hash = calcularSha256(bytes);
                boolean esFoto = mime.startsWith("image/");
                String base64 = esFoto && size < 2 * 1024 * 1024 ? Base64.getEncoder().encodeToString(bytes) : null;

                AlertaDocumento doc = AlertaDocumento.builder()
                        .alerta(alerta)
                        .nombreArchivo(nombreLimpio)
                        .tipoMime(mime)
                        .tamanoBytes(size)
                        .hashSha256(hash)
                        .esFotografia(esFoto)
                        .contenidoBase64(base64)
                        .build();

                documentoRepository.save(doc);

                historialRepository.save(AlertaHistorial.builder()
                        .alerta(alerta)
                        .tipoEvento(TipoEventoHistorialAlerta.DOCUMENTO_ADJUNTO)
                        .estadoNuevo(alerta.getEstado().name())
                        .motivo("Archivo adjuntado: " + nombreLimpio + " (" + (size / 1024) + " KB)")
                        .esVisibleCiudadano(true)
                        .build());
            } catch (IOException e) {
                log.error("Fallo al procesar archivo adjunto para alerta {}", alerta.getCodigo(), e);
            }
        }
    }

    private void enviarCorreoRecepcionSiCorresponde(AlertaCiudadanaSeguridad alerta, String pinClaro) {
        if (!StringUtils.hasText(alerta.getReportanteEmail())) return;

        try {
            emailService.encolarCorreo(NotificationDtos.EmailRequestDto.builder()
                    .destinatarioCorreo(alerta.getReportanteEmail())
                    .destinatarioNombre(alerta.getReportanteNombre() != null ? alerta.getReportanteNombre() : "Ciudadano")
                    .plantillaCodigo("NOTIFICACION_SISTEMA")
                    .asuntoPersonalizado("SIGM-QUINUA: Constancia de recepción de reporte de seguridad " + alerta.getCodigo())
                    .variables(Map.of(
                            "codigo", alerta.getCodigo(),
                            "pin", pinClaro,
                            "mensaje", "Hemos recibido su comunicación para evaluación administrativa por parte de la Central de Serenazgo de Quinua."
                    ))
                    .tipo("INFORMACION")
                    .build());
        } catch (Exception e) {
            log.warn("No se pudo encolar correo de confirmación para {}: {}", alerta.getCodigo(), e.getMessage());
        }
    }

    private AlertaDetailResponse mapearDetalleParaCiudadano(AlertaCiudadanaSeguridad alerta) {
        // Filtrar historial a solo eventos visibles al ciudadano
        List<AlertaHistorialResponse> hist = historialRepository.findByAlertaIdAndEsVisibleCiudadanoTrueOrderByCreatedAtAsc(alerta.getId()).stream()
                .map(this::mapearHistorial)
                .collect(Collectors.toList());

        List<AlertaDocumentoResponse> docs = documentoRepository.findByAlertaIdOrderByCreatedAtDesc(alerta.getId()).stream()
                .map(this::mapearDocumento)
                .collect(Collectors.toList());

        List<AlertaSolicitudInfoResponse> sols = solicitudInfoRepository.findByAlertaIdOrderByFechaSolicitudDesc(alerta.getId()).stream()
                .map(this::mapearSolicitud)
                .collect(Collectors.toList());

        return AlertaDetailResponse.builder()
                .id(alerta.getId())
                .codigo(alerta.getCodigo())
                .titulo(alerta.getTitulo())
                .tipoId(alerta.getTipoReporte() != null ? alerta.getTipoReporte().getId() : null)
                .tipoNombre(alerta.getTipoReporte() != null ? alerta.getTipoReporte().getNombre() : "General")
                .descripcion(alerta.getDescripcion())
                .fechaRecepcion(alerta.getFechaRecepcion())
                .fechaOcurrencia(alerta.getFechaOcurrencia())
                .ubicacionReferencia(alerta.getUbicacionReferencia())
                .latitud(alerta.getLatitud())
                .longitud(alerta.getLongitud())
                .sectorNombre(alerta.getSector() != null ? alerta.getSector().getNombre() : "Área Distrital Quinua")
                .estado(alerta.getEstado())
                .estadoDesc(alerta.getEstado().getDescripcion())
                .resultadoCiudadano(alerta.getResultadoCiudadano())
                .historial(hist)
                .documentos(docs)
                .solicitudesInfo(sols)
                .build();
    }

    private AlertaResponse mapearResumen(AlertaCiudadanaSeguridad a) {
        boolean solPendiente = solicitudInfoRepository.findFirstByAlertaIdAndEstadoOrderByFechaSolicitudDesc(a.getId(), EstadoSolicitudInfoAlerta.PENDIENTE).isPresent();

        String nom = a.getReportanteNombre();
        String tel = a.getReportanteTelefonoContacto();
        if (Boolean.TRUE.equals(a.getReportanteAnonimo())) {
            nom = "[ANÓNIMO]";
            tel = "[OCULTO]";
        }

        return AlertaResponse.builder()
                .id(a.getId())
                .codigo(a.getCodigo())
                .titulo(a.getTitulo())
                .tipoId(a.getTipoReporte() != null ? a.getTipoReporte().getId() : null)
                .tipoNombre(a.getTipoReporte() != null ? a.getTipoReporte().getNombre() : "General")
                .canalOrigen(a.getCanalOrigen())
                .canalOrigenDesc(a.getCanalOrigen().getDescripcion())
                .fechaRecepcion(a.getFechaRecepcion())
                .fechaOcurrencia(a.getFechaOcurrencia())
                .prioridad(a.getPrioridad())
                .prioridadDesc(a.getPrioridad().getDescripcion())
                .ubicacionReferencia(a.getUbicacionReferencia())
                .latitud(a.getLatitud())
                .longitud(a.getLongitud())
                .sectorId(a.getSector() != null ? a.getSector().getId() : null)
                .sectorNombre(a.getSector() != null ? a.getSector().getNombre() : "Sin Sector")
                .zonaId(a.getZona() != null ? a.getZona().getId() : null)
                .zonaNombre(a.getZona() != null ? a.getZona().getNombre() : "")
                .estado(a.getEstado())
                .estadoDesc(a.getEstado().getDescripcion())
                .reportanteAnonimo(a.getReportanteAnonimo())
                .reportanteNombre(nom)
                .reportanteTelefono(tel)
                .nivelConfidencialidad(a.getNivelConfidencialidad())
                .incidenciaId(a.getIncidencia() != null ? a.getIncidencia().getId() : null)
                .incidenciaCodigo(a.getIncidencia() != null ? a.getIncidencia().getCodigo() : null)
                .tieneSolicitudPendiente(solPendiente)
                .build();
    }

    private AlertaHistorialResponse mapearHistorial(AlertaHistorial h) {
        return AlertaHistorialResponse.builder()
                .id(h.getId())
                .tipoEvento(h.getTipoEvento())
                .tipoEventoDesc(h.getTipoEvento().getDescripcion())
                .estadoAnterior(h.getEstadoAnterior())
                .estadoNuevo(h.getEstadoNuevo())
                .motivo(h.getMotivo())
                .esVisibleCiudadano(h.getEsVisibleCiudadano())
                .usuarioNombre(h.getUsuario() != null && h.getUsuario().getPersona() != null
                        ? h.getUsuario().getPersona().getNombres() + " " + h.getUsuario().getPersona().getApellidoPaterno()
                        : (h.getUsuario() != null ? h.getUsuario().getUsername() : "SISTEMA"))
                .createdAt(h.getCreatedAt())
                .build();
    }

    private AlertaDocumentoResponse mapearDocumento(AlertaDocumento d) {
        return AlertaDocumentoResponse.builder()
                .id(d.getId())
                .nombreArchivo(d.getNombreArchivo())
                .tipoMime(d.getTipoMime())
                .tamanoBytes(d.getTamanoBytes())
                .hashSha256(d.getHashSha256())
                .esFotografia(d.getEsFotografia())
                .contenidoBase64(d.getContenidoBase64())
                .createdAt(d.getCreatedAt())
                .build();
    }

    private AlertaSolicitudInfoResponse mapearSolicitud(AlertaSolicitudInfo s) {
        return AlertaSolicitudInfoResponse.builder()
                .id(s.getId())
                .solicitud(s.getSolicitud())
                .fechaSolicitud(s.getFechaSolicitud())
                .usuarioSolicitaNombre(s.getUsuarioSolicita() != null && s.getUsuarioSolicita().getPersona() != null
                        ? s.getUsuarioSolicita().getPersona().getNombres() + " " + s.getUsuarioSolicita().getPersona().getApellidoPaterno()
                        : (s.getUsuarioSolicita() != null ? s.getUsuarioSolicita().getUsername() : "Operador"))
                .respuestaCiudadano(s.getRespuestaCiudadano())
                .fechaRespuesta(s.getFechaRespuesta())
                .estado(s.getEstado())
                .estadoDesc(s.getEstado().getDescripcion())
                .build();
    }

    private String generarPinAleatorio(int longitud) {
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(PIN_CARACTERES.charAt(RANDOM.nextInt(PIN_CARACTERES.length())));
        }
        return sb.toString();
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
            return "HASH_NO_DISPONIBLE";
        }
    }

    private String sanitizar(String s) {
        if (s == null) return null;
        return s.replace("<", "&lt;").replace(">", "&gt;").trim();
    }

    private String enmascarar(String tel) {
        if (!StringUtils.hasText(tel)) return "[NO PROVISTO]";
        String limpio = tel.trim();
        if (limpio.length() <= 4) return "****";
        return limpio.substring(0, 2) + "****" + limpio.substring(limpio.length() - 2);
    }

    private void agregarCelda(PdfPTable table, String clave, String valor, Font fontBold, Font fontBody) {
        PdfPCell cClave = new PdfPCell(new Phrase(clave, fontBold));
        cClave.setBackgroundColor(new java.awt.Color(241, 245, 249));
        cClave.setPadding(4);
        table.addCell(cClave);

        PdfPCell cVal = new PdfPCell(new Phrase(valor != null ? valor : "-", fontBody));
        cVal.setPadding(4);
        table.addCell(cVal);
    }

    private void agregarHeader(PdfPTable table, String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(new java.awt.Color(30, 41, 59));
        c.setPadding(4);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c);
    }

    private PdfPCell crearCeldaSimple(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "-", font));
        c.setPadding(4);
        return c;
    }
}
