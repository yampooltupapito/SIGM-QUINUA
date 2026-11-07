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
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AsignacionPuestoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ContratoHistorialRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ContratoOcupacionRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.TipoContratoRepository;

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
public class ContratoOcupacionService {

    private final ContratoOcupacionRepository contratoRepository;
    private final ContratoHistorialRepository historialRepository;
    private final TipoContratoRepository tipoContratoRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final OficinaRepository oficinaRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final AuditoriaAdministrativaService auditoriaService;

    @PersistenceContext
    private EntityManager entityManager;

    public ContratoOcupacionService(
            ContratoOcupacionRepository contratoRepository,
            ContratoHistorialRepository historialRepository,
            TipoContratoRepository tipoContratoRepository,
            AsignacionPuestoRepository asignacionRepository,
            OficinaRepository oficinaRepository,
            QrCodeGenerator qrCodeGenerator,
            AuditoriaAdministrativaService auditoriaService) {
        this.contratoRepository = contratoRepository;
        this.historialRepository = historialRepository;
        this.tipoContratoRepository = tipoContratoRepository;
        this.asignacionRepository = asignacionRepository;
        this.oficinaRepository = oficinaRepository;
        this.qrCodeGenerator = qrCodeGenerator;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public ContratoResponse crearBorrador(ContratoCreateRequest request, String username) {
        if (request.getAsignacionId() == null) {
            throw new BusinessException("La asignación es obligatoria para emitir un contrato de ocupación.");
        }
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(request.getAsignacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada: " + request.getAsignacionId()));

        if (request.getFechaFin() != null && request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        // Control de solapamientos con contratos vigentes en el mismo puesto
        List<ContratoOcupacion> solapados = contratoRepository.detectarSolapamientoVigente(
                asignacion.getPuesto().getId(), request.getFechaInicio(), request.getFechaFin(), null);
        if (!solapados.isEmpty()) {
            throw new BusinessException("Existe un contrato vigente superpuesto para este puesto comercial en el período seleccionado.");
        }

        ContratoOcupacion contrato = new ContratoOcupacion();
        contrato.setAsignacion(asignacion);
        contrato.setComerciante(asignacion.getComerciante());
        contrato.setPuesto(asignacion.getPuesto());

        // Correlativo seguro
        Long seq = contratoRepository.obtenerSiguienteCorrelativo();
        int anio = Year.now().getValue();
        contrato.setCodigoContrato(String.format("CONT-%d-%06d", anio, seq));

        if (request.getTipoContratoId() != null) {
            TipoContrato tc = tipoContratoRepository.findById(request.getTipoContratoId()).orElse(null);
            contrato.setTipoContrato(tc);
            if (tc != null) {
                contrato.setTipoContratoCodigo(tc.getCodigo());
            }
        } else if (request.getTipoContratoCodigo() != null) {
            contrato.setTipoContratoCodigo(request.getTipoContratoCodigo());
        }

        contrato.setFechaElaboracion(request.getFechaElaboracion() != null ? request.getFechaElaboracion() : LocalDate.now());
        contrato.setFechaInicio(request.getFechaInicio());
        contrato.setFechaFin(request.getFechaFin());
        contrato.setEstado(EstadoContrato.BORRADOR);
        Long expId = request.getExpedienteId() != null ? request.getExpedienteId() : asignacion.getExpedienteId();
        if (expId != null) {
            validarExpedienteExistente(expId);
        }
        contrato.setExpedienteId(expId);

        if (request.getOficinaResponsableId() != null) {
            Oficina of = oficinaRepository.findById(request.getOficinaResponsableId()).orElse(null);
            contrato.setOficinaResponsable(of);
        }

        contrato.setResponsableElaboracion(username != null ? username : "sistema");
        contrato.setObservaciones(request.getObservaciones());

        ContratoOcupacion guardado = contratoRepository.save(contrato);

        // Registro de Trazabilidad
        historialRepository.save(new ContratoHistorial(
                guardado, null, EstadoContrato.BORRADOR.name(), "CREAR_BORRADOR",
                "Registro inicial de borrador de contrato de ocupación", username, guardado.getCodigoContrato()));

        auditoriaService.registrar("CREAR_BORRADOR_CONTRATO", "mercados_contratos", guardado.getId(),
                "EXITO", "Contrato elaborado con código: " + guardado.getCodigoContrato());

        return toResponse(guardado);
    }

    @Transactional
    public ContratoResponse actualizarBorrador(UUID id, ContratoUpdateRequest request, String username) {
        ContratoOcupacion contrato = contratoRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));

        if (!contrato.getEstado().isEditable()) {
            throw new BusinessException("Solo se pueden editar contratos en estado BORRADOR u OBSERVADO. Estado actual: " + contrato.getEstado());
        }

        if (request.getFechaFin() != null && request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        if (request.getTipoContratoId() != null) {
            TipoContrato tc = tipoContratoRepository.findById(request.getTipoContratoId()).orElse(null);
            contrato.setTipoContrato(tc);
            if (tc != null) contrato.setTipoContratoCodigo(tc.getCodigo());
        }

        contrato.setFechaInicio(request.getFechaInicio());
        contrato.setFechaFin(request.getFechaFin());
        if (request.getExpedienteId() != null) {
            validarExpedienteExistente(request.getExpedienteId());
            contrato.setExpedienteId(request.getExpedienteId());
        }
        if (request.getOficinaResponsableId() != null) {
            contrato.setOficinaResponsable(oficinaRepository.findById(request.getOficinaResponsableId()).orElse(null));
        }
        if (request.getObservaciones() != null) contrato.setObservaciones(request.getObservaciones());

        ContratoOcupacion guardado = contratoRepository.save(contrato);

        historialRepository.save(new ContratoHistorial(
                guardado, guardado.getEstado().name(), guardado.getEstado().name(), "ACTUALIZAR_DATOS",
                "Actualización de términos y condiciones en borrador", username, guardado.getCodigoContrato()));

        return toResponse(guardado);
    }

    @Transactional
    public ContratoResponse enviarARevision(UUID id, String username) {
        ContratoOcupacion contrato = contratoRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));

        validarTransicion(contrato, EstadoContrato.EN_REVISION);
        String estadoAnterior = contrato.getEstado().name();
        contrato.setEstado(EstadoContrato.EN_REVISION);

        ContratoOcupacion guardado = contratoRepository.save(contrato);
        historialRepository.save(new ContratoHistorial(
                guardado, estadoAnterior, EstadoContrato.EN_REVISION.name(), "ENVIAR_A_REVISION",
                "Envío para revisión legal y administrativa", username, guardado.getCodigoContrato()));

        auditoriaService.registrar("ENVIAR_A_REVISION_CONTRATO", "mercados_contratos", guardado.getId(),
                "EXITO", "Contrato derivado a revisión");

        return toResponse(guardado);
    }

    @Transactional
    public ContratoResponse observar(UUID id, ContratoEstadoRequest request, String username) {
        ContratoOcupacion contrato = contratoRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));

        validarTransicion(contrato, EstadoContrato.OBSERVADO);
        if (request.getMotivo() == null || request.getMotivo().isBlank()) {
            throw new BusinessException("El motivo de la observación es obligatorio.");
        }

        String estadoAnterior = contrato.getEstado().name();
        contrato.setEstado(EstadoContrato.OBSERVADO);
        contrato.setMotivoObservacion(request.getMotivo());

        ContratoOcupacion guardado = contratoRepository.save(contrato);
        historialRepository.save(new ContratoHistorial(
                guardado, estadoAnterior, EstadoContrato.OBSERVADO.name(), "REGISTRAR_OBSERVACION",
                request.getMotivo(), username, request.getDocumentoReferencia()));

        auditoriaService.registrar("OBSERVAR_CONTRATO", "mercados_contratos", guardado.getId(),
                "EXITO", "Contrato observado: " + request.getMotivo());

        return toResponse(guardado);
    }

    @Transactional
    public ContratoResponse aprobar(UUID id, ContratoEstadoRequest request, String username) {
        ContratoOcupacion contrato = contratoRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));

        validarTransicion(contrato, EstadoContrato.APROBADO);
        String estadoAnterior = contrato.getEstado().name();
        contrato.setEstado(EstadoContrato.APROBADO);
        contrato.setResponsableAprobacion(username != null ? username : "autoridad");
        contrato.setFechaAprobacion(OffsetDateTime.now());

        ContratoOcupacion guardado = contratoRepository.save(contrato);
        historialRepository.save(new ContratoHistorial(
                guardado, estadoAnterior, EstadoContrato.APROBADO.name(), "APROBAR_CONTRATO",
                request.getMotivo() != null ? request.getMotivo() : "Aprobación conforme de términos contractuales",
                username, request.getDocumentoReferencia()));

        auditoriaService.registrar("APROBAR_CONTRATO", "mercados_contratos", guardado.getId(),
                "EXITO", "Contrato aprobado formalmente");

        return toResponse(guardado);
    }

    @Transactional
    public ContratoResponse formalizar(UUID id, ContratoEstadoRequest request, String username) {
        ContratoOcupacion contrato = contratoRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));

        validarTransicion(contrato, EstadoContrato.VIGENTE);

        // Control final de solapamiento antes de activar vigencia
        List<ContratoOcupacion> solapados = contratoRepository.detectarSolapamientoVigente(
                contrato.getPuesto().getId(), contrato.getFechaInicio(), contrato.getFechaFin(), contrato.getId());
        if (!solapados.isEmpty()) {
            throw new BusinessException("No se puede formalizar: existe otro contrato vigente superpuesto en el mismo puesto.");
        }

        String estadoAnterior = contrato.getEstado().name();
        contrato.setEstado(EstadoContrato.VIGENTE);
        contrato.setFechaFormalizacion(OffsetDateTime.now());

        // Generación de código seguro tokenizado para verificación QR pública
        if (contrato.getCodigoSeguro() == null) {
            contrato.setCodigoSeguro(UUID.randomUUID().toString().replace("-", ""));
        }

        // Generar hash del documento
        String hashData = contrato.getCodigoContrato() + "|" + contrato.getCodigoSeguro() + "|" + contrato.getFechaInicio();
        contrato.setHashDocumento(calcularHashSha256(hashData));

        ContratoOcupacion guardado = contratoRepository.save(contrato);
        historialRepository.save(new ContratoHistorial(
                guardado, estadoAnterior, EstadoContrato.VIGENTE.name(), "FORMALIZAR_Y_ACTIVAR",
                request.getMotivo() != null ? request.getMotivo() : "Formalización física y entrada en vigencia legal",
                username, request.getDocumentoReferencia()));

        auditoriaService.registrar("FORMALIZAR_CONTRATO", "mercados_contratos", guardado.getId(),
                "EXITO", "Contrato formalizado y activado con vigencia");

        return toResponse(guardado);
    }

    @Transactional
    public ContratoResponse suspender(UUID id, ContratoEstadoRequest request, String username) {
        ContratoOcupacion contrato = contratoRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));

        validarTransicion(contrato, EstadoContrato.SUSPENDIDO);
        if (request.getMotivo() == null || request.getMotivo().isBlank()) {
            throw new BusinessException("El motivo de suspensión es obligatorio.");
        }

        String estadoAnterior = contrato.getEstado().name();
        contrato.setEstado(EstadoContrato.SUSPENDIDO);
        contrato.setMotivoSuspension(request.getMotivo());

        ContratoOcupacion guardado = contratoRepository.save(contrato);
        historialRepository.save(new ContratoHistorial(
                guardado, estadoAnterior, EstadoContrato.SUSPENDIDO.name(), "SUSPENDER_CONTRATO",
                request.getMotivo(), username, request.getDocumentoReferencia()));

        auditoriaService.registrar("SUSPENDER_CONTRATO", "mercados_contratos", guardado.getId(),
                "EXITO", "Contrato suspendido: " + request.getMotivo());

        return toResponse(guardado);
    }

    @Transactional
    public ContratoResponse terminar(UUID id, ContratoEstadoRequest request, String username) {
        ContratoOcupacion contrato = contratoRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));

        validarTransicion(contrato, EstadoContrato.TERMINADO);
        if (request.getMotivo() == null || request.getMotivo().isBlank()) {
            throw new BusinessException("El motivo de terminación es obligatorio.");
        }

        String estadoAnterior = contrato.getEstado().name();
        contrato.setEstado(EstadoContrato.TERMINADO);
        contrato.setMotivoTerminacion(request.getMotivo());
        if (request.getFechaEfectiva() != null) {
            contrato.setFechaFin(request.getFechaEfectiva());
        }

        ContratoOcupacion guardado = contratoRepository.save(contrato);
        historialRepository.save(new ContratoHistorial(
                guardado, estadoAnterior, EstadoContrato.TERMINADO.name(), "TERMINAR_CONTRATO",
                request.getMotivo(), username, request.getDocumentoReferencia()));

        auditoriaService.registrar("TERMINAR_CONTRATO", "mercados_contratos", guardado.getId(),
                "EXITO", "Contrato terminado: " + request.getMotivo());

        return toResponse(guardado);
    }

    @Transactional
    public ContratoResponse anular(UUID id, ContratoEstadoRequest request, String username) {
        ContratoOcupacion contrato = contratoRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado: " + id));

        validarTransicion(contrato, EstadoContrato.ANULADO);
        if (request.getMotivo() == null || request.getMotivo().isBlank()) {
            throw new BusinessException("El motivo de anulación es obligatorio.");
        }

        String estadoAnterior = contrato.getEstado().name();
        contrato.setEstado(EstadoContrato.ANULADO);

        ContratoOcupacion guardado = contratoRepository.save(contrato);
        historialRepository.save(new ContratoHistorial(
                guardado, estadoAnterior, EstadoContrato.ANULADO.name(), "ANULAR_CONTRATO",
                request.getMotivo(), username, request.getDocumentoReferencia()));

        auditoriaService.registrar("ANULAR_CONTRATO", "mercados_contratos", guardado.getId(),
                "EXITO", "Contrato anulado: " + request.getMotivo());

        return toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public ContratoResponse obtenerPorId(UUID id) {
        ContratoOcupacion contrato = contratoRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado con ID: " + id));
        return toResponse(contrato);
    }

    @Transactional(readOnly = true)
    public ContratoDetailResponse obtenerDetalle(UUID id) {
        ContratoOcupacion c = contratoRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado con ID: " + id));

        ContratoDetailResponse d = new ContratoDetailResponse();
        d.setId(c.getId());
        d.setCodigoContrato(c.getCodigoContrato());
        if (c.getAsignacion() != null) {
            d.setAsignacionId(c.getAsignacion().getId());
            d.setCodigoAsignacion(c.getAsignacion().getCodigoAsignacion());
        }
        if (c.getComerciante() != null) {
            d.setComercianteId(c.getComerciante().getId());
            d.setComercianteNombre(c.getComerciante().getNombreOTitular());
            d.setComercianteDocumento(c.getComerciante().getNumeroDocumentoTitular());
            d.setComercianteTipo(c.getComerciante().getTipoComerciante().name());
            d.setComercianteTelefono(c.getComerciante().getTelefonoContacto());
            d.setComercianteCorreo(c.getComerciante().getCorreoContacto());
            d.setComercianteDireccion(c.getComerciante().getDireccionContacto());
        }
        if (c.getPuesto() != null) {
            d.setPuestoId(c.getPuesto().getId());
            d.setPuestoCodigo(c.getPuesto().getCodigo());
            d.setPuestoNumero(c.getPuesto().getNumero());
            d.setPuestoSector(c.getPuesto().getSector() != null ? c.getPuesto().getSector().getNombre() : "-");
            d.setPuestoPabellon(c.getPuesto().getPabellon() != null ? c.getPuesto().getPabellon().getNombre() : "-");
            d.setPuestoTipo(c.getPuesto().getTipoPuesto() != null ? c.getPuesto().getTipoPuesto().getNombre() : "-");
            if (c.getPuesto().getMercado() != null) {
                d.setMercadoId(c.getPuesto().getMercado().getId());
                d.setMercadoNombre(c.getPuesto().getMercado().getNombre());
            }
        }
        if (c.getTipoContrato() != null) {
            d.setTipoContratoId(c.getTipoContrato().getId());
            d.setTipoContratoCodigo(c.getTipoContrato().getCodigo());
            d.setTipoContratoNombre(c.getTipoContrato().getNombre());
            d.setTipoContratoBaseNormativa(c.getTipoContrato().getBaseNormativa());
        } else {
            d.setTipoContratoCodigo(c.getTipoContratoCodigo());
            d.setTipoContratoNombre(c.getTipoContratoCodigo());
        }

        d.setFechaElaboracion(c.getFechaElaboracion());
        d.setFechaInicio(c.getFechaInicio());
        d.setFechaFin(c.getFechaFin());
        d.setEstado(c.getEstado());
        d.setEstadoNombre(c.getEstado().name());

        if (c.getFechaFin() != null) {
            d.setDiasRestantes(ChronoUnit.DAYS.between(LocalDate.now(), c.getFechaFin()));
        }

        d.setDocumentoId(c.getDocumento() != null ? c.getDocumento().getId() : null);
        d.setExpedienteId(c.getExpedienteId());
        if (c.getOficinaResponsable() != null) {
            d.setOficinaResponsableId(c.getOficinaResponsable().getId());
            d.setOficinaResponsableNombre(c.getOficinaResponsable().getNombre());
        }
        d.setResponsableElaboracion(c.getResponsableElaboracion());
        d.setResponsableAprobacion(c.getResponsableAprobacion());
        d.setFechaAprobacion(c.getFechaAprobacion());
        d.setFechaFormalizacion(c.getFechaFormalizacion());
        d.setCodigoSeguro(c.getCodigoSeguro());
        d.setHashDocumento(c.getHashDocumento());
        d.setMotivoObservacion(c.getMotivoObservacion());
        d.setMotivoSuspension(c.getMotivoSuspension());
        d.setMotivoTerminacion(c.getMotivoTerminacion());
        d.setObservaciones(c.getObservaciones());
        d.setCreatedAt(c.getCreatedAt());
        d.setUpdatedAt(c.getUpdatedAt());
        d.setCreatedBy(c.getCreatedBy());

        // Historial
        List<ContratoHistorial> histList = historialRepository.findByContratoIdOrderByFechaAsc(c.getId());
        d.setHistorial(histList.stream().map(h -> new ContratoHistorialResponse(
                h.getId(), h.getEstadoAnterior(), h.getEstadoNuevo(), h.getAccion(),
                h.getMotivo(), h.getUsuario(), h.getFecha(), h.getDocumentoReferencia()
        )).collect(Collectors.toList()));

        return d;
    }

    @Transactional(readOnly = true)
    public Page<ContratoResponse> buscarConFiltros(
            String termino, UUID mercadoId, EstadoContrato estado, String tipoContratoCodigo,
            LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable) {
        String term = (termino != null && !termino.trim().isEmpty()) ? termino.trim() : null;
        String tipo = (tipoContratoCodigo != null && !tipoContratoCodigo.trim().isEmpty()) ? tipoContratoCodigo.trim() : null;

        Page<ContratoOcupacion> page;
        if (term != null) {
            page = contratoRepository.buscarConFiltrosConTermino(
                    term, mercadoId, estado, tipo, fechaDesde, fechaHasta, pageable);
        } else {
            page = contratoRepository.buscarConFiltrosSinTermino(
                    mercadoId, estado, tipo, fechaDesde, fechaHasta, pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public byte[] generarPdf(UUID id) {
        ContratoOcupacion c = contratoRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado con ID: " + id));

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

            // Encabezado
            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontHeaderMuni);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(pHeader);

            Paragraph pSubHeader = new Paragraph("Provincia de Huamanga — Departamento de Ayacucho\nGerencia de Servicios Municipales y Desarrollo Económico\nSubgerencia de Comercialización y Mercados", fontSubMuni);
            pSubHeader.setAlignment(Element.ALIGN_CENTER);
            pSubHeader.setSpacingAfter(10);
            document.add(pSubHeader);

            Paragraph separator = new Paragraph("_____________________________________________________________________________\n", fontSubMuni);
            separator.setAlignment(Element.ALIGN_CENTER);
            separator.setSpacingAfter(10);
            document.add(separator);

            // Título
            Paragraph pTitulo = new Paragraph("CONTRATO DE OCUPACIÓN Y CONDUCCIÓN DE PUESTO COMERCIAL", fontTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);

            Paragraph pCodigo = new Paragraph("REGISTRO: " + c.getCodigoContrato(), fontSubtitulo);
            pCodigo.setAlignment(Element.ALIGN_CENTER);
            pCodigo.setSpacingAfter(15);
            document.add(pCodigo);

            // Tabla de Datos
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{35f, 65f});
            table.setSpacingBefore(5);
            table.setSpacingAfter(10);

            agregarFila(table, "CÓDIGO DE CONTRATO:", c.getCodigoContrato(), fontLabel, fontValue);
            agregarFila(table, "MODALIDAD CONTRACTUAL:", c.getTipoContrato() != null ? c.getTipoContrato().getNombre() : c.getTipoContratoCodigo(), fontLabel, fontValue);
            agregarFila(table, "ESTADO ADMINISTRATIVO:", c.getEstado().name(), fontLabel, fontValue);
            agregarFila(table, "MERCADO MUNICIPAL:", c.getPuesto().getMercado() != null ? c.getPuesto().getMercado().getNombre() : "-", fontLabel, fontValue);
            agregarFila(table, "PUESTO ASIGNADO:", c.getPuesto().getCodigo() + " (N° " + c.getPuesto().getNumero() + ")", fontLabel, fontValue);
            agregarFila(table, "COMERCIANTE TITULAR:", c.getComerciante().getNombreOTitular(), fontLabel, fontValue);
            agregarFila(table, "DOCUMENTO TITULAR:", c.getComerciante().getTipoDocumentoTitular() + " " + c.getComerciante().getNumeroDocumentoTitular(), fontLabel, fontValue);
            agregarFila(table, "FECHA DE INICIO:", c.getFechaInicio().toString(), fontLabel, fontValue);
            agregarFila(table, "FECHA DE FINALIZACIÓN:", c.getFechaFin() != null ? c.getFechaFin().toString() : "DETERMINADA SEGÚN REGLAMENTO", fontLabel, fontValue);
            if (c.getCodigoSeguro() != null) {
                agregarFila(table, "CÓDIGO DE SEGURIDAD (QR):", c.getCodigoSeguro(), fontLabel, fontValue);
            }
            if (c.getHashDocumento() != null) {
                agregarFila(table, "FIRMA DE INTEGRIDAD (SHA-256):", c.getHashDocumento(), fontLabel, fontValue);
            }
            document.add(table);

            // Cláusula / Disposiciones de Ley
            Paragraph pClausula = new Paragraph(
                    "El presente instrumento acredita las obligaciones y derechos del comerciante adjudicatario en el mercado municipal " +
                    "de acuerdo con la Ley Orgánica de Municipalidades N° 27972 y el marco normativo vigente de la Municipalidad Distrital de Quinua. " +
                    "La validez del documento está sujeta al cumplimiento estricto del reglamento de mercados y las normas de salubridad pública.",
                    fontDisclaimer);
            pClausula.setAlignment(Element.ALIGN_JUSTIFIED);
            pClausula.setSpacingAfter(15);
            document.add(pClausula);

            // Código QR para verificación digital
            if (c.getCodigoSeguro() != null) {
                String urlVerificacion = "http://localhost:8080/verificar/mercados/documento/" + c.getCodigoSeguro();
                byte[] qrBytes = qrCodeGenerator.generateQrCodePng(urlVerificacion, 110, 110);
                Image qrImage = Image.getInstance(qrBytes);
                qrImage.setAlignment(Element.ALIGN_CENTER);
                qrImage.scaleToFit(90, 90);
                document.add(qrImage);

                Paragraph pQrInfo = new Paragraph("Escanee el código QR para verificar la autenticidad y vigencia de este contrato.", fontDisclaimer);
                pQrInfo.setAlignment(Element.ALIGN_CENTER);
                document.add(pQrInfo);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Error al generar el documento PDF del contrato: " + e.getMessage());
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

    private void validarTransicion(ContratoOcupacion contrato, EstadoContrato nuevoEstado) {
        if (!contrato.getEstado().canTransitionTo(nuevoEstado)) {
            throw new BusinessException(String.format("Transición de estado no autorizada: de %s a %s.",
                    contrato.getEstado(), nuevoEstado));
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

    private ContratoResponse toResponse(ContratoOcupacion c) {
        Long diasRestantes = null;
        if (c.getFechaFin() != null) {
            diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), c.getFechaFin());
        }

        return ContratoResponse.builder()
                .id(c.getId())
                .codigoContrato(c.getCodigoContrato())
                .asignacionId(c.getAsignacion() != null ? c.getAsignacion().getId() : null)
                .codigoAsignacion(c.getAsignacion() != null ? c.getAsignacion().getCodigoAsignacion() : null)
                .comercianteId(c.getComerciante() != null ? c.getComerciante().getId() : null)
                .comercianteNombre(c.getComerciante() != null ? c.getComerciante().getNombreOTitular() : "-")
                .comercianteDocumento(c.getComerciante() != null ? c.getComerciante().getNumeroDocumentoTitular() : "-")
                .puestoId(c.getPuesto() != null ? c.getPuesto().getId() : null)
                .puestoCodigo(c.getPuesto() != null ? c.getPuesto().getCodigo() : "-")
                .puestoNumero(c.getPuesto() != null ? c.getPuesto().getNumero() : "-")
                .mercadoId(c.getPuesto() != null && c.getPuesto().getMercado() != null ? c.getPuesto().getMercado().getId() : null)
                .mercadoNombre(c.getPuesto() != null && c.getPuesto().getMercado() != null ? c.getPuesto().getMercado().getNombre() : "-")
                .tipoContratoCodigo(c.getTipoContratoCodigo())
                .tipoContratoNombre(c.getTipoContrato() != null ? c.getTipoContrato().getNombre() : c.getTipoContratoCodigo())
                .fechaElaboracion(c.getFechaElaboracion())
                .fechaInicio(c.getFechaInicio())
                .fechaFin(c.getFechaFin())
                .estado(c.getEstado())
                .estadoNombre(c.getEstado().name())
                .diasRestantes(diasRestantes)
                .codigoSeguro(c.getCodigoSeguro())
                .hashDocumento(c.getHashDocumento())
                .documentoId(c.getDocumento() != null ? c.getDocumento().getId() : null)
                .createdAt(c.getCreatedAt())
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
