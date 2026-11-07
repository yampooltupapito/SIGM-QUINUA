package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvAdminDtos.ConstanciaRecepcionResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ExpedienteHistorial;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteHistorialRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ConstanciaRecepcionServiceImpl implements ConstanciaRecepcionService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final ExpedienteRepository expedienteRepository;
    private final ExpedienteHistorialRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final QrCodeGenerator qrCodeGenerator;

    @Value("${sigm.app.base-url:http://localhost:8080}")
    private String defaultBaseUrl;

    public ConstanciaRecepcionServiceImpl(ExpedienteRepository expedienteRepository,
                                          ExpedienteHistorialRepository historialRepository,
                                          UsuarioRepository usuarioRepository,
                                          QrCodeGenerator qrCodeGenerator) {
        this.expedienteRepository = expedienteRepository;
        this.historialRepository = historialRepository;
        this.usuarioRepository = usuarioRepository;
        this.qrCodeGenerator = qrCodeGenerator;
    }

    @Override
    @Transactional(readOnly = true)
    public ConstanciaRecepcionResponse obtenerDatosConstancia(Long expedienteId, String baseUrl, String username) {
        Usuario usuario = obtenerUsuario(username);
        Expediente expediente = expedienteRepository.findByIdWithDetalles(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado con ID: " + expedienteId));

        validarAccesoLectura(expediente, usuario);

        if (expediente.getEstadoActual() == EstadoExpediente.BORRADOR || expediente.getEstadoActual() == EstadoExpediente.PRESENTADO) {
            throw new BusinessException("El expediente aún no ha sido recibido formalmente por la Mesa de Partes.");
        }

        String urlBase = baseUrl != null && !baseUrl.isBlank() ? baseUrl : defaultBaseUrl;
        String codigoVerificacion = calcularCodigoVerificacion(expediente);
        String urlVerificacion = urlBase + "/verificar-documento?codigo=" + codigoVerificacion;

        Persona sol = expediente.getSolicitante();
        String solNombre = sol != null ? sol.getNombreCompleto() : "No registrado";
        String solDoc = sol != null ? (sol.getTipoDocumento() + ": " + sol.getNumeroDocumento()) : "";

        String oficina = expediente.getOficinaResponsable() != null ? expediente.getOficinaResponsable().getNombre() : "Mesa de Partes General";

        // Obtener operador que recibió
        String operadorReceptor = "Mesa de Partes Virtual";
        List<ExpedienteHistorial> histList = historialRepository.findByExpedienteIdOrderByFechaMovimientoAsc(expedienteId);
        for (ExpedienteHistorial h : histList) {
            if ("RECEPCION_EXPEDIENTE".equalsIgnoreCase(h.getAccion()) && h.getUsuarioResponsable() != null) {
                operadorReceptor = h.getUsuarioResponsable().getUsername();
                break;
            }
        }

        return ConstanciaRecepcionResponse.builder()
                .numeroExpediente(expediente.getNumeroExpediente())
                .expedienteUuid(expediente.getUuid())
                .tipoTramiteNombre(expediente.getTipoTramite() != null ? expediente.getTipoTramite().getNombre() : "")
                .asunto(expediente.getAsunto())
                .solicitanteNombreCompleto(solNombre)
                .solicitanteDocumento(solDoc)
                .oficinaReceptora(oficina)
                .operadorReceptor(operadorReceptor)
                .fechaPresentacion(expediente.getFechaPresentacion())
                .fechaRecepcion(expediente.getFechaRecepcion())
                .totalDocumentos(expediente.getDocumentos() != null ? expediente.getDocumentos().size() : 0)
                .codigoVerificacion(codigoVerificacion)
                .urlVerificacion(urlVerificacion)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarConstanciaRecepcionPdf(Long expedienteId, String baseUrl, String username) {
        ConstanciaRecepcionResponse datos = obtenerDatosConstancia(expedienteId, baseUrl, username);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            document.open();

            // Colores Institucionales
            Color azulPrincipal = new Color(29, 78, 216);    // #1d4ed8
            Color azulOscuro = new Color(16, 24, 39);        // #101827
            Color grisTexto = new Color(15, 23, 42);         // #0f172a
            Color grisBorde = new Color(226, 232, 240);      // #e2e8f0
            Color fondoTarjeta = new Color(248, 250, 252);   // #f8fafc

            // Fuentes
            Font fTituloInstitucion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, azulOscuro);
            Font fSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, azulPrincipal);
            Font fTituloConstancia = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, azulOscuro);
            Font fEtiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, azulOscuro);
            Font fValor = FontFactory.getFont(FontFactory.HELVETICA, 9, grisTexto);
            Font fLegal = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, new Color(100, 116, 139));

            // Encabezado
            Paragraph pMuni = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fTituloInstitucion);
            pMuni.setAlignment(Element.ALIGN_CENTER);
            document.add(pMuni);

            Paragraph pSub = new Paragraph("PROVINCIA DE HUAMANGA — DEPARTAMENTO DE AYACUCHO\nSISTEMA INTEGRAL DE GESTIÓN MUNICIPAL (SIGM-QUINUA)", fSubtitulo);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(10);
            document.add(pSub);

            LineSeparator sep = new LineSeparator(1.5f, 100, azulPrincipal, Element.ALIGN_CENTER, -2);
            document.add(sep);

            Paragraph pTitulo = new Paragraph("CONSTANCIA DE RECEPCIÓN ADMINISTRATIVA", fTituloConstancia);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            pTitulo.setSpacingBefore(12);
            pTitulo.setSpacingAfter(4);
            document.add(pTitulo);

            Paragraph pNumExp = new Paragraph("EXPEDIENTE N° " + datos.getNumeroExpediente(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, azulPrincipal));
            pNumExp.setAlignment(Element.ALIGN_CENTER);
            pNumExp.setSpacingAfter(15);
            document.add(pNumExp);

            // Tabla de Datos de Radicación
            PdfPTable tRadicacion = new PdfPTable(2);
            tRadicacion.setWidthPercentage(100);
            tRadicacion.setWidths(new float[]{1.8f, 3.2f});
            tRadicacion.setSpacingAfter(12);

            agregarFilaTabla(tRadicacion, "Fecha de Presentación:", formatearFecha(datos.getFechaPresentacion()), fEtiqueta, fValor, fondoTarjeta, grisBorde);
            agregarFilaTabla(tRadicacion, "Fecha de Recepción Formal:", formatearFecha(datos.getFechaRecepcion()), fEtiqueta, fValor, fondoTarjeta, grisBorde);
            agregarFilaTabla(tRadicacion, "Oficina Receptora:", datos.getOficinaReceptora(), fEtiqueta, fValor, fondoTarjeta, grisBorde);
            agregarFilaTabla(tRadicacion, "Operador Receptor:", datos.getOperadorReceptor(), fEtiqueta, fValor, fondoTarjeta, grisBorde);
            agregarFilaTabla(tRadicacion, "Trámite:", datos.getTipoTramiteNombre(), fEtiqueta, fValor, fondoTarjeta, grisBorde);
            agregarFilaTabla(tRadicacion, "Asunto:", datos.getAsunto(), fEtiqueta, fValor, fondoTarjeta, grisBorde);
            agregarFilaTabla(tRadicacion, "Solicitante:", datos.getSolicitanteNombreCompleto(), fEtiqueta, fValor, fondoTarjeta, grisBorde);
            agregarFilaTabla(tRadicacion, "Documento de Identidad:", datos.getSolicitanteDocumento(), fEtiqueta, fValor, fondoTarjeta, grisBorde);
            agregarFilaTabla(tRadicacion, "Documentos Recaudos:", datos.getTotalDocumentos() + " archivo(s) admitido(s)", fEtiqueta, fValor, fondoTarjeta, grisBorde);

            document.add(tRadicacion);

            // Advertencia Legal
            PdfPTable tNota = new PdfPTable(1);
            tNota.setWidthPercentage(100);
            tNota.setSpacingAfter(15);

            PdfPCell cellNota = new PdfPCell();
            cellNota.setBackgroundColor(new Color(254, 249, 195)); // #fef9c3
            cellNota.setBorderColor(new Color(234, 179, 8));       // #eab308
            cellNota.setPadding(8);

            Paragraph pNota = new Paragraph(
                    "NOTA LEGAL: La recepción formal del presente expediente acredita el ingreso y radicación de los documentos " +
                    "ante la Municipalidad Distrital de Quinua para inicio del procedimiento administrativo de conformidad con el TUO " +
                    "de la Ley N° 27444. La recepción de los actuados no constituye aprobación automática ni prejuzgamiento sobre la pretensión solicitada.",
                    FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(113, 63, 18))
            );
            cellNota.addElement(pNota);
            tNota.addCell(cellNota);
            document.add(tNota);

            // Código QR y Hash de Verificación
            PdfPTable tVerif = new PdfPTable(2);
            tVerif.setWidthPercentage(100);
            tVerif.setWidths(new float[]{1.2f, 3.8f});

            byte[] qrBytes = qrCodeGenerator.generateQrCodePng(datos.getUrlVerificacion(), 110, 110);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.setAlignment(Element.ALIGN_CENTER);

            PdfPCell cQr = new PdfPCell(qrImage);
            cQr.setBorder(Rectangle.NO_BORDER);
            cQr.setHorizontalAlignment(Element.ALIGN_CENTER);
            tVerif.addCell(cQr);

            PdfPCell cHash = new PdfPCell();
            cHash.setBorder(Rectangle.NO_BORDER);
            cHash.addElement(new Paragraph("Verificación de Autenticidad Digital:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, azulOscuro)));
            cHash.addElement(new Paragraph("Código Hash SHA-256: " + datos.getCodigoVerificacion(), FontFactory.getFont(FontFactory.COURIER, 7.5f, azulPrincipal)));
            cHash.addElement(new Paragraph("Enlace de Verificación Oficial: " + datos.getUrlVerificacion(), FontFactory.getFont(FontFactory.HELVETICA, 7.5f, grisTexto)));
            cHash.addElement(new Paragraph("Municipalidad Distrital de Quinua — Gestión Transparente y Moderna", fLegal));
            tVerif.addCell(cHash);

            document.add(tVerif);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar constancia PDF de recepción para expediente ID {}: {}", expedienteId, e.getMessage(), e);
            throw new BusinessException("No fue posible generar el documento PDF de la constancia de recepción.");
        }
    }

    private void agregarFilaTabla(PdfPTable table, String etiqueta, String valor, Font fEtiq, Font fVal, Color bg, Color border) {
        PdfPCell c1 = new PdfPCell(new Phrase(etiqueta, fEtiq));
        c1.setBackgroundColor(bg);
        c1.setBorderColor(border);
        c1.setPadding(5);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(valor != null ? valor : "", fVal));
        c2.setBorderColor(border);
        c2.setPadding(5);
        table.addCell(c2);
    }

    private String formatearFecha(OffsetDateTime fecha) {
        if (fecha == null) return "No registrada";
        return fecha.atZoneSameInstant(ZONA_LIMA).format(FORMATO_FECHA_HORA);
    }

    private String calcularCodigoVerificacion(Expediente e) {
        try {
            String raw = "RECEPCION-QUINUA-" + e.getNumeroExpediente() + "-" + e.getUuid() + "-" + e.getFechaRecepcion();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 24).toUpperCase();
        } catch (Exception ex) {
            return UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase();
        }
    }

    private Usuario obtenerUsuario(String username) {
        return usuarioRepository.findByUsernameWithRolesAndPersona(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private void validarAccesoLectura(Expediente expediente, Usuario usuario) {
        if (usuario.getRoles() != null && usuario.getRoles().stream()
                .allMatch(r -> r.getNombre().equalsIgnoreCase("ROLE_CIUDADANO"))) {
            if (expediente.getSolicitante() == null || usuario.getPersona() == null
                    || !expediente.getSolicitante().getId().equals(usuario.getPersona().getId())) {
                throw new AccessDeniedException("No tiene permisos para descargar la constancia de este expediente.");
            }
        }
    }
}
