package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.AplicacionPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PagoTributario;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Motor de generación PDF oficial para Recibos de Caja y Constancias Tributarias.
 * Cumple con formato institucional A4, tabla de imputaciones, QR de verificación y hash SHA-256.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfDocumentoTributarioService {

    private final QrCodeGenerator qrCodeGenerator;

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA_LARGA = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy, HH:mm");

    public byte[] generarPdfDocumento(DocumentoPagoTributario doc, List<AplicacionPagoTributario> aplicaciones, String urlVerificacion) {
        if (doc == null || doc.getPagoTributario() == null) {
            throw new BusinessException("Datos insuficientes para generar el PDF del documento tributario");
        }

        PagoTributario pago = doc.getPagoTributario();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPdfVersion(PdfWriter.PDF_VERSION_1_7);

            document.open();

            // Paleta de colores institucional (#101827, #1d4ed8, #202c3d, #334155, #64748b)
            Color azulInstitucional = new Color(29, 78, 216);
            Color grisTexto = new Color(30, 41, 59);
            Color grisBorde = new Color(203, 213, 225);
            Color grisFondoCabecera = new Color(241, 245, 249);
            Color grisSecundario = new Color(100, 116, 139);

            // Tipografías
            Font fontMuniTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, azulInstitucional);
            Font fontMuniSub = FontFactory.getFont(FontFactory.HELVETICA, 8, grisSecundario);
            Font fontDocTipo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, azulInstitucional);
            Font fontDocNumero = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(15, 23, 42));
            Font fontSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, azulInstitucional);
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, grisTexto);
            Font fontValor = FontFactory.getFont(FontFactory.HELVETICA, 8, grisTexto);
            Font fontTableHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, grisTexto);
            Font fontTableBody = FontFactory.getFont(FontFactory.HELVETICA, 8, grisTexto);
            Font fontTotalLetras = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, grisSecundario);
            Font fontTotalNumero = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, azulInstitucional);
            Font fontDisclaimer = FontFactory.getFont(FontFactory.HELVETICA, 7, grisSecundario);
            Font fontHash = FontFactory.getFont(FontFactory.COURIER, 7, grisSecundario);

            // 1. ENCABEZADO: Logotipo / Nombre Institucional y Recuadro Documental
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{62f, 38f});
            headerTable.setSpacingAfter(10);

            // Columna Izquierda: Identidad Municipal
            PdfPCell cellMuni = new PdfPCell();
            cellMuni.setBorder(Rectangle.NO_BORDER);
            Paragraph pMuni = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontMuniTitulo);
            pMuni.setSpacingAfter(2);
            Paragraph pSub1 = new Paragraph("Capital de la Independencia Americana | Huamanga - Ayacucho", fontMuniSub);
            Paragraph pSub2 = new Paragraph("RUC: 20194451731 | Subgerencia de Administración Tributaria y Rentas", fontMuniSub);
            cellMuni.addElement(pMuni);
            cellMuni.addElement(pSub1);
            cellMuni.addElement(pSub2);
            headerTable.addCell(cellMuni);

            // Columna Derecha: Recuadro del Documento
            PdfPCell cellRecuadro = new PdfPCell();
            cellRecuadro.setBorder(Rectangle.BOX);
            cellRecuadro.setBorderColor(azulInstitucional);
            cellRecuadro.setBorderWidth(1.5f);
            cellRecuadro.setPadding(8);
            cellRecuadro.setBackgroundColor(new Color(248, 250, 252));

            Paragraph pTipo = new Paragraph(doc.getTipoDocumento().getNombre().toUpperCase(), fontDocTipo);
            pTipo.setAlignment(Element.ALIGN_CENTER);
            Paragraph pNum = new Paragraph(doc.getCodigoDocumento(), fontDocNumero);
            pNum.setAlignment(Element.ALIGN_CENTER);
            pNum.setSpacingBefore(4);

            cellRecuadro.addElement(pTipo);
            cellRecuadro.addElement(pNum);
            headerTable.addCell(cellRecuadro);

            document.add(headerTable);

            // Separador
            document.add(new LineSeparator(1, 100, grisBorde, Element.ALIGN_CENTER, -2));

            // 2. DATOS DEL CONTRIBUYENTE Y DE LA OPERACIÓN
            PdfPTable datosTable = new PdfPTable(4);
            datosTable.setWidthPercentage(100);
            datosTable.setWidths(new float[]{18f, 32f, 18f, 32f});
            datosTable.setSpacingBefore(8);
            datosTable.setSpacingAfter(10);

            // Fila 1: Contribuyente y Código
            agregarCelda(datosTable, "Contribuyente:", fontLabel, grisFondoCabecera);
            agregarCelda(datosTable, doc.getContribuyente().getNombreRazonSocial(), fontValor, Color.WHITE);
            agregarCelda(datosTable, "Código Contrib.:", fontLabel, grisFondoCabecera);
            agregarCelda(datosTable, doc.getContribuyente().getCodigoContribuyente(), fontValor, Color.WHITE);

            // Fila 2: Documento y Fecha
            String tipoNumDoc = (doc.getContribuyente().getTipoDocumento() != null ? doc.getContribuyente().getTipoDocumento() : "DOC")
                    + ": " + (doc.getContribuyente().getNumeroDocumento() != null ? doc.getContribuyente().getNumeroDocumento() : "-");
            agregarCelda(datosTable, "Documento Ident.:", fontLabel, grisFondoCabecera);
            agregarCelda(datosTable, tipoNumDoc, fontValor, Color.WHITE);
            agregarCelda(datosTable, "Fecha Emisión:", fontLabel, grisFondoCabecera);
            agregarCelda(datosTable, doc.getFechaEmision().atZoneSameInstant(ZONA_LIMA).format(FORMATO_FECHA_HORA), fontValor, Color.WHITE);

            // Fila 3: Operación de Caja y Medio de Pago
            agregarCelda(datosTable, "Código Operación:", fontLabel, grisFondoCabecera);
            agregarCelda(datosTable, pago.getCodigoPago(), fontValor, Color.WHITE);
            agregarCelda(datosTable, "Medio de Pago:", fontLabel, grisFondoCabecera);
            String medio = pago.getMedioPago() != null ? pago.getMedioPago().name().replace("_", " ") : "EFECTIVO";
            if (pago.getReferenciaExterna() != null && !pago.getReferenciaExterna().isBlank()) {
                medio += " (Ref: " + pago.getReferenciaExterna() + ")";
            }
            agregarCelda(datosTable, medio, fontValor, Color.WHITE);

            // Fila 4: Cajero y Estado
            agregarCelda(datosTable, "Cajero Receptor:", fontLabel, grisFondoCabecera);
            String cajero = doc.getUsuarioEmisor() != null ? doc.getUsuarioEmisor().getNombreCompleto() : "Ventanilla de Rentas";
            agregarCelda(datosTable, cajero, fontValor, Color.WHITE);
            agregarCelda(datosTable, "Estado Documento:", fontLabel, grisFondoCabecera);
            agregarCelda(datosTable, doc.getEstado().name(), fontValor, Color.WHITE);

            document.add(datosTable);

            // 3. TABLA DE CONCEPTOS TRIBUTARIOS E IMPUTACIONES
            Paragraph pSecConceptos = new Paragraph("DETALLE DE CONCEPTOS TRIBUTARIOS APLICADOS", fontSeccion);
            pSecConceptos.setSpacingAfter(4);
            document.add(pSecConceptos);

            PdfPTable tableDetalle = new PdfPTable(6);
            tableDetalle.setWidthPercentage(100);
            tableDetalle.setWidths(new float[]{6f, 36f, 14f, 14f, 14f, 16f});
            tableDetalle.setSpacingAfter(8);

            // Encabezados de tabla
            agregarCeldaHeader(tableDetalle, "N°", fontTableHeader, grisFondoCabecera, Element.ALIGN_CENTER);
            agregarCeldaHeader(tableDetalle, "Concepto Tributario", fontTableHeader, grisFondoCabecera, Element.ALIGN_LEFT);
            agregarCeldaHeader(tableDetalle, "Período", fontTableHeader, grisFondoCabecera, Element.ALIGN_CENTER);
            agregarCeldaHeader(tableDetalle, "Cuota", fontTableHeader, grisFondoCabecera, Element.ALIGN_CENTER);
            agregarCeldaHeader(tableDetalle, "Ref. Predio", fontTableHeader, grisFondoCabecera, Element.ALIGN_CENTER);
            agregarCeldaHeader(tableDetalle, "Importe (S/)", fontTableHeader, grisFondoCabecera, Element.ALIGN_RIGHT);

            int item = 1;
            BigDecimal sumaAplicada = BigDecimal.ZERO;

            if (aplicaciones != null && !aplicaciones.isEmpty()) {
                for (AplicacionPagoTributario apl : aplicaciones) {
                    var obl = apl.getObligacion();
                    String conceptoNombre = obl != null && obl.getConcepto() != null ? obl.getConcepto().getNombre() : "Tributo Municipal";
                    String periodo = obl != null && obl.getPeriodo() != null ? String.valueOf(obl.getPeriodo().getAnio()) : "-";
                    String cuotaStr = obl != null && obl.getCuotaNumero() != null ? "Cuota " + obl.getCuotaNumero() : "Total";
                    String predio = obl != null && obl.getPredio() != null ? obl.getPredio().getCodigoPredio() : "-";
                    BigDecimal monto = apl.getImporteAplicado() != null ? apl.getImporteAplicado() : BigDecimal.ZERO;
                    sumaAplicada = sumaAplicada.add(monto);

                    agregarCelda(tableDetalle, String.valueOf(item++), fontTableBody, Color.WHITE, Element.ALIGN_CENTER);
                    agregarCelda(tableDetalle, conceptoNombre, fontTableBody, Color.WHITE, Element.ALIGN_LEFT);
                    agregarCelda(tableDetalle, periodo, fontTableBody, Color.WHITE, Element.ALIGN_CENTER);
                    agregarCelda(tableDetalle, cuotaStr, fontTableBody, Color.WHITE, Element.ALIGN_CENTER);
                    agregarCelda(tableDetalle, predio, fontTableBody, Color.WHITE, Element.ALIGN_CENTER);
                    agregarCelda(tableDetalle, String.format("%.2f", monto), fontTableBody, Color.WHITE, Element.ALIGN_RIGHT);
                }
            } else {
                agregarCelda(tableDetalle, "1", fontTableBody, Color.WHITE, Element.ALIGN_CENTER);
                agregarCelda(tableDetalle, "Pago Tributario General", fontTableBody, Color.WHITE, Element.ALIGN_LEFT);
                agregarCelda(tableDetalle, "-", fontTableBody, Color.WHITE, Element.ALIGN_CENTER);
                agregarCelda(tableDetalle, "-", fontTableBody, Color.WHITE, Element.ALIGN_CENTER);
                agregarCelda(tableDetalle, "-", fontTableBody, Color.WHITE, Element.ALIGN_CENTER);
                agregarCelda(tableDetalle, String.format("%.2f", doc.getImporteTotal()), fontTableBody, Color.WHITE, Element.ALIGN_RIGHT);
                sumaAplicada = doc.getImporteTotal();
            }

            document.add(tableDetalle);

            // 4. TOTAL GENERAL Y MONTO EN LETRAS
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setWidths(new float[]{65f, 35f});
            totalTable.setSpacingAfter(12);

            PdfPCell cellLetras = new PdfPCell();
            cellLetras.setBorder(Rectangle.BOX);
            cellLetras.setBorderColor(grisBorde);
            cellLetras.setPadding(6);
            cellLetras.setBackgroundColor(new Color(248, 250, 252));
            Paragraph pLetras = new Paragraph("SON: " + convertirMontoALetras(doc.getImporteTotal()), fontTotalLetras);
            cellLetras.addElement(pLetras);
            totalTable.addCell(cellLetras);

            PdfPCell cellMonto = new PdfPCell();
            cellMonto.setBorder(Rectangle.BOX);
            cellMonto.setBorderColor(azulInstitucional);
            cellMonto.setPadding(6);
            cellMonto.setBackgroundColor(new Color(239, 246, 255));
            Paragraph pTotal = new Paragraph("TOTAL: S/ " + String.format("%.2f", doc.getImporteTotal()), fontTotalNumero);
            pTotal.setAlignment(Element.ALIGN_RIGHT);
            cellMonto.addElement(pTotal);
            totalTable.addCell(cellMonto);

            document.add(totalTable);

            // Separador
            document.add(new LineSeparator(1, 100, grisBorde, Element.ALIGN_CENTER, -2));

            // 5. PIE DE PÁGINA: CÓDIGO QR, VERIFICACIÓN Y ADVERTENCIAS LEGALES
            PdfPTable qrFooterTable = new PdfPTable(2);
            qrFooterTable.setWidthPercentage(100);
            qrFooterTable.setWidths(new float[]{22f, 78f});
            qrFooterTable.setSpacingBefore(8);

            // Código QR
            String urlQr = (urlVerificacion != null && !urlVerificacion.isBlank())
                    ? urlVerificacion
                    : "https://muniquinua.gob.pe/verificar/documento-tributario/" + doc.getCodigoVerificacion();

            byte[] qrBytes = qrCodeGenerator.generateQrCodePng(urlQr, 110, 110);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.scaleToFit(75, 75);

            PdfPCell cellQr = new PdfPCell(qrImage);
            cellQr.setBorder(Rectangle.NO_BORDER);
            cellQr.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellQr.setVerticalAlignment(Element.ALIGN_MIDDLE);
            qrFooterTable.addCell(cellQr);

            // Leyenda de Verificación y Advertencia
            PdfPCell cellLeyenda = new PdfPCell();
            cellLeyenda.setBorder(Rectangle.NO_BORDER);
            cellLeyenda.setPaddingLeft(10);

            Paragraph pCodSeg = new Paragraph("CÓDIGO DE VERIFICACIÓN ELECTRÓNICA: " + doc.getCodigoVerificacion(), fontLabel);
            pCodSeg.setSpacingAfter(2);
            Paragraph pHash = new Paragraph("HASH SHA-256: " + doc.getHashDocumento(), fontHash);
            pHash.setSpacingAfter(4);

            Paragraph pAdv1 = new Paragraph(
                    "Este documento constituye el recibo oficial emitido por la Municipalidad Distrital de Quinua para acreditar la recaudación de tributos municipales.",
                    fontDisclaimer);
            Paragraph pAdv2 = new Paragraph(
                    "No constituye comprobante de pago electrónico regulado por la SUNAT. La autenticidad e integridad del presente documento puede ser verificada escaneando el código QR o en: " + urlQr,
                    fontDisclaimer);

            cellLeyenda.addElement(pCodSeg);
            cellLeyenda.addElement(pHash);
            cellLeyenda.addElement(pAdv1);
            cellLeyenda.addElement(pAdv2);

            if (doc.getNumeroReimpresiones() != null && doc.getNumeroReimpresiones() > 0) {
                Paragraph pReimp = new Paragraph("COPIA REIMPRESA N° " + doc.getNumeroReimpresiones() + " — Emisión Original Inalterada",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(185, 28, 28)));
                pReimp.setSpacingBefore(3);
                cellLeyenda.addElement(pReimp);
            }

            qrFooterTable.addCell(cellLeyenda);
            document.add(qrFooterTable);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF de documento tributario {}: {}", doc.getCodigoDocumento(), e.getMessage(), e);
            throw new BusinessException("Error al generar PDF del documento tributario: " + e.getMessage());
        }
    }

    public String calcularSha256(byte[] data) {
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
            throw new BusinessException("Error al calcular el hash SHA-256: " + e.getMessage());
        }
    }

    private void agregarCelda(PdfPTable table, String texto, Font font, Color bgColor) {
        agregarCelda(table, texto, font, bgColor, Element.ALIGN_LEFT);
    }

    private void agregarCelda(PdfPTable table, String texto, Font font, Color bgColor, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto != null ? texto : "-", font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(4);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void agregarCeldaHeader(PdfPTable table, String texto, Font font, Color bgColor, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(5);
        cell.setBorderColor(new Color(203, 213, 225));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private String convertirMontoALetras(BigDecimal monto) {
        if (monto == null) return "CERO CON 00/100 SOLES";
        long parteEntera = monto.longValue();
        int centavos = monto.remainder(BigDecimal.ONE).multiply(new BigDecimal("100")).intValue();
        return String.format("%s CON %02d/100 SOLES", convertirNumero(parteEntera), Math.abs(centavos));
    }

    private String convertirNumero(long n) {
        if (n == 0) return "CERO";
        if (n < 0) return "MENOS " + convertirNumero(-n);
        if (n <= 20) {
            String[] unidades = {"", "UN", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE",
                    "DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISEIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE", "VEINTE"};
            return unidades[(int) n];
        }
        if (n < 100) {
            String[] decenas = {"", "", "VEINTI", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"};
            int dec = (int) (n / 10);
            int uni = (int) (n % 10);
            if (dec == 2) return "VEINTI" + convertirNumero(uni);
            return decenas[dec] + (uni > 0 ? " Y " + convertirNumero(uni) : "");
        }
        if (n < 1000) {
            if (n == 100) return "CIEN";
            String[] centenas = {"", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS", "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"};
            int cen = (int) (n / 100);
            long resto = n % 100;
            return centenas[cen] + (resto > 0 ? " " + convertirNumero(resto) : "");
        }
        if (n < 1000000) {
            long miles = n / 1000;
            long resto = n % 1000;
            String strMiles = miles == 1 ? "MIL" : convertirNumero(miles) + " MIL";
            return strMiles + (resto > 0 ? " " + convertirNumero(resto) : "");
        }
        return String.valueOf(n);
    }
}
