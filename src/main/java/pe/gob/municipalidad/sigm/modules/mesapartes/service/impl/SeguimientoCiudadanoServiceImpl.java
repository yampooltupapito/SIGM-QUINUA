package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.document.service.DocumentoService;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoEmisionDocumento;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoObservacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.VisibilidadObservacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.CodigoSeguimientoService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.SeguimientoCiudadanoService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class SeguimientoCiudadanoServiceImpl implements SeguimientoCiudadanoService {

    private final ExpedienteRepository expedienteRepository;
    private final ExpedienteHistorialRepository historialRepository;
    private final ObservacionExpedienteRepository observacionRepository;
    private final SubsanacionExpedienteRepository subsanacionRepository;
    private final DecisionExpedienteRepository decisionRepository;
    private final ExpedienteDocumentoRepository expedienteDocumentoRepository;
    private final DocumentoRepository documentoRepository;
    private final DocumentoService documentoService;
    private final UsuarioRepository usuarioRepository;
    private final CodigoSeguimientoService codigoSeguimientoService;

    @Value("${sigm.app.base-url:http://localhost:8080}")
    private String baseUrl;

    public SeguimientoCiudadanoServiceImpl(ExpedienteRepository expedienteRepository,
                                           ExpedienteHistorialRepository historialRepository,
                                           ObservacionExpedienteRepository observacionRepository,
                                           SubsanacionExpedienteRepository subsanacionRepository,
                                           DecisionExpedienteRepository decisionRepository,
                                           ExpedienteDocumentoRepository expedienteDocumentoRepository,
                                           DocumentoRepository documentoRepository,
                                           DocumentoService documentoService,
                                           UsuarioRepository usuarioRepository,
                                           CodigoSeguimientoService codigoSeguimientoService) {
        this.expedienteRepository = expedienteRepository;
        this.historialRepository = historialRepository;
        this.observacionRepository = observacionRepository;
        this.subsanacionRepository = subsanacionRepository;
        this.decisionRepository = decisionRepository;
        this.expedienteDocumentoRepository = expedienteDocumentoRepository;
        this.documentoRepository = documentoRepository;
        this.documentoService = documentoService;
        this.usuarioRepository = usuarioRepository;
        this.codigoSeguimientoService = codigoSeguimientoService;
    }

    @Override
    @Transactional
    public SeguimientoCiudadanoDetalleDto obtenerSeguimientoDetalle(Long expedienteId, String username) {
        Expediente exp = expedienteRepository.findById(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        validarAccesoPropietario(exp, username);

        CodigoSeguimiento cs = codigoSeguimientoService.generarOObtenerCodigo(exp);

        int etapaActual = ConsultaPublicaExpedienteServiceImpl.calcularNumeroEtapa(exp.getEstadoActual());
        List<EtapaProgresoDto> etapas = ConsultaPublicaExpedienteServiceImpl.generarEtapasProgreso(exp);
        String descCiudadana = ConsultaPublicaExpedienteServiceImpl.traducirEstadoCiudadano(exp.getEstadoActual());

        // Días restantes si existe plazo legal
        Integer diasRestantes = null;
        if (exp.getFechaLimite() != null) {
            long dias = Duration.between(OffsetDateTime.now(), exp.getFechaLimite()).toDays();
            diasRestantes = (int) dias;
        }

        List<EventoLineaTiempoCiudadanaDto> lineaTiempo = obtenerLineaTiempoCiudadanaInterna(exp);
        List<ObservacionCiudadanaDto> observaciones = obtenerObservacionesCiudadanasInterna(exp);
        List<SubsanacionCiudadanaDto> subsanaciones = obtenerSubsanacionesCiudadanasInterna(exp);
        List<DocumentoDisponibleDto> documentos = obtenerDocumentosDisponiblesInterna(exp);

        String solNombre = exp.getSolicitante() != null ? exp.getSolicitante().getNombreCompleto() : "No registrado";
        String solDoc = exp.getSolicitante() != null ? (exp.getSolicitante().getTipoDocumento() + ": " + exp.getSolicitante().getNumeroDocumento()) : "";
        String solCorreo = exp.getSolicitante() != null ? exp.getSolicitante().getCorreo() : "";

        return SeguimientoCiudadanoDetalleDto.builder()
                .expedienteId(exp.getId())
                .uuid(exp.getUuid())
                .numeroExpediente(exp.getNumeroExpediente() != null ? exp.getNumeroExpediente() : "EN TRÁMITE INICIAL")
                .codigoSeguimiento(cs.getCodigo())
                .asunto(exp.getAsunto())
                .descripcion(exp.getDescripcion())
                .tipoTramite(exp.getTipoTramite() != null ? exp.getTipoTramite().getNombre() : "Trámite General")
                .solicitanteNombre(solNombre)
                .solicitanteDocumento(solDoc)
                .solicitanteCorreo(solCorreo)
                .oficinaResponsable(exp.getOficinaResponsable() != null ? exp.getOficinaResponsable().getNombre() : "Mesa de Partes General")
                .estado(exp.getEstadoActual())
                .estadoDescripcionCiudadana(descCiudadana)
                .etapaProgreso(etapaActual)
                .fechaPresentacion(exp.getFechaPresentacion())
                .fechaRecepcion(exp.getFechaRecepcion())
                .fechaInicioAtencion(exp.getFechaInicioAtencion())
                .fechaLimite(exp.getFechaLimite())
                .diasRestantes(diasRestantes)
                .resultadoFinal(exp.getResultadoFinal())
                .fechaCierre(exp.getFechaCierre())
                .esReabierto(Boolean.TRUE.equals(exp.getEsReabierto()))
                .etapasProgreso(etapas)
                .lineaTiempo(lineaTiempo)
                .observaciones(observaciones)
                .subsanaciones(subsanaciones)
                .documentosDisponibles(documentos)
                .build();
    }

    @Override
    @Transactional
    public SeguimientoCiudadanoDetalleDto obtenerSeguimientoDetallePorUuid(UUID expedienteUuid, String username) {
        Expediente exp = expedienteRepository.findByUuid(expedienteUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con identificador: " + expedienteUuid));
        return obtenerSeguimientoDetalle(exp.getId(), username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoLineaTiempoCiudadanaDto> obtenerLineaTiempoCiudadana(Long expedienteId, String username) {
        Expediente exp = expedienteRepository.findById(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));
        validarAccesoPropietario(exp, username);
        return obtenerLineaTiempoCiudadanaInterna(exp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObservacionCiudadanaDto> obtenerObservacionesCiudadanas(Long expedienteId, String username) {
        Expediente exp = expedienteRepository.findById(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));
        validarAccesoPropietario(exp, username);
        return obtenerObservacionesCiudadanasInterna(exp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoDisponibleDto> obtenerDocumentosDisponibles(Long expedienteId, String username) {
        Expediente exp = expedienteRepository.findById(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));
        validarAccesoPropietario(exp, username);
        return obtenerDocumentosDisponiblesInterna(exp);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] descargarDocumentoAutorizado(Long expedienteId, Long documentoId, String username) {
        Expediente exp = expedienteRepository.findById(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));
        validarAccesoPropietario(exp, username);

        Documento doc = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con ID: " + documentoId));

        // Verificar que el documento realmente pertenece al expediente o a sus decisiones
        boolean esDelExpediente = expedienteDocumentoRepository.existsByExpedienteIdAndDocumentoId(expedienteId, documentoId);
        boolean esDeDecision = decisionRepository.findByExpedienteIdOrderByFechaDecisionDesc(expedienteId).stream()
                .anyMatch(d -> d.getDocumento() != null && d.getDocumento().getId().equals(documentoId));

        if (!esDelExpediente && !esDeDecision) {
            throw new AccessDeniedException("El documento solicitado no forma parte de las actuaciones autorizadas de este expediente.");
        }

        try (var in = documentoService.descargarDocumento(doc.getId(), null, username).getInputStream()) {
            return in.readAllBytes();
        } catch (Exception e) {
            log.error("Error al descargar documento {} de almacenamiento: {}", documentoId, e.getMessage());
            throw new BusinessException("No fue posible recuperar el archivo físico del almacenamiento.");
        }
    }

    private List<EventoLineaTiempoCiudadanaDto> obtenerLineaTiempoCiudadanaInterna(Expediente exp) {
        List<EventoLineaTiempoCiudadanaDto> eventos = new ArrayList<>();
        List<ExpedienteHistorial> historial = historialRepository.findByExpedienteIdOrderByFechaMovimientoAsc(exp.getId());

        for (ExpedienteHistorial h : historial) {
            String titulo = traducirAccionHistorial(h.getEstadoNuevo() != null ? h.getEstadoNuevo().name() : h.getAccion());
            String desc = h.getObservacion();
            // Filtrar referencias internas si contiene información administrativa sensible
            if (desc != null && desc.contains("[INTERNO]")) {
                desc = "Trámite registrado y procesado administrativamente";
            }
            String ofi = h.getOficina() != null ? h.getOficina().getNombre() : "Mesa de Partes Virtual";

            eventos.add(EventoLineaTiempoCiudadanaDto.builder()
                    .fecha(h.getFechaMovimiento())
                    .estado(h.getEstadoNuevo() != null ? h.getEstadoNuevo().name() : "EVENTO")
                    .titulo(titulo)
                    .descripcion(desc)
                    .oficina(ofi)
                    .build());
        }

        if (eventos.isEmpty() && exp.getFechaPresentacion() != null) {
            eventos.add(EventoLineaTiempoCiudadanaDto.builder()
                    .fecha(exp.getFechaPresentacion())
                    .estado("PRESENTADO")
                    .titulo("Solicitud Presentada")
                    .descripcion("Ingreso formal de la solicitud a través del Portal Ciudadano de Mesa de Partes Virtual")
                    .oficina("Mesa de Partes General")
                    .build());
        }

        return eventos;
    }

    private List<ObservacionCiudadanaDto> obtenerObservacionesCiudadanasInterna(Expediente exp) {
        List<ObservacionExpediente> obs = observacionRepository
                .findByExpedienteIdAndVisibilidadOrderByFechaRegistroDesc(exp.getId(), VisibilidadObservacion.CIUDADANO);

        List<ObservacionCiudadanaDto> lista = new ArrayList<>();
        for (ObservacionExpediente o : obs) {
            Integer diasRest = null;
            if (o.getFechaLimite() != null) {
                long d = Duration.between(OffsetDateTime.now(), o.getFechaLimite()).toDays();
                diasRest = (int) d;
            }

            boolean permiteSub = o.getEstado() == EstadoObservacion.PENDIENTE
                    && exp.getEstadoActual() == EstadoExpediente.OBSERVADO;

            lista.add(ObservacionCiudadanaDto.builder()
                    .id(o.getId())
                    .fecha(o.getFechaRegistro())
                    .descripcion(o.getDescripcion())
                    .requisitoNombre(o.getRequisito() != null ? o.getRequisito().getNombre() : "Requisito general")
                    .estado(o.getEstado().name())
                    .fechaLimite(o.getFechaLimite())
                    .diasRestantes(diasRest)
                    .permiteSubsanar(permiteSub)
                    .build());
        }
        return lista;
    }

    private List<SubsanacionCiudadanaDto> obtenerSubsanacionesCiudadanasInterna(Expediente exp) {
        List<SubsanacionExpediente> subs = subsanacionRepository.findByExpedienteIdOrderByFechaSubsanacionDesc(exp.getId());
        List<SubsanacionCiudadanaDto> lista = new ArrayList<>();
        for (SubsanacionExpediente s : subs) {
            String reqDesc = s.getObservacion() != null ? s.getObservacion().getDescripcion() : "Requerimiento de subsanación";
            lista.add(SubsanacionCiudadanaDto.builder()
                    .id(s.getId())
                    .fechaSubsanacion(s.getFechaSubsanacion())
                    .requerimiento(reqDesc)
                    .respuestaCiudadana(s.getRespuestaCiudadano())
                    .estado(s.getEstado() != null ? s.getEstado().name() : "RECIBIDA")
                    .totalDocumentos(0)
                    .build());
        }
        return lista;
    }

    private List<DocumentoDisponibleDto> obtenerDocumentosDisponiblesInterna(Expediente exp) {
        List<DocumentoDisponibleDto> lista = new ArrayList<>();

        // 1. Constancia oficial de presentación
        if (exp.getEstadoActual() != EstadoExpediente.BORRADOR) {
            lista.add(DocumentoDisponibleDto.builder()
                    .id(null)
                    .uuid(exp.getUuid())
                    .nombreArchivo("Constancia-Presentacion-" + (exp.getNumeroExpediente() != null ? exp.getNumeroExpediente() : exp.getUuid()) + ".pdf")
                    .tipoDocumento("Constancia de Presentación Electrónica")
                    .hashSha256("SHA256-CONSTANCIA-GENERADA")
                    .tamanoBytes(null)
                    .fechaEmision(exp.getFechaPresentacion() != null ? exp.getFechaPresentacion() : exp.getCreatedAt())
                    .urlDescarga("/mesa-partes/expedientes/" + exp.getUuid() + "/constancia")
                    .build());
        }

        // 2. Constancia formal de recepción si ya fue recepcionado
        if (exp.getFechaRecepcion() != null) {
            lista.add(DocumentoDisponibleDto.builder()
                    .id(null)
                    .uuid(exp.getUuid())
                    .nombreArchivo("Constancia-Recepcion-" + exp.getNumeroExpediente() + ".pdf")
                    .tipoDocumento("Constancia Oficial de Recepción y Radicación")
                    .hashSha256("SHA256-RECEPCION-OFICIAL")
                    .tamanoBytes(null)
                    .fechaEmision(exp.getFechaRecepcion())
                    .urlDescarga("/api/mesa-partes/expedientes/" + exp.getId() + "/constancia-recepcion")
                    .build());
        }

        // 3. Resoluciones oficiales emitidas y notificadas
        List<DecisionExpediente> decisiones = decisionRepository.findByExpedienteIdOrderByFechaDecisionDesc(exp.getId());
        for (DecisionExpediente dec : decisiones) {
            if (dec.getEstadoEmision() == EstadoEmisionDocumento.EMITIDO && dec.getDocumento() != null) {
                Documento doc = dec.getDocumento();
                lista.add(DocumentoDisponibleDto.builder()
                        .id(doc.getId())
                        .uuid(doc.getIdentificadorPublico())
                        .nombreArchivo(dec.getNumeroDocumento() + ".pdf")
                        .tipoDocumento("Acto Resolutivo: " + dec.getTipoDecision().name())
                        .hashSha256(doc.getHashSha256())
                        .tamanoBytes(doc.getTamanoBytes())
                        .fechaEmision(dec.getFechaEmision() != null ? dec.getFechaEmision() : dec.getFechaDecision())
                        .urlDescarga("/dashboard/mesa-partes/decisiones/" + dec.getId() + "/descargar-pdf")
                        .build());
            }
        }

        // 4. Documentos presentados por el ciudadano (recaudos no confidenciales)
        List<ExpedienteDocumento> docs = expedienteDocumentoRepository.findByExpedienteIdWithDocumento(exp.getId());
        for (ExpedienteDocumento ed : docs) {
            Documento d = ed.getDocumento();
            if (d != null) {
                lista.add(DocumentoDisponibleDto.builder()
                        .id(d.getId())
                        .uuid(d.getIdentificadorPublico())
                        .nombreArchivo(d.getNombreOriginal())
                        .tipoDocumento("Recaudo: " + (ed.getTipoRelacion() != null ? ed.getTipoRelacion().name() : "ANEXO"))
                        .hashSha256(d.getHashSha256())
                        .tamanoBytes(d.getTamanoBytes())
                        .fechaEmision(ed.getFechaAsociacion())
                        .urlDescarga("/api/documentos/" + d.getIdentificadorPublico() + "/descargar")
                        .build());
            }
        }

        return lista;
    }

    private void validarAccesoPropietario(Expediente exp, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        // Permite acceso a administradores o personal directivo
        boolean esPersonal = usuario.getRoles() != null && usuario.getRoles().stream()
                .anyMatch(r -> r.getCodigo().equals("ROLE_ADMIN")
                        || r.getCodigo().equals("ROLE_SUPERADMIN")
                        || r.getCodigo().equals("ROLE_JEFE_OFICINA")
                        || r.getCodigo().equals("ROLE_MESA_PARTES_OPERADOR"));

        if (esPersonal) {
            return;
        }

        // Si es ciudadano, solo puede consultar su propio expediente
        if (usuario.getPersona() == null || exp.getSolicitante() == null
                || !usuario.getPersona().getId().equals(exp.getSolicitante().getId())) {
            log.warn("Intento de acceso no autorizado (IDOR) de usuario '{}' al expediente '{}'", username, exp.getNumeroExpediente());
            throw new AccessDeniedException("No tiene autorización para consultar la información de este expediente.");
        }
    }

    private String traducirAccionHistorial(String accion) {
        if (accion == null) return "Movimiento Administrativo";
        return switch (accion) {
            case "PRESENTADO", "PRESENTACION" -> "Solicitud Presentada Formalmente";
            case "RECIBIDO", "RECEPCION" -> "Recepción y Asignación de Correlativo";
            case "EN_REVISION" -> "Inicio de Revisión Documental y Requisitos";
            case "OBSERVADO", "OBSERVACION" -> "Notificación de Observaciones al Administrado";
            case "EN_SUBSANACION", "SUBSANACION" -> "Ingreso de Escrito y Recaudos de Subsanación";
            case "DERIVADO", "DERIVACION" -> "Derivación a Oficina Especializada";
            case "EN_ATENCION", "ATENCION" -> "Inicio de Evaluación Técnica y Sustanciación";
            case "RESUELTO", "RESOLUCION" -> "Emisión Formal de Resolución";
            case "NOTIFICADO", "NOTIFICACION" -> "Diligenciamiento Válido de Cédula de Notificación";
            case "CERRADO", "CIERRE" -> "Conclusión Formal del Procedimiento";
            case "ARCHIVADO", "ARCHIVO" -> "Custodia Pasiva en Archivo Central";
            case "REABIERTO", "REAPERTURA" -> "Reapertura Excepcional Justificada";
            default -> "Actuación Administrativa: " + accion;
        };
    }
}
