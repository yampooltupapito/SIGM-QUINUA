package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.DocumentoResponse;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.SubirDocumentoRequest;
import pe.gob.municipalidad.sigm.common.document.entity.CategoriaDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.ClasificacionAcceso;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.document.service.DocumentoService;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Cargo;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.AtencionDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.DecisionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.PropuestaDecision;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoEmisionDocumento;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoPropuesta;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.DecisionExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.PropuestaDecisionRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.DecisionExpedienteService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.NumeracionDocumentalService;
import pe.gob.municipalidad.sigm.modules.mesapartes.workflow.ExpedienteWorkflowService;
import pe.gob.municipalidad.sigm.common.notification.dto.NotificationDtos.EnviarNotificacionRequest;
import pe.gob.municipalidad.sigm.common.notification.service.CentralNotificationService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DecisionExpedienteServiceImpl implements DecisionExpedienteService {

    private static final Logger log = LoggerFactory.getLogger(DecisionExpedienteServiceImpl.class);

    private final DecisionExpedienteRepository decisionRepository;
    private final ExpedienteRepository expedienteRepository;
    private final PropuestaDecisionRepository propuestaRepository;
    private final NumeracionDocumentalService numeracionService;
    private final ExpedienteWorkflowService workflowService;
    private final DocumentoRepository documentoRepository;
    private final DocumentoService documentoService;
    private final UsuarioRepository usuarioRepository;
    private final PersonaOficinaRepository personaOficinaRepository;
    private final CentralNotificationService notificationService;
    private final QrCodeGenerator qrCodeGenerator;

    @Value("${sigm.app.base-url:http://localhost:8080}")
    private String defaultBaseUrl;

    public DecisionExpedienteServiceImpl(
            DecisionExpedienteRepository decisionRepository,
            ExpedienteRepository expedienteRepository,
            PropuestaDecisionRepository propuestaRepository,
            NumeracionDocumentalService numeracionService,
            ExpedienteWorkflowService workflowService,
            DocumentoRepository documentoRepository,
            DocumentoService documentoService,
            UsuarioRepository usuarioRepository,
            PersonaOficinaRepository personaOficinaRepository,
            CentralNotificationService notificationService,
            QrCodeGenerator qrCodeGenerator) {
        this.decisionRepository = decisionRepository;
        this.expedienteRepository = expedienteRepository;
        this.propuestaRepository = propuestaRepository;
        this.numeracionService = numeracionService;
        this.workflowService = workflowService;
        this.documentoRepository = documentoRepository;
        this.documentoService = documentoService;
        this.usuarioRepository = usuarioRepository;
        this.personaOficinaRepository = personaOficinaRepository;
        this.notificationService = notificationService;
        this.qrCodeGenerator = qrCodeGenerator;
    }

    @Override
    @Transactional
    public DecisionResponse registrarDecision(Long expedienteId, RegistrarDecisionRequest request, String username) {
        Usuario autoridad = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        Oficina oficina = expediente.getOficinaResponsable();
        if (oficina == null) {
            throw new BusinessException("El expediente no tiene oficina responsable configurada.");
        }

        // VALIDACIÓN DE COMPETENCIA FUNCIONAL (Sección 15 de Prompt 16)
        validarCompetenciaFuncional(autoridad, oficina.getId());

        PropuestaDecision propuesta = null;
        if (request.getPropuestaId() != null) {
            propuesta = propuestaRepository.findById(request.getPropuestaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Propuesta no encontrada con ID: " + request.getPropuestaId()));
            if (propuesta.getEstado() != EstadoPropuesta.APROBADA_INTERNAMENTE) {
                throw new BusinessException("Solo es posible formalizar decisiones a partir de propuestas con estado APROBADA_INTERNAMENTE.");
            }
        }

        String cargoAutoridad = obtenerCargoFuncionario(autoridad, oficina.getId());

        String numeroDoc = request.getNumeroDocumento();
        if (numeroDoc == null || numeroDoc.isBlank()) {
            numeroDoc = numeracionService.generarNumeroDocumento(
                    request.getTipoDecision().name(),
                    oficina.getId(),
                    OffsetDateTime.now().getYear()
            );
        }

        DecisionExpediente decision = DecisionExpediente.builder()
                .expediente(expediente)
                .propuesta(propuesta)
                .tipoDecision(request.getTipoDecision())
                .resultado(request.getResultado())
                .fundamento(request.getFundamento().trim())
                .autoridadCompetente(autoridad)
                .oficinaEmisora(oficina)
                .cargoAutoridad(cargoAutoridad)
                .fechaDecision(OffsetDateTime.now())
                .numeroDocumento(numeroDoc)
                .estadoEmision(EstadoEmisionDocumento.BORRADOR)
                .createdBy(autoridad.getUsername())
                .build();

        DecisionExpediente guardada = decisionRepository.save(decision);
        log.info("Decisión administrativa registrada ID {} para expediente {}", guardada.getId(), expediente.getNumeroExpediente());

        return mapearAResponse(guardada);
    }

    @Override
    @Transactional
    public DecisionResponse generarDocumentoPdf(Long decisionId, String baseUrl, String username) {
        Usuario usuario = obtenerUsuario(username);
        DecisionExpediente decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decisión no encontrada con ID: " + decisionId));

        validarCompetenciaFuncional(usuario, decision.getOficinaEmisora().getId());

        byte[] pdfBytes = compilarDocumentoOficialPdf(decision, baseUrl != null ? baseUrl : defaultBaseUrl);

        String nombreArchivo = "DOC-" + decision.getTipoDecision() + "-" + decision.getId() + ".pdf";
        org.springframework.web.multipart.MultipartFile file = new org.springframework.web.multipart.MultipartFile() {
            @Override
            public String getName() { return "archivo"; }
            @Override
            public String getOriginalFilename() { return nombreArchivo; }
            @Override
            public String getContentType() { return "application/pdf"; }
            @Override
            public boolean isEmpty() { return pdfBytes.length == 0; }
            @Override
            public long getSize() { return pdfBytes.length; }
            @Override
            public byte[] getBytes() { return pdfBytes; }
            @Override
            public java.io.InputStream getInputStream() { return new java.io.ByteArrayInputStream(pdfBytes); }
            @Override
            public void transferTo(java.io.File dest) throws java.io.IOException {
                java.nio.file.Files.write(dest.toPath(), pdfBytes);
            }
        };

        SubirDocumentoRequest uploadRequest = SubirDocumentoRequest.builder()
                .categoria(CategoriaDocumento.RESOLUCION)
                .clasificacionAcceso(ClasificacionAcceso.PUBLICO)
                .descripcion("Documento oficial de resolución administrativa #" + decision.getNumeroDocumento())
                .oficinaId(decision.getOficinaEmisora().getId())
                .build();

        DocumentoResponse docResp = documentoService.subirDocumento(file, uploadRequest, username);
        Documento documento = documentoRepository.findById(docResp.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Error al registrar documento PDF emitido"));

        decision.setDocumento(documento);
        decision.setEstadoEmision(EstadoEmisionDocumento.PENDIENTE_FIRMA);
        decision.setUpdatedBy(usuario.getUsername());

        DecisionExpediente actualizada = decisionRepository.save(decision);
        return mapearAResponse(actualizada);
    }

    @Override
    @Transactional
    public DecisionResponse registrarFormalizacionEmision(Long decisionId, FormalizarDecisionRequest request, String username) {
        Usuario usuario = obtenerUsuario(username);
        DecisionExpediente decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decisión no encontrada con ID: " + decisionId));

        validarCompetenciaFuncional(usuario, decision.getOficinaEmisora().getId());

        if (decision.getDocumento() == null) {
            throw new BusinessException("Debe generar previamente el documento PDF oficial antes de registrar su emisión formal.");
        }

        decision.setNumeroDocumento(request.getNumeroDocumento().trim());
        decision.setEvidenciaFormalizacion(request.getEvidenciaFormalizacion().trim());
        decision.setEstadoEmision(EstadoEmisionDocumento.EMITIDO);
        decision.setFechaEmision(OffsetDateTime.now());
        decision.setUpdatedBy(usuario.getUsername());

        DecisionExpediente guardada = decisionRepository.save(decision);

        // Actualizar resultado en el expediente y transicionar en el workflow
        Expediente expediente = decision.getExpediente();
        expediente.setResultadoFinal(decision.getResultado().name());

        workflowService.transicionar(
                expediente,
                EstadoExpediente.RESUELTO,
                "EMISION_RESOLUCION_FINAL",
                "Se formalizó y emitió el documento oficial: " + decision.getNumeroDocumento() + " con resultado: " + decision.getResultado(),
                usuario,
                decision.getOficinaEmisora()
        );

        // Notificar al administrado
        try {
            if (expediente.getSolicitante() != null) {
                usuarioRepository.findByPersonaId(expediente.getSolicitante().getId()).ifPresent(usuarioSolicitante -> {
                    notificationService.enviarNotificacion(
                            EnviarNotificacionRequest.builder()
                                    .usuarioId(usuarioSolicitante.getId())
                                    .titulo("Expediente Resuelto: " + expediente.getNumeroExpediente())
                                    .mensaje("Se ha emitido la decisión oficial para su expediente con número: " + decision.getNumeroDocumento())
                                    .tipo("DOCUMENTO")
                                    .referenciaId(expediente.getNumeroExpediente())
                                    .urlDestino("/mesa-partes/mis-solicitudes")
                                    .enviarCorreo(true)
                                    .build(),
                            usuario.getUsername()
                    );
                });
            }
        } catch (Exception e) {
            log.warn("No se pudo notificar al administrado: {}", e.getMessage());
        }

        return mapearAResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DecisionResponse> listarDecisionesPorExpediente(Long expedienteId, String username) {
        return decisionRepository.findByExpedienteIdOrderByFechaDecisionDesc(expedienteId)
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DecisionResponse obtenerPorId(Long decisionId, String username) {
        DecisionExpediente decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decisión no encontrada con ID: " + decisionId));
        return mapearAResponse(decision);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] descargarDocumentoDecisionPdf(Long decisionId, String username) {
        DecisionExpediente decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decisión no encontrada con ID: " + decisionId));

        if (decision.getDocumento() == null) {
            return compilarDocumentoOficialPdf(decision, defaultBaseUrl);
        }

        try (var in = documentoService.descargarDocumento(decision.getDocumento().getId(), null, username).getInputStream()) {
            return in.readAllBytes();
        } catch (Exception e) {
            log.error("Error al descargar documento PDF de decisión: {}", e.getMessage());
            return compilarDocumentoOficialPdf(decision, defaultBaseUrl);
        }
    }

    private byte[] compilarDocumentoOficialPdf(DecisionExpediente d, String baseUrl) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // Encabezado institucional
            Font fontTituloInst = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(15, 23, 42));
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(71, 85, 105));
            Font fontDocNumero = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(29, 78, 216));
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font fontValor = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font fontCuerpo = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(30, 41, 59));

            Paragraph pInst = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTituloInst);
            pInst.setAlignment(Element.ALIGN_CENTER);
            document.add(pInst);

            Paragraph pDep = new Paragraph("PROVINCIA DE HUAMANGA - AYACUCHO\n" + d.getOficinaEmisora().getNombre().toUpperCase(), fontSubtitulo);
            pDep.setAlignment(Element.ALIGN_CENTER);
            document.add(pDep);

            document.add(new Paragraph(" "));
            LineSeparator sep = new LineSeparator(1.5f, 100, new Color(203, 213, 225), Element.ALIGN_CENTER, -2);
            document.add(sep);
            document.add(new Paragraph(" "));

            Paragraph pNumero = new Paragraph(d.getTipoDecision() + " N° " + d.getNumeroDocumento(), fontDocNumero);
            pNumero.setAlignment(Element.ALIGN_CENTER);
            document.add(pNumero);

            document.add(new Paragraph(" "));

            // Metadatos
            PdfPTable tablaMeta = new PdfPTable(2);
            tablaMeta.setWidthPercentage(100);
            tablaMeta.setWidths(new float[]{30, 70});

            agregarFila(tablaMeta, "EXPEDIENTE N°:", d.getExpediente().getNumeroExpediente(), fontLabel, fontValor);
            agregarFila(tablaMeta, "FECHA DE DECISIÓN:", d.getFechaDecision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), fontLabel, fontValor);
            agregarFila(tablaMeta, "ADMINISTRADO(A):", d.getExpediente().getSolicitante().getNombreCompleto(), fontLabel, fontValor);
            agregarFila(tablaMeta, "ASUNTO:", d.getExpediente().getAsunto(), fontLabel, fontValor);
            agregarFila(tablaMeta, "RESULTADO:", d.getResultado().name(), fontLabel, fontValor);
            agregarFila(tablaMeta, "AUTORIDAD EMISORA:", d.getAutoridadCompetente().getNombreCompleto() + " (" + (d.getCargoAutoridad() != null ? d.getCargoAutoridad() : "Autoridad Municipal") + ")", fontLabel, fontValor);

            document.add(tablaMeta);
            document.add(new Paragraph(" "));

            // Fundamento y Parte Resolutiva
            Paragraph pFundamentoTitulo = new Paragraph("I. PARTE CONSIDERATIVA Y FUNDAMENTACIÓN LEGAL:", fontLabel);
            document.add(pFundamentoTitulo);

            Paragraph pFundamento = new Paragraph(d.getFundamento(), fontCuerpo);
            pFundamento.setAlignment(Element.ALIGN_JUSTIFIED);
            pFundamento.setSpacingBefore(5);
            pFundamento.setSpacingAfter(15);
            document.add(pFundamento);

            Paragraph pResolutivaTitulo = new Paragraph("II. PARTE DISPOSITIVA O RESOLUTIVA:", fontLabel);
            document.add(pResolutivaTitulo);

            String resolutivaTexto = "SE RESUELVE declarar " + d.getResultado() + " la solicitud formulada en el marco del expediente " +
                    d.getExpediente().getNumeroExpediente() + ", disponiéndose su debida notificación al administrado conforme a la Ley N° 27444.";
            Paragraph pResolutiva = new Paragraph(resolutivaTexto, fontCuerpo);
            pResolutiva.setAlignment(Element.ALIGN_JUSTIFIED);
            pResolutiva.setSpacingBefore(5);
            pResolutiva.setSpacingAfter(20);
            document.add(pResolutiva);

            // Pie y verificación QR
            PdfPTable pieTabla = new PdfPTable(2);
            pieTabla.setWidthPercentage(100);
            pieTabla.setWidths(new float[]{70, 30});

            PdfPCell cellFirma = new PdfPCell();
            cellFirma.setBorder(Rectangle.NO_BORDER);
            Paragraph pFirma = new Paragraph("\n\n_____________________________________\n" +
                    d.getAutoridadCompetente().getNombreCompleto() + "\n" +
                    (d.getCargoAutoridad() != null ? d.getCargoAutoridad() : "Funcionario Competente") + "\n" +
                    "Municipalidad Distrital de Quinua", fontSubtitulo);
            cellFirma.addElement(pFirma);
            pieTabla.addCell(cellFirma);

            String urlVerificacion = baseUrl + "/consulta/expedientes/" + d.getExpediente().getNumeroExpediente();
            byte[] qrBytes = qrCodeGenerator.generateQrCodePng(urlVerificacion, 120, 120);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.setAlignment(Element.ALIGN_RIGHT);

            PdfPCell cellQr = new PdfPCell(qrImage);
            cellQr.setBorder(Rectangle.NO_BORDER);
            cellQr.setHorizontalAlignment(Element.ALIGN_RIGHT);
            pieTabla.addCell(cellQr);

            document.add(pieTabla);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al compilar documento oficial PDF: {}", e.getMessage(), e);
            throw new BusinessException("No fue posible compilar el documento oficial en formato PDF: " + e.getMessage());
        }
    }

    private void agregarFila(PdfPTable table, String label, String valor, Font fLabel, Font fValor) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, fLabel));
        cLabel.setBackgroundColor(new Color(248, 250, 252));
        cLabel.setPadding(5);
        cLabel.setBorderColor(new Color(226, 232, 240));

        PdfPCell cValor = new PdfPCell(new Phrase(valor != null ? valor : "", fValor));
        cValor.setPadding(5);
        cValor.setBorderColor(new Color(226, 232, 240));

        table.addCell(cLabel);
        table.addCell(cValor);
    }

    private DecisionResponse mapearAResponse(DecisionExpediente d) {
        return DecisionResponse.builder()
                .id(d.getId())
                .expedienteId(d.getExpediente().getId())
                .numeroExpediente(d.getExpediente().getNumeroExpediente())
                .propuestaId(d.getPropuesta() != null ? d.getPropuesta().getId() : null)
                .tipoDecision(d.getTipoDecision())
                .resultado(d.getResultado())
                .fundamento(d.getFundamento())
                .autoridadCompetenteId(d.getAutoridadCompetente().getId())
                .autoridadCompetenteNombre(d.getAutoridadCompetente().getNombreCompleto())
                .cargoAutoridad(d.getCargoAutoridad())
                .oficinaEmisoraId(d.getOficinaEmisora().getId())
                .oficinaEmisoraNombre(d.getOficinaEmisora().getNombre())
                .fechaDecision(d.getFechaDecision())
                .numeroDocumento(d.getNumeroDocumento())
                .documentoId(d.getDocumento() != null ? d.getDocumento().getId() : null)
                .estadoEmision(d.getEstadoEmision())
                .evidenciaFormalizacion(d.getEvidenciaFormalizacion())
                .fechaEmision(d.getFechaEmision())
                .version(d.getVersion())
                .build();
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private void validarCompetenciaFuncional(Usuario usuario, Long oficinaId) {
        boolean esSuperAdmin = usuario.getRoles().stream().anyMatch(r -> r.getCodigo().equals("ROLE_SUPERADMIN") || r.getCodigo().equals("ROLE_ADMIN"));
        if (esSuperAdmin) return;

        boolean tieneCargoAutorizado = personaOficinaRepository.findByPersonaIdAndEstado(usuario.getPersona().getId(), "ACTIVO")
                .stream()
                .anyMatch(po -> po.getOficina().getId().equals(oficinaId));

        if (!tieneCargoAutorizado) {
            throw new AccessDeniedException("El usuario autenticado no posee investidura ni competencia funcional en la oficina emisora de la decisión.");
        }
    }

    private String obtenerCargoFuncionario(Usuario usuario, Long oficinaId) {
        return personaOficinaRepository.findByPersonaIdAndEstado(usuario.getPersona().getId(), "ACTIVO")
                .stream()
                .filter(po -> po.getOficina().getId().equals(oficinaId))
                .findFirst()
                .map(po -> po.getCargo() != null ? po.getCargo().getNombre() : "Titular de Dependencia")
                .orElse("Funcionario Competente");
    }
}
