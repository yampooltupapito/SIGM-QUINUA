package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DetalleNotificacionObligacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NotificacionTributaria;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio de generación de documentos oficiales PDF para Cédulas de Notificación
 * y Comunicaciones Tributarias de la Municipalidad Distrital de Quinua.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfNotificacionTributariaService {

    private final QrCodeGenerator qrCodeGenerator;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static class DocumentoPdfResult {
        private final byte[] pdfBytes;
        private final String hashSha256;

        public DocumentoPdfResult(byte[] pdfBytes, String hashSha256) {
            this.pdfBytes = pdfBytes;
            this.hashSha256 = hashSha256;
        }

        public byte[] getPdfBytes() {
            return pdfBytes;
        }

        public String getHashSha256() {
            return hashSha256;
        }
    }

    public DocumentoPdfResult generarDocumentoNotificacion(NotificacionTributaria notif, String urlVerificacion) {
        if (notif == null) {
            throw new BusinessException("La notificación tributaria es nula.");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPdfVersion(PdfWriter.PDF_VERSION_1_7);

            document.open();

            // Colores institucionales
            Color azulInstitucional = new Color(29, 78, 216); // #1d4ed8
            Color grisOscuro = new Color(30, 41, 59);        // #1e293b
            Color grisBorde = new Color(203, 213, 225);       // #cbd5e1
            Color grisFondo = new Color(248, 250, 252);       // #f8fafc
            Color grisSecundario = new Color(100, 116, 139);  // #64748b

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, azulInstitucional);
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, grisSecundario);
            Font fontNumeroDoc = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, azulInstitucional);
            Font fontSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, azulInstitucional);
            Font fontTexto = FontFactory.getFont(FontFactory.HELVETICA, 8, grisOscuro);
            Font fontTextoBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, grisOscuro);
            Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 7, grisSecundario);

            // 1. MEMBRETE OFICIAL
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{65f, 35f});

            PdfPCell cellIzq = new PdfPCell();
            cellIzq.setBorder(Rectangle.NO_BORDER);
            cellIzq.addElement(new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo));
            cellIzq.addElement(new Paragraph("SUBGERENCIA DE ADMINISTRACIÓN TRIBUTARIA Y RENTAS", fontSubtitulo));
            cellIzq.addElement(new Paragraph("Provincia de Huamanga - Región Ayacucho | Capital de la Libertad Americana", fontSmall));
            headerTable.addCell(cellIzq);

            PdfPCell cellDer = new PdfPCell();
            cellDer.setBorder(Rectangle.BOX);
            cellDer.setBorderColor(azulInstitucional);
            cellDer.setBorderWidth(1.2f);
            cellDer.setBackgroundColor(grisFondo);
            cellDer.setPadding(6);
            cellDer.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph pTipo = new Paragraph(notif.getTipoNotificacion().getNombre().toUpperCase(), fontSeccion);
            pTipo.setAlignment(Element.ALIGN_CENTER);
            Paragraph pNum = new Paragraph(notif.getCodigoNotificacion(), fontNumeroDoc);
            pNum.setAlignment(Element.ALIGN_CENTER);
            Paragraph pCat = new Paragraph("Categoría: " + notif.getTipoNotificacion().getCategoria().getNombre(), fontSmall);
            pCat.setAlignment(Element.ALIGN_CENTER);

            cellDer.addElement(pTipo);
            cellDer.addElement(pNum);
            cellDer.addElement(pCat);
            headerTable.addCell(cellDer);

            document.add(headerTable);
            document.add(new Paragraph(" "));

            // 2. DATOS DEL DESTINATARIO Y DOMICILIO
            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{22f, 28f, 22f, 28f});

            agregarCeldaHeader(infoTable, "I. DATOS DEL DESTINATARIO Y DOMICILIO DE NOTIFICACIÓN", azulInstitucional, 4);

            agregarCampo(infoTable, "Destinatario:", notif.getDestinatarioNombre(), fontTextoBold, fontTexto);
            agregarCampo(infoTable, "N° Documento:", notif.getDestinatarioNumeroDoc(), fontTextoBold, fontTexto);

            String contribInfo = notif.getContribuyente().getPersona() != null
                    ? notif.getContribuyente().getPersona().getNombreCompleto()
                    : notif.getContribuyente().getCodigoContribuyente();
            agregarCampo(infoTable, "Contribuyente:", contribInfo, fontTextoBold, fontTexto);
            agregarCampo(infoTable, "Cód. Contribuyente:", notif.getContribuyente().getCodigoContribuyente(), fontTextoBold, fontTexto);

            if (notif.getRepresentante() != null) {
                agregarCampo(infoTable, "Representante Legal:", notif.getRepresentante().getNombresApellidos(), fontTextoBold, fontTexto);
                agregarCampo(infoTable, "Doc. Representante:", notif.getRepresentante().getTipoDocumento() + " " + notif.getRepresentante().getNumeroDocumento(), fontTextoBold, fontTexto);
            }

            PdfPCell cDomEtq = new PdfPCell(new Phrase("Domicilio Notificación:", fontTextoBold));
            cDomEtq.setBackgroundColor(grisFondo);
            cDomEtq.setBorderColor(grisBorde);
            cDomEtq.setPadding(4);
            infoTable.addCell(cDomEtq);

            PdfPCell cDomVal = new PdfPCell(new Phrase(notif.getDomicilioNotificacion() +
                    (notif.getReferenciaUbicacion() != null ? " (Ref: " + notif.getReferenciaUbicacion() + ")" : ""), fontTexto));
            cDomVal.setColspan(3);
            cDomVal.setBorderColor(grisBorde);
            cDomVal.setPadding(4);
            infoTable.addCell(cDomVal);

            agregarCampo(infoTable, "Medio de Notificación:", notif.getMedioNotificacion().getNombre(), fontTextoBold, fontTexto);
            agregarCampo(infoTable, "Fecha de Emisión:", notif.getFechaCreacion().format(FORMATO_FECHA_HORA), fontTextoBold, fontTexto);

            document.add(infoTable);
            document.add(new Paragraph(" "));

            // 3. ACTO ADMINISTRATIVO O ASUNTO DE LA COMUNICACIÓN
            PdfPTable asuntoTable = new PdfPTable(2);
            asuntoTable.setWidthPercentage(100);
            asuntoTable.setWidths(new float[]{25f, 75f});

            agregarCeldaHeader(asuntoTable, "II. ACTO ADMINISTRATIVO O MATERIA NOTIFICADA", azulInstitucional, 2);

            if (notif.getDocumentoOrigen() != null) {
                agregarCampo(asuntoTable, "Documento de Origen:", notif.getDocumentoOrigen().getNombreOriginal() != null
                        ? notif.getDocumentoOrigen().getNombreOriginal() : "DOC-" + notif.getDocumentoOrigen().getId(), fontTextoBold, fontTexto);
            }
            if (notif.getObligacion() != null) {
                agregarCampo(asuntoTable, "Obligación Vinculada:", notif.getObligacion().getCodigoObligacion() +
                        " - " + notif.getObligacion().getConcepto().getNombre(), fontTextoBold, fontTexto);
            }
            if (notif.getConvenio() != null) {
                agregarCampo(asuntoTable, "Convenio de Fraccionamiento:", notif.getConvenio().getCodigoConvenio() +
                        " (Resolución: " + notif.getConvenio().getResolucionAprobacion() + ")", fontTextoBold, fontTexto);
            }

            PdfPCell cObsEtq = new PdfPCell(new Phrase("Contenido / Observaciones:", fontTextoBold));
            cObsEtq.setBackgroundColor(grisFondo);
            cObsEtq.setBorderColor(grisBorde);
            cObsEtq.setPadding(4);
            asuntoTable.addCell(cObsEtq);

            String obsTexto = notif.getObservaciones() != null && !notif.getObservaciones().isBlank()
                    ? notif.getObservaciones()
                    : "Por medio del presente acto se pone en conocimiento del destinatario la comunicación oficial de la administración tributaria municipal para los fines de ley correspondientes.";
            PdfPCell cObsVal = new PdfPCell(new Phrase(obsTexto, fontTexto));
            cObsVal.setBorderColor(grisBorde);
            cObsVal.setPadding(4);
            asuntoTable.addCell(cObsVal);

            document.add(asuntoTable);
            document.add(new Paragraph(" "));

            // 4. DETALLE DE OBLIGACIONES RELACIONADAS (SI EXISTIEREN)
            List<DetalleNotificacionObligacion> detalles = notif.getDetallesObligacion();
            if (detalles != null && !detalles.isEmpty()) {
                PdfPTable detTable = new PdfPTable(5);
                detTable.setWidthPercentage(100);
                detTable.setWidths(new float[]{25f, 25f, 16f, 16f, 18f});

                agregarCeldaHeader(detTable, "III. OBLIGACIONES TRIBUTARIAS COMPRENDIDAS", azulInstitucional, 5);

                agregarColumnaCabecera(detTable, "Código Obligación", fontTextoBold, grisFondo, grisBorde);
                agregarColumnaCabecera(detTable, "Concepto / Período", fontTextoBold, grisFondo, grisBorde);
                agregarColumnaCabecera(detTable, "Insoluto (S/)", fontTextoBold, grisFondo, grisBorde);
                agregarColumnaCabecera(detTable, "Interés (S/)", fontTextoBold, grisFondo, grisBorde);
                agregarColumnaCabecera(detTable, "Total (S/)", fontTextoBold, grisFondo, grisBorde);

                for (DetalleNotificacionObligacion d : detalles) {
                    agregarCeldaTexto(detTable, d.getObligacion().getCodigoObligacion(), fontTexto, grisBorde);
                    agregarCeldaTexto(detTable, d.getObligacion().getConcepto().getNombre() + " (" + d.getPeriodo() + ")", fontTexto, grisBorde);
                    agregarCeldaTexto(detTable, String.format("S/ %.2f", d.getMontoInsoluto()), fontTexto, grisBorde);
                    agregarCeldaTexto(detTable, String.format("S/ %.2f", d.getMontoInteres()), fontTexto, grisBorde);
                    agregarCeldaTexto(detTable, String.format("S/ %.2f", d.getMontoTotal()), fontTextoBold, grisBorde);
                }

                document.add(detTable);
                document.add(new Paragraph(" "));
            }

            // 5. RECUADRO DE CARGO DE RECEPCIÓN / DILIGENCIAMIENTO
            PdfPTable cargoTable = new PdfPTable(2);
            cargoTable.setWidthPercentage(100);
            cargoTable.setWidths(new float[]{50f, 50f});

            agregarCeldaHeader(cargoTable, "IV. DILIGENCIA DE ENTREGA Y CARGO DE RECEPCIÓN", azulInstitucional, 2);

            PdfPCell cCargoIzq = new PdfPCell();
            cCargoIzq.setBorderColor(grisBorde);
            cCargoIzq.setPadding(6);
            cCargoIzq.addElement(new Paragraph("DATOS DE LA RECEPCIÓN:", fontTextoBold));
            cCargoIzq.addElement(new Paragraph("Fecha y Hora: _____ / _____ / 202___   ____:____", fontTexto));
            cCargoIzq.addElement(new Paragraph("Nombre Receptor: ____________________________________________", fontTexto));
            cCargoIzq.addElement(new Paragraph("DNI / Doc. Id: _________________  Vínculo: __________________", fontTexto));
            cCargoIzq.addElement(new Paragraph("\n\n____________________________________", fontTextoBold));
            cCargoIzq.addElement(new Paragraph("Firma del Receptor o Destinatario", fontSmall));
            cargoTable.addCell(cCargoIzq);

            PdfPCell cCargoDer = new PdfPCell();
            cCargoDer.setBorderColor(grisBorde);
            cCargoDer.setPadding(6);
            cCargoDer.addElement(new Paragraph("DATOS DEL NOTIFICADOR AUTORIZADO:", fontTextoBold));
            cCargoDer.addElement(new Paragraph("Notificador: " + (notif.getUsuarioResponsable() != null ? notif.getUsuarioResponsable().getNombreCompleto() : "Personal Acreditado"), fontTexto));
            cCargoDer.addElement(new Paragraph("Estado Fáctico: [  ] Recibió Conforme    [  ] Se Negó a Firmar", fontTexto));
            cCargoDer.addElement(new Paragraph("                [  ] Predio Cerrado       [  ] Dirección No Existe", fontTexto));
            cCargoDer.addElement(new Paragraph("\n\n____________________________________", fontTextoBold));
            cCargoDer.addElement(new Paragraph("Firma y Sello del Notificador Institucional", fontSmall));
            cargoTable.addCell(cCargoDer);

            document.add(cargoTable);
            document.add(new Paragraph(" "));

            // 6. PIE DOCUMENTAL CON CÓDIGO QR Y HASH CRIPTOGRÁFICO
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{82f, 18f});

            PdfPCell cLegal = new PdfPCell();
            cLegal.setBorder(Rectangle.NO_BORDER);
            cLegal.addElement(new Paragraph("Verificación de Integridad y Autenticidad Documental:", fontTextoBold));
            cLegal.addElement(new Paragraph("Este documento cuenta con identificación criptográfica única generada por el SIGM-QUINUA.", fontSmall));
            cLegal.addElement(new Paragraph("Consulte la autenticidad en: " + urlVerificacion, fontSmall));
            cLegal.addElement(new Paragraph("Nota Legal: La validez legal y el cómputo de plazos procesales se rigen por los requisitos establecidos en el Texto Único Ordenado del Código Tributario (D.S. 133-2013-EF).", fontSmall));
            footerTable.addCell(cLegal);

            PdfPCell cQr = new PdfPCell();
            cQr.setBorder(Rectangle.NO_BORDER);
            cQr.setHorizontalAlignment(Element.ALIGN_RIGHT);
            try {
                byte[] qrBytes = qrCodeGenerator.generateQrCodePng(urlVerificacion, 90, 90);
                Image qrImg = Image.getInstance(qrBytes);
                qrImg.scaleToFit(80, 80);
                cQr.addElement(qrImg);
            } catch (Exception e) {
                log.warn("No se pudo generar QR en PDF para notificación: {}", notif.getCodigoNotificacion(), e);
            }
            footerTable.addCell(cQr);

            document.add(footerTable);

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            String hash = calcularSha256(pdfBytes);
            return new DocumentoPdfResult(pdfBytes, hash);
        } catch (Exception e) {
            log.error("Error al generar PDF de notificación tributaria: {}", notif.getCodigoNotificacion(), e);
            throw new BusinessException("Error al generar el documento PDF oficial de la notificación: " + e.getMessage());
        }
    }

    private void agregarCeldaHeader(PdfPTable table, String titulo, Color color, int colspan) {
        PdfPCell c = new PdfPCell(new Phrase(titulo, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
        c.setBackgroundColor(color);
        c.setColspan(colspan);
        c.setPadding(4);
        c.setBorder(Rectangle.NO_BORDER);
        table.addCell(c);
    }

    private void agregarCampo(PdfPTable table, String etq, String val, Font fontEtq, Font fontVal) {
        PdfPCell c1 = new PdfPCell(new Phrase(etq, fontEtq));
        c1.setBackgroundColor(new Color(248, 250, 252));
        c1.setBorderColor(new Color(203, 213, 225));
        c1.setPadding(4);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(val != null ? val : "-", fontVal));
        c2.setBorderColor(new Color(203, 213, 225));
        c2.setPadding(4);
        table.addCell(c2);
    }

    private void agregarColumnaCabecera(PdfPTable table, String texto, Font font, Color fondo, Color borde) {
        PdfPCell c = new PdfPCell(new Phrase(texto, font));
        c.setBackgroundColor(fondo);
        c.setBorderColor(borde);
        c.setPadding(4);
        table.addCell(c);
    }

    private void agregarCeldaTexto(PdfPTable table, String texto, Font font, Color borde) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "-", font));
        c.setBorderColor(borde);
        c.setPadding(4);
        table.addCell(c);
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
        } catch (Exception e) {
            return "HASH-SHA256-UNAVAILABLE";
        }
    }
}

