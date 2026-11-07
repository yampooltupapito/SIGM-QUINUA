package pe.gob.municipalidad.sigm.modules.mercados.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AutorizacionComercialService {

    private final AutorizacionComercialRepository autorizacionRepository;
    private final AutorizacionHistorialRepository historialRepository;
    private final TipoAutorizacionRepository tipoAutorizacionRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final ComercianteRepository comercianteRepository;
    private final PuestoComercialRepository puestoRepository;
    private final OficinaRepository oficinaRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final AuditoriaAdministrativaService auditoriaService;

    @PersistenceContext
    private EntityManager entityManager;

    public AutorizacionComercialService(
            AutorizacionComercialRepository autorizacionRepository,
            AutorizacionHistorialRepository historialRepository,
            TipoAutorizacionRepository tipoAutorizacionRepository,
            AsignacionPuestoRepository asignacionRepository,
            ComercianteRepository comercianteRepository,
            PuestoComercialRepository puestoRepository,
            OficinaRepository oficinaRepository,
            QrCodeGenerator qrCodeGenerator,
            AuditoriaAdministrativaService auditoriaService) {
        this.autorizacionRepository = autorizacionRepository;
        this.historialRepository = historialRepository;
        this.tipoAutorizacionRepository = tipoAutorizacionRepository;
        this.asignacionRepository = asignacionRepository;
        this.comercianteRepository = comercianteRepository;
        this.puestoRepository = puestoRepository;
        this.oficinaRepository = oficinaRepository;
        this.qrCodeGenerator = qrCodeGenerator;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public AutorizacionResponse crearBorrador(AutorizacionCreateRequest request, String username) {
        Comerciante comerciante = null;
        PuestoComercial puesto = null;
        AsignacionPuesto asignacion = null;

        if (request.getAsignacionId() != null) {
            asignacion = asignacionRepository.findByIdCompleto(request.getAsignacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada: " + request.getAsignacionId()));
            puesto = asignacion.getPuesto();
            comerciante = asignacion.getComerciante();
        } else {
            if (request.getComercianteId() == null) {
                throw new BusinessException("El comerciante titular es obligatorio para la autorización comercial.");
            }
            comerciante = comercianteRepository.findById(request.getComercianteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado: " + request.getComercianteId()));
            if (request.getPuestoId() != null) {
                puesto = puestoRepository.findById(request.getPuestoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado: " + request.getPuestoId()));
            }
        }

        if (comerciante == null) {
            throw new BusinessException("El comerciante titular es obligatorio para la autorización comercial.");
        }

        if (request.getFechaFin() != null && request.getFechaInicio() != null && request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        AutorizacionComercial aut = new AutorizacionComercial();
        aut.setAsignacion(asignacion);
        aut.setComerciante(comerciante);
        aut.setPuesto(puesto);

        Long seq = autorizacionRepository.obtenerSiguienteCorrelativo();
        int anio = Year.now().getValue();
        String codigoGenerado = String.format("AUT-%d-%06d", anio, seq);
        aut.setCodigoAutorizacion(codigoGenerado);
        aut.setNumeroAutorizacion(codigoGenerado);

        if (request.getTipoAutorizacionId() != null) {
            TipoAutorizacion ta = tipoAutorizacionRepository.findById(request.getTipoAutorizacionId()).orElse(null);
            aut.setTipoAutorizacionEntity(ta);
            if (ta != null) aut.setTipoAutorizacion(ta.getCodigo());
        } else if (request.getTipoAutorizacion() != null) {
            aut.setTipoAutorizacion(request.getTipoAutorizacion());
        }

        aut.setFechaSolicitud(request.getFechaSolicitud() != null ? request.getFechaSolicitud() : LocalDate.now());
        aut.setFechaInicio(request.getFechaInicio());
        aut.setFechaFin(request.getFechaFin());
        aut.setEstado(EstadoAutorizacion.BORRADOR);
        Long expId = request.getExpedienteId() != null ? request.getExpedienteId() : (asignacion != null ? asignacion.getExpedienteId() : null);
        if (expId != null) {
            validarExpedienteExistente(expId);
        }
        aut.setExpedienteId(expId);

        if (request.getOficinaEmisoraId() != null) {
            aut.setOficinaEmisora(oficinaRepository.findById(request.getOficinaEmisoraId()).orElse(null));
        }
        if (request.getAutoridadCompetenteId() != null) {
            aut.setAutoridadCompetente(oficinaRepository.findById(request.getAutoridadCompetenteId()).orElse(null));
        }

        aut.setObservaciones(request.getObservaciones());

        AutorizacionComercial guardado = autorizacionRepository.save(aut);

        historialRepository.save(new AutorizacionHistorial(
                guardado, null, EstadoAutorizacion.BORRADOR.name(), "CREAR_BORRADOR",
                "Registro inicial de solicitud de autorización comercial", username, guardado.getCodigoAutorizacion()));

        auditoriaService.registrar("CREAR_BORRADOR_AUTORIZACION", "mercados_autorizaciones", guardado.getId(),
                "EXITO", "Autorización iniciada con código: " + guardado.getCodigoAutorizacion());

        return toResponse(guardado);
    }

    @Transactional
    public AutorizacionResponse actualizarBorrador(UUID id, AutorizacionUpdateRequest request, String username) {
        AutorizacionComercial aut = autorizacionRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autorización no encontrada: " + id));

        if (!aut.getEstado().isEditable()) {
            throw new BusinessException("Solo se pueden modificar autorizaciones en estado BORRADOR u OBSERVADA.");
        }

        if (request.getFechaFin() != null && request.getFechaInicio() != null && request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        if (request.getTipoAutorizacionId() != null) {
            TipoAutorizacion ta = tipoAutorizacionRepository.findById(request.getTipoAutorizacionId()).orElse(null);
            aut.setTipoAutorizacionEntity(ta);
            if (ta != null) aut.setTipoAutorizacion(ta.getCodigo());
        }

        aut.setFechaInicio(request.getFechaInicio());
        aut.setFechaFin(request.getFechaFin());
        if (request.getExpedienteId() != null) {
            validarExpedienteExistente(request.getExpedienteId());
            aut.setExpedienteId(request.getExpedienteId());
        }
        if (request.getOficinaEmisoraId() != null) {
            aut.setOficinaEmisora(oficinaRepository.findById(request.getOficinaEmisoraId()).orElse(null));
        }
        if (request.getAutoridadCompetenteId() != null) {
            aut.setAutoridadCompetente(oficinaRepository.findById(request.getAutoridadCompetenteId()).orElse(null));
        }
        if (request.getObservaciones() != null) aut.setObservaciones(request.getObservaciones());

        AutorizacionComercial guardado = autorizacionRepository.save(aut);

        historialRepository.save(new AutorizacionHistorial(
                guardado, guardado.getEstado().name(), guardado.getEstado().name(), "ACTUALIZAR_DATOS",
                "Actualización de requisitos y datos en borrador", username, guardado.getCodigoAutorizacion()));

        return toResponse(guardado);
    }

    @Transactional
    public AutorizacionResponse enviarAEvaluacion(UUID id, String username) {
        AutorizacionComercial aut = autorizacionRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autorización no encontrada: " + id));

        validarTransicion(aut, EstadoAutorizacion.EN_EVALUACION);
        String anterior = aut.getEstado().name();
        aut.setEstado(EstadoAutorizacion.EN_EVALUACION);

        AutorizacionComercial guardado = autorizacionRepository.save(aut);
        historialRepository.save(new AutorizacionHistorial(
                guardado, anterior, EstadoAutorizacion.EN_EVALUACION.name(), "ENVIAR_A_EVALUACION",
                "Presentación formal para evaluación técnica", username, guardado.getCodigoAutorizacion()));

        auditoriaService.registrar("ENVIAR_A_EVALUACION_AUTORIZACION", "mercados_autorizaciones", guardado.getId(),
                "EXITO", "Autorización enviada a evaluación");

        return toResponse(guardado);
    }

    @Transactional
    public AutorizacionResponse observar(UUID id, AutorizacionEstadoRequest request, String username) {
        AutorizacionComercial aut = autorizacionRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autorización no encontrada: " + id));

        validarTransicion(aut, EstadoAutorizacion.OBSERVADA);
        if (request.getMotivo() == null || request.getMotivo().isBlank()) {
            throw new BusinessException("El motivo de observación es obligatorio.");
        }

        String anterior = aut.getEstado().name();
        aut.setEstado(EstadoAutorizacion.OBSERVADA);
        aut.setMotivoObservacion(request.getMotivo());
        aut.setResponsableEvaluacion(username);
        aut.setFechaEvaluacion(OffsetDateTime.now());

        AutorizacionComercial guardado = autorizacionRepository.save(aut);
        historialRepository.save(new AutorizacionHistorial(
                guardado, anterior, EstadoAutorizacion.OBSERVADA.name(), "OBSERVAR_SOLICITUD",
                request.getMotivo(), username, request.getDocumentoReferencia()));

        auditoriaService.registrar("OBSERVAR_AUTORIZACION", "mercados_autorizaciones", guardado.getId(),
                "EXITO", "Autorización observada: " + request.getMotivo());

        return toResponse(guardado);
    }

    @Transactional
    public AutorizacionResponse aprobar(UUID id, AutorizacionEstadoRequest request, String username) {
        AutorizacionComercial aut = autorizacionRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autorización no encontrada: " + id));

        validarTransicion(aut, EstadoAutorizacion.APROBADA);
        String anterior = aut.getEstado().name();
        aut.setEstado(EstadoAutorizacion.APROBADA);
        aut.setResponsableAprobacion(username);
        aut.setFechaAprobacion(OffsetDateTime.now());

        AutorizacionComercial guardado = autorizacionRepository.save(aut);
        historialRepository.save(new AutorizacionHistorial(
                guardado, anterior, EstadoAutorizacion.APROBADA.name(), "APROBAR_AUTORIZACION",
                request.getMotivo() != null ? request.getMotivo() : "Aprobación técnica de solicitud",
                username, request.getDocumentoReferencia()));

        auditoriaService.registrar("APROBAR_AUTORIZACION", "mercados_autorizaciones", guardado.getId(),
                "EXITO", "Autorización aprobada formalmente");

        return toResponse(guardado);
    }

    @Transactional
    public AutorizacionResponse emitir(UUID id, AutorizacionEstadoRequest request, String username) {
        AutorizacionComercial aut = autorizacionRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autorización no encontrada: " + id));

        validarTransicion(aut, EstadoAutorizacion.VIGENTE);

        if (aut.getFechaInicio() == null) {
            aut.setFechaInicio(LocalDate.now());
        }
        if (aut.getFechaFin() == null) {
            int vigenciaMeses = aut.getTipoAutorizacionEntity() != null ?
                    aut.getTipoAutorizacionEntity().getVigenciaMesesDefecto() : 12;
            aut.setFechaFin(aut.getFechaInicio().plusMonths(vigenciaMeses));
        }

        String anterior = aut.getEstado().name();
        aut.setEstado(EstadoAutorizacion.VIGENTE);
        aut.setFechaEmision(LocalDate.now());

        if (aut.getCodigoSeguro() == null) {
            aut.setCodigoSeguro(UUID.randomUUID().toString().replace("-", ""));
        }

        String hashData = aut.getCodigoAutorizacion() + "|" + aut.getCodigoSeguro() + "|" + aut.getFechaEmision();
        aut.setHashDocumento(calcularHashSha256(hashData));

        AutorizacionComercial guardado = autorizacionRepository.save(aut);
        historialRepository.save(new AutorizacionHistorial(
                guardado, anterior, EstadoAutorizacion.VIGENTE.name(), "EMITIR_Y_ACTIVAR",
                request.getMotivo() != null ? request.getMotivo() : "Emisión oficial de acto administrativo habilitante",
                username, request.getDocumentoReferencia()));

        auditoriaService.registrar("EMITIR_AUTORIZACION", "mercados_autorizaciones", guardado.getId(),
                "EXITO", "Autorización emitida con vigencia oficial");

        return toResponse(guardado);
    }

    @Transactional
    public AutorizacionResponse suspender(UUID id, AutorizacionEstadoRequest request, String username) {
        AutorizacionComercial aut = autorizacionRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autorización no encontrada: " + id));

        validarTransicion(aut, EstadoAutorizacion.SUSPENDIDA);
        if (request.getMotivo() == null || request.getMotivo().isBlank()) {
            throw new BusinessException("El motivo de suspensión es obligatorio.");
        }

        String anterior = aut.getEstado().name();
        aut.setEstado(EstadoAutorizacion.SUSPENDIDA);
        aut.setMotivoSuspension(request.getMotivo());

        AutorizacionComercial guardado = autorizacionRepository.save(aut);
        historialRepository.save(new AutorizacionHistorial(
                guardado, anterior, EstadoAutorizacion.SUSPENDIDA.name(), "SUSPENDER_AUTORIZACION",
                request.getMotivo(), username, request.getDocumentoReferencia()));

        auditoriaService.registrar("SUSPENDER_AUTORIZACION", "mercados_autorizaciones", guardado.getId(),
                "EXITO", "Autorización suspendida: " + request.getMotivo());

        return toResponse(guardado);
    }

    @Transactional
    public AutorizacionResponse revocar(UUID id, AutorizacionEstadoRequest request, String username) {
        AutorizacionComercial aut = autorizacionRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autorización no encontrada: " + id));

        validarTransicion(aut, EstadoAutorizacion.REVOCADA);
        if (request.getMotivo() == null || request.getMotivo().isBlank()) {
            throw new BusinessException("El motivo de revocación administrativa es obligatorio.");
        }

        String anterior = aut.getEstado().name();
        aut.setEstado(EstadoAutorizacion.REVOCADA);
        aut.setMotivoRevocacion(request.getMotivo());

        AutorizacionComercial guardado = autorizacionRepository.save(aut);
        historialRepository.save(new AutorizacionHistorial(
                guardado, anterior, EstadoAutorizacion.REVOCADA.name(), "REVOCAR_AUTORIZACION",
                request.getMotivo(), username, request.getDocumentoReferencia()));

        auditoriaService.registrar("REVOCAR_AUTORIZACION", "mercados_autorizaciones", guardado.getId(),
                "EXITO", "Autorización revocada: " + request.getMotivo());

        return toResponse(guardado);
    }

    @Transactional
    public AutorizacionResponse anular(UUID id, AutorizacionEstadoRequest request, String username) {
        AutorizacionComercial aut = autorizacionRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autorización no encontrada: " + id));

        validarTransicion(aut, EstadoAutorizacion.ANULADA);
        if (request.getMotivo() == null || request.getMotivo().isBlank()) {
            throw new BusinessException("El motivo de anulación es obligatorio.");
        }

        String anterior = aut.getEstado().name();
        aut.setEstado(EstadoAutorizacion.ANULADA);

        AutorizacionComercial guardado = autorizacionRepository.save(aut);
        historialRepository.save(new AutorizacionHistorial(
                guardado, anterior, EstadoAutorizacion.ANULADA.name(), "ANULAR_AUTORIZACION",
                request.getMotivo(), username, request.getDocumentoReferencia()));

        auditoriaService.registrar("ANULAR_AUTORIZACION", "mercados_autorizaciones", guardado.getId(),
                "EXITO", "Autorización anulada: " + request.getMotivo());

        return toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public AutorizacionResponse obtenerPorId(UUID id) {
        AutorizacionComercial aut = autorizacionRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autorización no encontrada con ID: " + id));
        return toResponse(aut);
    }

    @Transactional(readOnly = true)
    public AutorizacionDetailResponse obtenerDetalle(UUID id) {
        AutorizacionComercial a = autorizacionRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autorización no encontrada con ID: " + id));

        AutorizacionDetailResponse d = new AutorizacionDetailResponse();
        d.setId(a.getId());
        d.setCodigoAutorizacion(a.getCodigoAutorizacion());
        d.setNumeroAutorizacion(a.getNumeroAutorizacion());
        if (a.getAsignacion() != null) {
            d.setAsignacionId(a.getAsignacion().getId());
            d.setCodigoAsignacion(a.getAsignacion().getCodigoAsignacion());
        }
        if (a.getComerciante() != null) {
            d.setComercianteId(a.getComerciante().getId());
            d.setComercianteNombre(a.getComerciante().getNombreOTitular());
            d.setComercianteDocumento(a.getComerciante().getNumeroDocumentoTitular());
            d.setComercianteTipo(a.getComerciante().getTipoComerciante().name());
            d.setComercianteTelefono(a.getComerciante().getTelefonoContacto());
            d.setComercianteCorreo(a.getComerciante().getCorreoContacto());
            d.setComercianteDireccion(a.getComerciante().getDireccionContacto());
        }
        if (a.getPuesto() != null) {
            d.setPuestoId(a.getPuesto().getId());
            d.setPuestoCodigo(a.getPuesto().getCodigo());
            d.setPuestoNumero(a.getPuesto().getNumero());
            d.setPuestoSector(a.getPuesto().getSector() != null ? a.getPuesto().getSector().getNombre() : "-");
            d.setPuestoPabellon(a.getPuesto().getPabellon() != null ? a.getPuesto().getPabellon().getNombre() : "-");
            d.setPuestoTipo(a.getPuesto().getTipoPuesto() != null ? a.getPuesto().getTipoPuesto().getNombre() : "-");
            if (a.getPuesto().getMercado() != null) {
                d.setMercadoId(a.getPuesto().getMercado().getId());
                d.setMercadoNombre(a.getPuesto().getMercado().getNombre());
            }
        }
        if (a.getTipoAutorizacionEntity() != null) {
            d.setTipoAutorizacionId(a.getTipoAutorizacionEntity().getId());
            d.setTipoAutorizacion(a.getTipoAutorizacionEntity().getCodigo());
            d.setTipoAutorizacionNombre(a.getTipoAutorizacionEntity().getNombre());
            d.setTipoAutorizacionProcedimiento(a.getTipoAutorizacionEntity().getProcedimientoRelacionado());
            d.setTipoAutorizacionFuenteNormativa(a.getTipoAutorizacionEntity().getFuenteNormativa());
        } else {
            d.setTipoAutorizacion(a.getTipoAutorizacion());
            d.setTipoAutorizacionNombre(a.getTipoAutorizacion());
        }

        d.setFechaSolicitud(a.getFechaSolicitud());
        d.setFechaEmision(a.getFechaEmision());
        d.setFechaInicio(a.getFechaInicio());
        d.setFechaFin(a.getFechaFin());
        d.setEstado(a.getEstado());
        d.setEstadoNombre(a.getEstado().name());

        if (a.getFechaFin() != null) {
            d.setDiasRestantes(ChronoUnit.DAYS.between(LocalDate.now(), a.getFechaFin()));
        }

        d.setDocumentoId(a.getDocumento() != null ? a.getDocumento().getId() : null);
        d.setExpedienteId(a.getExpedienteId());
        if (a.getOficinaEmisora() != null) {
            d.setOficinaEmisoraId(a.getOficinaEmisora().getId());
            d.setOficinaEmisoraNombre(a.getOficinaEmisora().getNombre());
        }
        if (a.getAutoridadCompetente() != null) {
            d.setAutoridadCompetenteId(a.getAutoridadCompetente().getId());
            d.setAutoridadCompetenteNombre(a.getAutoridadCompetente().getNombre());
        }
        d.setResponsableEvaluacion(a.getResponsableEvaluacion());
        d.setResponsableAprobacion(a.getResponsableAprobacion());
        d.setFechaEvaluacion(a.getFechaEvaluacion());
        d.setFechaAprobacion(a.getFechaAprobacion());
        d.setCodigoSeguro(a.getCodigoSeguro());
        d.setHashDocumento(a.getHashDocumento());
        d.setMotivoObservacion(a.getMotivoObservacion());
        d.setMotivoSuspension(a.getMotivoSuspension());
        d.setMotivoRevocacion(a.getMotivoRevocacion());
        d.setObservaciones(a.getObservaciones());
        d.setCreatedAt(a.getCreatedAt());
        d.setUpdatedAt(a.getUpdatedAt());
        d.setCreatedBy(a.getCreatedBy());

        List<AutorizacionHistorial> histList = historialRepository.findByAutorizacionIdOrderByFechaAsc(a.getId());
        d.setHistorial(histList.stream().map(h -> new AutorizacionHistorialResponse(
                h.getId(), h.getEstadoAnterior(), h.getEstadoNuevo(), h.getAccion(),
                h.getMotivo(), h.getUsuario(), h.getFecha(), h.getDocumentoReferencia()
        )).collect(Collectors.toList()));

        return d;
    }

    @Transactional(readOnly = true)
    public Page<AutorizacionResponse> buscarConFiltros(
            String termino, UUID mercadoId, EstadoAutorizacion estado, String tipoAutorizacion,
            LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable) {
        Page<AutorizacionComercial> page;
        if (termino != null && !termino.trim().isEmpty()) {
            page = autorizacionRepository.buscarConFiltrosConTermino(
                    termino.trim(), mercadoId, estado, tipoAutorizacion, fechaDesde, fechaHasta, pageable);
        } else {
            page = autorizacionRepository.buscarConFiltrosSinTermino(
                    mercadoId, estado, tipoAutorizacion, fechaDesde, fechaHasta, pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public byte[] generarPdf(UUID id) {
        AutorizacionComercial a = autorizacionRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autorización no encontrada con ID: " + id));

        Document document = new Document(PageSize.A4, 40, 40, 45, 45);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontHeaderMuni = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.BLACK);
            Font fontSubMuni = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(29, 78, 216));
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.DARK_GRAY);
            Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.BLACK);
            Font fontDisclaimer = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7.5f, new Color(100, 116, 139));

            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontHeaderMuni);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(pHeader);

            Paragraph pSubHeader = new Paragraph("Subgerencia de Comercialización y Mercados Municipales\nQuinua — Huamanga — Ayacucho", fontSubMuni);
            pSubHeader.setAlignment(Element.ALIGN_CENTER);
            pSubHeader.setSpacingAfter(10);
            document.add(pSubHeader);

            Paragraph separator = new Paragraph("_____________________________________________________________________________\n", fontSubMuni);
            separator.setAlignment(Element.ALIGN_CENTER);
            separator.setSpacingAfter(10);
            document.add(separator);

            Paragraph pTitulo = new Paragraph("AUTORIZACIÓN COMERCIAL MUNICIPAL DE PUESTO DE MERCADO", fontTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);

            Paragraph pCodigo = new Paragraph("CÓDIGO OFICIAL: " + a.getCodigoAutorizacion(), fontSubtitulo);
            pCodigo.setAlignment(Element.ALIGN_CENTER);
            pCodigo.setSpacingAfter(15);
            document.add(pCodigo);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{35f, 65f});
            table.setSpacingBefore(5);
            table.setSpacingAfter(10);

            agregarFila(table, "CÓDIGO DE AUTORIZACIÓN:", a.getCodigoAutorizacion(), fontLabel, fontValue);
            agregarFila(table, "MODALIDAD DE AUTORIZACIÓN:", a.getTipoAutorizacionEntity() != null ? a.getTipoAutorizacionEntity().getNombre() : a.getTipoAutorizacion(), fontLabel, fontValue);
            agregarFila(table, "ESTADO ADMINISTRATIVO:", a.getEstado().name(), fontLabel, fontValue);
            agregarFila(table, "COMERCIANTE TITULAR:", a.getComerciante().getNombreOTitular(), fontLabel, fontValue);
            agregarFila(table, "DOCUMENTO IDENTIDAD:", a.getComerciante().getTipoDocumentoTitular() + " " + a.getComerciante().getNumeroDocumentoTitular(), fontLabel, fontValue);
            if (a.getPuesto() != null) {
                agregarFila(table, "MERCADO MUNICIPAL:", a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getNombre() : "-", fontLabel, fontValue);
                agregarFila(table, "PUESTO ASIGNADO:", a.getPuesto().getCodigo() + " (N° " + a.getPuesto().getNumero() + ")", fontLabel, fontValue);
            }
            agregarFila(table, "FECHA DE EMISIÓN:", a.getFechaEmision() != null ? a.getFechaEmision().toString() : "PENDIENTE", fontLabel, fontValue);
            agregarFila(table, "INICIO DE VIGENCIA:", a.getFechaInicio() != null ? a.getFechaInicio().toString() : "-", fontLabel, fontValue);
            agregarFila(table, "FINALIZACIÓN DE VIGENCIA:", a.getFechaFin() != null ? a.getFechaFin().toString() : "-", fontLabel, fontValue);
            if (a.getCodigoSeguro() != null) {
                agregarFila(table, "CÓDIGO DE SEGURIDAD (QR):", a.getCodigoSeguro(), fontLabel, fontValue);
            }
            if (a.getHashDocumento() != null) {
                agregarFila(table, "FIRMA DE INTEGRIDAD (SHA-256):", a.getHashDocumento(), fontLabel, fontValue);
            }
            document.add(table);

            Paragraph pDisclaimer = new Paragraph(
                    "Esta autorización municipal habilita exclusivamente el giro comercial declarado en el puesto adjudicado. " +
                    "Cualquier modificación o cesión sin autorización previa de la Subgerencia de Comercialización acarreará su revocación automática " +
                    "de conformidad con la Ley N° 27972 y ordenanzas municipales aplicables.",
                    fontDisclaimer);
            pDisclaimer.setAlignment(Element.ALIGN_JUSTIFIED);
            pDisclaimer.setSpacingAfter(15);
            document.add(pDisclaimer);

            if (a.getCodigoSeguro() != null) {
                String urlVerificacion = "http://localhost:8080/verificar/mercados/documento/" + a.getCodigoSeguro();
                byte[] qrBytes = qrCodeGenerator.generateQrCodePng(urlVerificacion, 110, 110);
                Image qrImage = Image.getInstance(qrBytes);
                qrImage.setAlignment(Element.ALIGN_CENTER);
                qrImage.scaleToFit(90, 90);
                document.add(qrImage);

                Paragraph pQrInfo = new Paragraph("Verifique la autenticidad de esta autorización escaneando el código QR oficial.", fontDisclaimer);
                pQrInfo.setAlignment(Element.ALIGN_CENTER);
                document.add(pQrInfo);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Error al generar el documento PDF de la autorización: " + e.getMessage());
        }
    }

    private void agregarFila(PdfPTable table, String label, String value, Font fontLabel, Font fontValue) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, fontLabel));
        c1.setBorder(Rectangle.BOTTOM);
        c1.setBorderColor(new Color(226, 232, 240));
        c1.setPadding(4f);
        c1.setBackgroundColor(new Color(248, 250, 252));
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value != null ? value : "-", fontValue));
        c2.setBorder(Rectangle.BOTTOM);
        c2.setBorderColor(new Color(226, 232, 240));
        c2.setPadding(4f);
        table.addCell(c2);
    }

    private void validarTransicion(AutorizacionComercial aut, EstadoAutorizacion nuevoEstado) {
        if (!aut.getEstado().canTransitionTo(nuevoEstado)) {
            throw new BusinessException(String.format("Transición de estado no autorizada: de %s a %s.",
                    aut.getEstado(), nuevoEstado));
        }
    }

    private String calcularHashSha256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            return "HASH-DEFAULT";
        }
    }

    private AutorizacionResponse toResponse(AutorizacionComercial a) {
        Long diasRestantes = null;
        if (a.getFechaFin() != null) {
            diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), a.getFechaFin());
        }

        return AutorizacionResponse.builder()
                .id(a.getId())
                .codigoAutorizacion(a.getCodigoAutorizacion())
                .numeroAutorizacion(a.getNumeroAutorizacion())
                .asignacionId(a.getAsignacion() != null ? a.getAsignacion().getId() : null)
                .codigoAsignacion(a.getAsignacion() != null ? a.getAsignacion().getCodigoAsignacion() : null)
                .comercianteId(a.getComerciante() != null ? a.getComerciante().getId() : null)
                .comercianteNombre(a.getComerciante() != null ? a.getComerciante().getNombreOTitular() : "-")
                .comercianteDocumento(a.getComerciante() != null ? a.getComerciante().getNumeroDocumentoTitular() : "-")
                .puestoId(a.getPuesto() != null ? a.getPuesto().getId() : null)
                .puestoCodigo(a.getPuesto() != null ? a.getPuesto().getCodigo() : "-")
                .puestoNumero(a.getPuesto() != null ? a.getPuesto().getNumero() : "-")
                .mercadoId(a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getId() : null)
                .mercadoNombre(a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getNombre() : "-")
                .tipoAutorizacion(a.getTipoAutorizacion())
                .tipoAutorizacionNombre(a.getTipoAutorizacionEntity() != null ? a.getTipoAutorizacionEntity().getNombre() : a.getTipoAutorizacion())
                .fechaSolicitud(a.getFechaSolicitud())
                .fechaEmision(a.getFechaEmision())
                .fechaInicio(a.getFechaInicio())
                .fechaFin(a.getFechaFin())
                .estado(a.getEstado())
                .estadoNombre(a.getEstado().name())
                .diasRestantes(diasRestantes)
                .codigoSeguro(a.getCodigoSeguro())
                .hashDocumento(a.getHashDocumento())
                .documentoId(a.getDocumento() != null ? a.getDocumento().getId() : null)
                .createdAt(a.getCreatedAt())
                .build();
    }

    private void validarExpedienteExistente(Long expId) {
        if (expId != null && entityManager != null) {
            Number count = (Number) entityManager.createNativeQuery(
                    "SELECT COUNT(1) FROM mpv_expedientes WHERE id = :expId"
            ).setParameter("expId", expId).getSingleResult();
            if (count == null || count.longValue() == 0) {
                throw new BusinessException("El Expediente MPV #" + expId + " no existe en el sistema. Deje el campo vacío si no dispone de un expediente.");
            }
        }
    }
}
