package pe.gob.municipalidad.sigm.modules.mesapartes.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.ConstanciaPresentacionResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.DocumentoAdjuntoResumenDto;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ExpedienteDocumento;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteDocumentoRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConstanciaPresentacionServiceImpl implements ConstanciaPresentacionService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final QrCodeGenerator qrCodeGenerator;
    private final ExpedienteDocumentoRepository expedienteDocumentoRepository;
    private final FormularioDinamicoService formularioDinamicoService;

    @Value("${sigm.app.base-url:http://localhost:8080}")
    private String defaultBaseUrl;

    public ConstanciaPresentacionServiceImpl(QrCodeGenerator qrCodeGenerator,
                                             ExpedienteDocumentoRepository expedienteDocumentoRepository,
                                             FormularioDinamicoService formularioDinamicoService) {
        this.qrCodeGenerator = qrCodeGenerator;
        this.expedienteDocumentoRepository = expedienteDocumentoRepository;
        this.formularioDinamicoService = formularioDinamicoService;
    }

    @Override
    @Transactional(readOnly = true)
    public ConstanciaPresentacionResponse obtenerDatosConstancia(Expediente expediente, String baseUrl) {
        String urlBase = baseUrl != null && !baseUrl.isBlank() ? baseUrl : defaultBaseUrl;
        String codParam = (expediente.getCodigoSeguimiento() != null && !expediente.getCodigoSeguimiento().isBlank())
                ? expediente.getCodigoSeguimiento()
                : (expediente.getNumeroExpediente() != null ? expediente.getNumeroExpediente() : expediente.getUuid().toString());
        String urlVerificacion = urlBase + "/mesa-partes/seguimiento?codigo=" + codParam;

        Persona sol = expediente.getSolicitante();
        String nombreCompleto = sol != null ? sol.getNombreCompleto() : "No registrado";
        String tipoNumDoc = sol != null ? (sol.getTipoDocumento() + ": " + sol.getNumeroDocumento()) : "";
        String correo = sol != null ? sol.getCorreo() : "";
        String telefono = sol != null ? sol.getTelefono() : "";
        String direccion = sol != null ? sol.getDireccion() : "";

        List<ExpedienteDocumento> expDocs = expedienteDocumentoRepository.findByExpedienteIdWithDocumento(expediente.getId());
        List<DocumentoAdjuntoResumenDto> docDtos = new ArrayList<>();
        for (ExpedienteDocumento ed : expDocs) {
            Documento doc = ed.getDocumento();
            if (doc != null) {
                docDtos.add(DocumentoAdjuntoResumenDto.builder()
                        .documentoUuid(doc.getIdentificadorPublico())
                        .nombreArchivo(doc.getNombreOriginal())
                        .tipoRelacion(ed.getTipoRelacion() != null ? ed.getTipoRelacion().name() : "")
                        .tamanoBytes(doc.getTamanoBytes())
                        .sha256(doc.getHashSha256())
                        .fechaAsociacion(ed.getFechaAsociacion() != null
                                ? ed.getFechaAsociacion().atZoneSameInstant(ZONA_LIMA).format(FORMATO_FECHA_HORA)
                                : "")
                        .build());
            }
        }

        Map<String, String> respuestas = formularioDinamicoService.obtenerMapaRespuestas(expediente.getId());

        String fechaPresStr = expediente.getFechaPresentacion() != null
                ? expediente.getFechaPresentacion().atZoneSameInstant(ZONA_LIMA).format(FORMATO_FECHA_HORA)
                : OffsetDateTime.now(ZONA_LIMA).format(FORMATO_FECHA_HORA);

        return ConstanciaPresentacionResponse.builder()
                .uuid(expediente.getUuid())
                .numeroExpediente(expediente.getNumeroExpediente())
                .fechaPresentacion(fechaPresStr)
                .tipoTramiteNombre(expediente.getTipoTramite() != null ? expediente.getTipoTramite().getNombre() : "")
                .asunto(expediente.getAsunto())
                .descripcion(expediente.getDescripcion())
                .solicitanteNombreCompleto(nombreCompleto)
                .solicitanteTipoNumeroDocumento(tipoNumDoc)
                .solicitanteCorreo(correo)
                .solicitanteTelefono(telefono)
                .solicitanteDireccion(direccion)
                .oficinaResponsableNombre(expediente.getOficinaResponsable() != null ? expediente.getOficinaResponsable().getNombre() : "")
                .totalDocumentos(docDtos.size())
                .documentos(docDtos)
                .camposDinamicos(respuestas)
                .estado(expediente.getEstadoActual().name())
                .urlVerificacion(urlVerificacion)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarConstanciaPdf(Expediente expediente, String baseUrl) {
        ConstanciaPresentacionResponse datos = obtenerDatosConstancia(expediente, baseUrl);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPdfVersion(PdfWriter.PDF_VERSION_1_7);

            document.open();

            // Tipografías
            Font fontTituloMuni = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(29, 78, 216));
            Font fontSubMuni = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(71, 85, 105));
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, new Color(16, 24, 39));
            Font fontNumExpediente = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(29, 78, 216));
            Font fontSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 41, 59));
            Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(15, 23, 42));
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(51, 65, 85));
            Font fontValor = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(15, 23, 42));
            Font fontCodigo = FontFactory.getFont(FontFactory.COURIER, 7, new Color(71, 85, 105));
            Font fontAvisoLegal = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(100, 116, 139));

            // Encabezado institucional
            Paragraph pMuni = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTituloMuni);
            pMuni.setAlignment(Element.ALIGN_CENTER);
            document.add(pMuni);

            Paragraph pSub = new Paragraph("Capital Histórica de la Batalla de Ayacucho | RUC: 20194451731 | Mesa de Partes Virtual", fontSubMuni);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(8);
            document.add(pSub);

            LineSeparator sep1 = new LineSeparator(1, 100, new Color(203, 213, 225), Element.ALIGN_CENTER, -2);
            document.add(sep1);

            // Título de la Constancia
            Paragraph pTit = new Paragraph("\nCONSTANCIA DE PRESENTACIÓN DE TRÁMITE", fontTitulo);
            pTit.setAlignment(Element.ALIGN_CENTER);
            document.add(pTit);

            String numExp = datos.getNumeroExpediente() != null ? datos.getNumeroExpediente() : "EN PROCESO DE REGISTRO";
            Paragraph pExp = new Paragraph("EXPEDIENTE N°: " + numExp, fontNumExpediente);
            pExp.setAlignment(Element.ALIGN_CENTER);
            pExp.setSpacingAfter(15);
            document.add(pExp);

            // 1. Datos Generales de la Solicitud
            Paragraph pSec1 = new Paragraph("1. DATOS GENERALES DEL EXPEDIENTE", fontSeccion);
            pSec1.setSpacingAfter(6);
            document.add(pSec1);

            PdfPTable tabGeneral = new PdfPTable(2);
            tabGeneral.setWidthPercentage(100);
            tabGeneral.setWidths(new float[]{30f, 70f});
            tabGeneral.setSpacingAfter(12);

            agregarFila(tabGeneral, "Número de Expediente:", numExp, fontLabel, fontValor);
            agregarFila(tabGeneral, "Fecha y Hora de Ingreso:", datos.getFechaPresentacion() + " (Hora Oficial Perú)", fontLabel, fontValor);
            agregarFila(tabGeneral, "Tipo de Trámite:", datos.getTipoTramiteNombre(), fontLabel, fontValor);
            agregarFila(tabGeneral, "Asunto:", datos.getAsunto(), fontLabel, fontValor);
            if (datos.getDescripcion() != null && !datos.getDescripcion().isBlank()) {
                agregarFila(tabGeneral, "Descripción / Petitorio:", datos.getDescripcion(), fontLabel, fontValor);
            }
            agregarFila(tabGeneral, "Unidad Orgánica Responsable:", datos.getOficinaResponsableNombre(), fontLabel, fontValor);
            agregarFila(tabGeneral, "Estado Actual:", datos.getEstado(), fontLabel, fontValor);
            document.add(tabGeneral);

            // 2. Datos del Administrado / Solicitante
            Paragraph pSec2 = new Paragraph("2. IDENTIFICACIÓN DEL SOLICITANTE", fontSeccion);
            pSec2.setSpacingAfter(6);
            document.add(pSec2);

            PdfPTable tabSol = new PdfPTable(2);
            tabSol.setWidthPercentage(100);
            tabSol.setWidths(new float[]{30f, 70f});
            tabSol.setSpacingAfter(12);

            agregarFila(tabSol, "Nombres y Apellidos / Razón:", datos.getSolicitanteNombreCompleto(), fontLabel, fontValor);
            agregarFila(tabSol, "Documento de Identidad:", datos.getSolicitanteTipoNumeroDocumento(), fontLabel, fontValor);
            if (datos.getSolicitanteCorreo() != null && !datos.getSolicitanteCorreo().isBlank()) {
                agregarFila(tabSol, "Correo Electrónico Notificación:", datos.getSolicitanteCorreo(), fontLabel, fontValor);
            }
            if (datos.getSolicitanteTelefono() != null && !datos.getSolicitanteTelefono().isBlank()) {
                agregarFila(tabSol, "Teléfono de Contacto:", datos.getSolicitanteTelefono(), fontLabel, fontValor);
            }
            if (datos.getSolicitanteDireccion() != null && !datos.getSolicitanteDireccion().isBlank()) {
                agregarFila(tabSol, "Dirección Registrada:", datos.getSolicitanteDireccion(), fontLabel, fontValor);
            }
            document.add(tabSol);

            // 3. Documentos y Anexos Presentados
            Paragraph pSec3 = new Paragraph("3. DOCUMENTOS ADJUNTOS (" + datos.getTotalDocumentos() + ")", fontSeccion);
            pSec3.setSpacingAfter(6);
            document.add(pSec3);

            if (datos.getDocumentos() != null && !datos.getDocumentos().isEmpty()) {
                PdfPTable tabDocs = new PdfPTable(4);
                tabDocs.setWidthPercentage(100);
                tabDocs.setWidths(new float[]{35f, 15f, 15f, 35f});
                tabDocs.setSpacingAfter(12);

                agregarCeldaHeader(tabDocs, "Nombre de Archivo", fontHeaderTabla);
                agregarCeldaHeader(tabDocs, "Tipo Relación", fontHeaderTabla);
                agregarCeldaHeader(tabDocs, "Tamaño", fontHeaderTabla);
                agregarCeldaHeader(tabDocs, "Huella Digital SHA-256", fontHeaderTabla);

                for (DocumentoAdjuntoResumenDto doc : datos.getDocumentos()) {
                    long kb = doc.getTamanoBytes() != null ? (doc.getTamanoBytes() / 1024) : 0;
                    String tamanoStr = kb > 1024 ? String.format(Locale.US, "%.1f MB", kb / 1024.0) : (kb + " KB");
                    String sha = doc.getSha256() != null && doc.getSha256().length() > 24
                            ? doc.getSha256().substring(0, 24) + "..."
                            : (doc.getSha256() != null ? doc.getSha256() : "-");

                    agregarCelda(tabDocs, doc.getNombreArchivo(), fontValor);
                    agregarCelda(tabDocs, doc.getTipoRelacion(), fontValor);
                    agregarCelda(tabDocs, tamanoStr, fontValor);
                    agregarCelda(tabDocs, sha, fontCodigo);
                }
                document.add(tabDocs);
            } else {
                Paragraph pSinDocs = new Paragraph("No se registraron documentos anexos.", fontValor);
                pSinDocs.setSpacingAfter(12);
                document.add(pSinDocs);
            }

            // 4. Datos del Formulario Dinámico (si existen)
            if (datos.getCamposDinamicos() != null && !datos.getCamposDinamicos().isEmpty()) {
                Paragraph pSec4 = new Paragraph("4. INFORMACIÓN ESPECÍFICA DEL TRÁMITE", fontSeccion);
                pSec4.setSpacingAfter(6);
                document.add(pSec4);

                PdfPTable tabCampos = new PdfPTable(2);
                tabCampos.setWidthPercentage(100);
                tabCampos.setWidths(new float[]{35f, 65f});
                tabCampos.setSpacingAfter(12);

                for (Map.Entry<String, String> entry : datos.getCamposDinamicos().entrySet()) {
                    agregarFila(tabCampos, entry.getKey() + ":", entry.getValue(), fontLabel, fontValor);
                }
                document.add(tabCampos);
            }

            // Pie de página con Código QR de Verificación y Aviso Legal
            LineSeparator sep2 = new LineSeparator(1, 100, new Color(203, 213, 225), Element.ALIGN_CENTER, -2);
            document.add(sep2);

            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{22f, 78f});
            footerTable.setSpacingBefore(10);

            byte[] qrBytes = qrCodeGenerator.generateQrCodePng(datos.getUrlVerificacion(), 110, 110);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.scaleToFit(75, 75);

            PdfPCell qrCell = new PdfPCell(qrImage);
            qrCell.setBorder(Rectangle.NO_BORDER);
            qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            footerTable.addCell(qrCell);

            PdfPCell legalCell = new PdfPCell();
            legalCell.setBorder(Rectangle.NO_BORDER);
            legalCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph pLegalHead = new Paragraph("CONSTANCIA DE EMISIÓN ELECTRÓNICA — SIGM-QUINUA", fontLabel);
            pLegalHead.setSpacingAfter(2);
            legalCell.addElement(pLegalHead);

            Paragraph pLegalBody = new Paragraph(
                    "AVISO LEGAL: La presente constancia acredita exclusivamente la recepción virtual y el registro informático de la solicitud en la fecha y hora consignadas. " +
                    "No constituye concesión del derecho solicitado ni prejuzga sobre la admisibilidad de fondo del petitorio. " +
                    "Conforme al TUO de la Ley N° 27444 del Procedimiento Administrativo General, la unidad orgánica responsable calificará el cumplimiento de requisitos de ley. " +
                    "Verifique la autenticidad e historial de este expediente escaneando el código QR o en: " + datos.getUrlVerificacion(),
                    fontAvisoLegal
            );
            pLegalBody.setLeading(9f);
            legalCell.addElement(pLegalBody);

            footerTable.addCell(legalCell);
            document.add(footerTable);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar constancia PDF para expediente {}", expediente.getId(), e);
            throw new BusinessException("No fue posible generar la constancia de presentación en formato PDF.");
        }
    }

    private void agregarFila(PdfPTable table, String label, String valor, Font fontLabel, Font fontValor) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, fontLabel));
        cLabel.setBackgroundColor(new Color(248, 250, 252));
        cLabel.setBorderColor(new Color(226, 232, 240));
        cLabel.setPadding(4);

        PdfPCell cValor = new PdfPCell(new Phrase(valor != null ? valor : "", fontValor));
        cValor.setBorderColor(new Color(226, 232, 240));
        cValor.setPadding(4);

        table.addCell(cLabel);
        table.addCell(cValor);
    }

    private void agregarCeldaHeader(PdfPTable table, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(new Color(241, 245, 249));
        cell.setBorderColor(new Color(203, 213, 225));
        cell.setPadding(4);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void agregarCelda(PdfPTable table, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto != null ? texto : "", font));
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setPadding(4);
        table.addCell(cell);
    }
}
