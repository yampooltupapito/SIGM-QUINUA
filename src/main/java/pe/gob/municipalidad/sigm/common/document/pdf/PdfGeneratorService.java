package pe.gob.municipalidad.sigm.common.document.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.GenerarPdfRequest;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio centralizado para la generación de documentos PDF institucionales (OpenPDF).
 * Genera Constancias, Comprobantes, Reportes y Oficios con código QR de verificación.
 */
@Slf4j
@Service
public class PdfGeneratorService {

    private final QrCodeGenerator qrCodeGenerator;

    public PdfGeneratorService(QrCodeGenerator qrCodeGenerator) {
        this.qrCodeGenerator = qrCodeGenerator;
    }

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy, HH:mm");

    public byte[] generarDocumentoPdf(GenerarPdfRequest request, String verificacionUrl) {
        if (request == null) {
            throw new BusinessException("La solicitud de generación de PDF no puede ser nula.");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPdfVersion(PdfWriter.PDF_VERSION_1_7);

            document.open();

            // Estilos tipográficos
            Font fontHeaderMuni = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(29, 78, 216));
            Font fontSubMuni = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(71, 85, 105));
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(15, 23, 42));
            Font fontCodigo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(30, 41, 59));
            Font fontFecha = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, new Color(100, 116, 139));
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(30, 41, 59));
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(30, 41, 59));
            Font fontFooter = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(100, 116, 139));

            // Encabezado institucional
            Paragraph muniHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontHeaderMuni);
            muniHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(muniHeader);

            Paragraph muniSub = new Paragraph("Capital de la Independencia Americana | Huamanga - Ayacucho | RUC: 20194451731", fontSubMuni);
            muniSub.setAlignment(Element.ALIGN_CENTER);
            muniSub.setSpacingAfter(10);
            document.add(muniSub);

            LineSeparator line = new LineSeparator(1, 100, new Color(203, 213, 225), Element.ALIGN_CENTER, -2);
            document.add(line);

            // Código documental y fecha
            String codigo = request.getCodigoDocumental() != null && !request.getCodigoDocumental().isBlank()
                    ? request.getCodigoDocumental()
                    : "DOC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Paragraph pCodigo = new Paragraph("\nCódigo Documental: " + codigo, fontCodigo);
            pCodigo.setAlignment(Element.ALIGN_RIGHT);
            document.add(pCodigo);

            String fechaStr = OffsetDateTime.now(ZONA_LIMA).format(FORMATO_FECHA);
            Paragraph pFecha = new Paragraph("Fecha de emisión: Quinua, " + fechaStr, fontFecha);
            pFecha.setAlignment(Element.ALIGN_RIGHT);
            pFecha.setSpacingAfter(20);
            document.add(pFecha);

            // Título del documento
            Paragraph pTitulo = new Paragraph(request.getTitulo().toUpperCase(), fontTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            pTitulo.setSpacingAfter(25);
            document.add(pTitulo);

            // Destinatario y Asunto si aplican
            if (request.getDestinatario() != null && !request.getDestinatario().isBlank()) {
                Paragraph pDest = new Paragraph();
                pDest.add(new Chunk("Destinatario: ", fontBold));
                pDest.add(new Chunk(request.getDestinatario(), fontNormal));
                pDest.setSpacingAfter(8);
                document.add(pDest);
            }

            if (request.getAsunto() != null && !request.getAsunto().isBlank()) {
                Paragraph pAsunto = new Paragraph();
                pAsunto.add(new Chunk("Asunto: ", fontBold));
                pAsunto.add(new Chunk(request.getAsunto(), fontNormal));
                pAsunto.setSpacingAfter(15);
                document.add(pAsunto);
            }

            // Cuerpo del documento
            Paragraph pCuerpo = new Paragraph(request.getCuerpo(), fontNormal);
            pCuerpo.setAlignment(Element.ALIGN_JUSTIFIED);
            pCuerpo.setLeading(18f);
            pCuerpo.setSpacingAfter(20);
            document.add(pCuerpo);

            // Metadatos adicionales si existen
            if (request.getMetadatosExtra() != null && !request.getMetadatosExtra().isEmpty()) {
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{30f, 70f});
                table.setSpacingBefore(10);
                table.setSpacingAfter(25);

                for (Map.Entry<String, String> entry : request.getMetadatosExtra().entrySet()) {
                    PdfPCell cellKey = new PdfPCell(new Phrase(entry.getKey(), fontBold));
                    cellKey.setBackgroundColor(new Color(241, 245, 249));
                    cellKey.setPadding(6);
                    cellKey.setBorderColor(new Color(203, 213, 225));
                    table.addCell(cellKey);

                    PdfPCell cellVal = new PdfPCell(new Phrase(entry.getValue(), fontNormal));
                    cellVal.setPadding(6);
                    cellVal.setBorderColor(new Color(203, 213, 225));
                    table.addCell(cellVal);
                }
                document.add(table);
            }

            // Pie de página con Código QR y validación
            document.add(new Paragraph("\n"));
            LineSeparator footerLine = new LineSeparator(1, 100, new Color(203, 213, 225), Element.ALIGN_CENTER, -2);
            document.add(footerLine);

            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{25f, 75f});
            footerTable.setSpacingBefore(10);

            // Generar código QR con la URL de verificación
            String urlFinal = verificacionUrl != null && !verificacionUrl.isBlank()
                    ? verificacionUrl
                    : "https://muniquinua.gob.pe/verificar/" + codigo;

            byte[] qrBytes = qrCodeGenerator.generateQrCodePng(urlFinal, 120, 120);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.scaleToFit(80, 80);

            PdfPCell qrCell = new PdfPCell(qrImage);
            qrCell.setBorder(Rectangle.NO_BORDER);
            qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            footerTable.addCell(qrCell);

            PdfPCell textCell = new PdfPCell();
            textCell.setBorder(Rectangle.NO_BORDER);
            textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph pVal = new Paragraph("CONSTANCIA DE EMISIÓN ELECTRÓNICA - SIGM-QUINUA", fontBold);
            pVal.setSpacingAfter(4);
            textCell.addElement(pVal);

            Paragraph pDesc = new Paragraph(
                    "Este documento electrónico ha sido generado por el Sistema Integral de Gestión Municipal de la Municipalidad Distrital de Quinua. " +
                    "La autenticidad e integridad del documento puede verificarse escaneando el código QR o ingresando al portal institucional:\n" +
                    urlFinal + "\n\n" +
                    "Aviso Legal: La huella criptográfica SHA-256 valida la integridad física del archivo. Este documento no sustituye una firma digital con certificado digital X.509 de autoría conforme a la Ley N° 27269.",
                    fontFooter
            );
            pDesc.setLeading(10f);
            textCell.addElement(pDesc);

            footerTable.addCell(textCell);
            document.add(footerTable);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF institucional", e);
            throw new BusinessException("Ocurrió un error al generar el archivo PDF institucional.");
        }
    }
}
