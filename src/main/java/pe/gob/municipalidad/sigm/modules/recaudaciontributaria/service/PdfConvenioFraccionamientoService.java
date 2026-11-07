package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConvenioFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CronogramaFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CuotaFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.CuotaFraccionamientoTributarioRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Motor de generación de PDF oficial para Convenios Formales de Fraccionamiento Tributario.
 * Formato A4 institucional, tabla de amortización, cláusulas de cumplimiento y QR de verificación.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfConvenioFraccionamientoService {

    private final QrCodeGenerator qrCodeGenerator;
    private final CuotaFraccionamientoTributarioRepository cuotaRepository;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generarPdfConvenio(ConvenioFraccionamientoTributario conv, String urlVerificacion) {
        if (conv == null) {
            throw new BusinessException("El convenio de fraccionamiento es nulo");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPdfVersion(PdfWriter.PDF_VERSION_1_7);

            document.open();

            // Colores institucionales
            Color azulInstitucional = new Color(29, 78, 216);
            Color grisTexto = new Color(30, 41, 59);
            Color grisBorde = new Color(203, 213, 225);
            Color grisFondoCabecera = new Color(241, 245, 249);
            Color grisSecundario = new Color(100, 116, 139);

            Font fontTituloPrincipal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, azulInstitucional);
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, grisSecundario);
            Font fontConvenioNumero = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, azulInstitucional);
            Font fontSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, azulInstitucional);
            Font fontTextoNormal = FontFactory.getFont(FontFactory.HELVETICA, 8, grisTexto);
            Font fontTextoNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, grisTexto);
            Font fontTextoPequeno = FontFactory.getFont(FontFactory.HELVETICA, 7, grisSecundario);

            // 1. ENCABEZADO INSTITUCIONAL
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{65f, 35f});

            PdfPCell cellIzq = new PdfPCell();
            cellIzq.setBorder(Rectangle.NO_BORDER);
            cellIzq.addElement(new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTituloPrincipal));
            cellIzq.addElement(new Paragraph("SUBGERENCIA DE ADMINISTRACIÓN TRIBUTARIA Y RENTAS", fontSubtitulo));
            cellIzq.addElement(new Paragraph("Provincia de Huamanga - Región Ayacucho | Capital de la Libertad Americana", fontTextoPequeno));
            headerTable.addCell(cellIzq);

            PdfPCell cellDer = new PdfPCell();
            cellDer.setBorder(Rectangle.BOX);
            cellDer.setBorderColor(azulInstitucional);
            cellDer.setBorderWidth(1.2f);
            cellDer.setBackgroundColor(grisFondoCabecera);
            cellDer.setPadding(8f);
            cellDer.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph pRuc = new Paragraph("R.U.C. N° 20177759841", fontTextoNegrita);
            pRuc.setAlignment(Element.ALIGN_CENTER);
            cellDer.addElement(pRuc);

            Paragraph pTipo = new Paragraph("CONVENIO DE FRACCIONAMIENTO", fontConvenioNumero);
            pTipo.setAlignment(Element.ALIGN_CENTER);
            cellDer.addElement(pTipo);

            Paragraph pNum = new Paragraph(conv.getCodigoConvenio(), fontTituloPrincipal);
            pNum.setAlignment(Element.ALIGN_CENTER);
            cellDer.addElement(pNum);

            headerTable.addCell(cellDer);
            document.add(headerTable);

            document.add(new Paragraph(" "));
            LineSeparator separator = new LineSeparator(1f, 100f, azulInstitucional, Element.ALIGN_CENTER, -2);
            document.add(separator);
            document.add(new Paragraph(" "));

            // 2. DATOS DEL CONTRIBUYENTE Y DEL CONVENIO
            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{22f, 28f, 22f, 28f});

            agregarCeldaTitulo(infoTable, "DATOS GENERALES DEL CONTRIBUYENTE Y RESOLUCIÓN", 4, fontSeccion, grisFondoCabecera, grisBorde);

            agregarCampo(infoTable, "Contribuyente:", conv.getContribuyente().getNombreRazonSocial(), fontTextoNegrita, fontTextoNormal, grisBorde);
            agregarCampo(infoTable, "N° Documento:", conv.getContribuyente().getNumeroDocumento(), fontTextoNegrita, fontTextoNormal, grisBorde);

            agregarCampo(infoTable, "Código Contribuyente:", conv.getContribuyente().getCodigoContribuyente(), fontTextoNegrita, fontTextoNormal, grisBorde);
            agregarCampo(infoTable, "Domicilio Fiscal:", conv.getContribuyente().getDomicilioFiscal(), fontTextoNegrita, fontTextoNormal, grisBorde);

            agregarCampo(infoTable, "Resolución de Aprobación:", conv.getResolucionAprobacion(), fontTextoNegrita, fontTextoNormal, grisBorde);
            agregarCampo(infoTable, "Fecha de Aprobación:", conv.getFechaAprobacion().format(FORMATO_FECHA), fontTextoNegrita, fontTextoNormal, grisBorde);

            agregarCampo(infoTable, "Programa:", conv.getPrograma().getNombre(), fontTextoNegrita, fontTextoNormal, grisBorde);
            agregarCampo(infoTable, "Estado Legal:", conv.getEstado().getDescripcion(), fontTextoNegrita, fontTextoNormal, grisBorde);

            document.add(infoTable);
            document.add(new Paragraph(" "));

            // 3. RESUMEN FINANCIERO DEL FRACCIONAMIENTO
            CronogramaFraccionamientoTributario cron = conv.getCronograma();
            PdfPTable finTable = new PdfPTable(4);
            finTable.setWidthPercentage(100);
            finTable.setWidths(new float[]{25f, 25f, 25f, 25f});

            agregarCeldaTitulo(finTable, "CONDICIONES FINANCIERAS ACORDADAS (ART. 36 TUO CÓDIGO TRIBUTARIO)", 4, fontSeccion, grisFondoCabecera, grisBorde);

            agregarCampo(finTable, "Deuda Base Total:", "S/ " + (cron != null ? cron.getImporteBase() : "0.00"), fontTextoNegrita, fontTextoNormal, grisBorde);
            agregarCampo(finTable, "Cuota Inicial:", "S/ " + (cron != null ? cron.getImporteCuotaInicial() : "0.00"), fontTextoNegrita, fontTextoNormal, grisBorde);
            agregarCampo(finTable, "Monto Financiado:", "S/ " + (cron != null ? cron.getImporteFinanciado() : "0.00"), fontTextoNegrita, fontTextoNormal, grisBorde);
            agregarCampo(finTable, "Tasa Interés Mensual:", (cron != null ? cron.getTasaInteresAplicada() : "0.00") + " %", fontTextoNegrita, fontTextoNormal, grisBorde);

            agregarCampo(finTable, "Interés de Fraccionamiento:", "S/ " + (cron != null ? cron.getImporteAccesorios() : "0.00"), fontTextoNegrita, fontTextoNormal, grisBorde);
            agregarCampo(finTable, "Importe Total a Pagar:", "S/ " + (cron != null ? cron.getImporteTotal() : "0.00"), fontTextoNegrita, fontTextoNormal, grisBorde);
            agregarCampo(finTable, "Número de Cuotas:", String.valueOf(cron != null ? cron.getNumeroCuotas() : 0), fontTextoNegrita, fontTextoNormal, grisBorde);
            agregarCampo(finTable, "Inicio de Vigencia:", conv.getFechaInicioVigencia().format(FORMATO_FECHA), fontTextoNegrita, fontTextoNormal, grisBorde);

            document.add(finTable);
            document.add(new Paragraph(" "));

            // 4. TABLA DE CRONOGRAMA DE CUOTAS
            List<CuotaFraccionamientoTributario> cuotas = cuotaRepository.findByCronogramaIdOrderByNumeroCuotaAsc(cron.getId());
            PdfPTable cuotasTable = new PdfPTable(6);
            cuotasTable.setWidthPercentage(100);
            cuotasTable.setWidths(new float[]{10f, 18f, 18f, 18f, 18f, 18f});

            agregarCeldaTitulo(cuotasTable, "CRONOGRAMA OFICIAL DE AMORTIZACIÓN", 6, fontSeccion, grisFondoCabecera, grisBorde);

            agregarCabeceraColumna(cuotasTable, "N° Cuota", fontTextoNegrita, grisFondoCabecera, grisBorde);
            agregarCabeceraColumna(cuotasTable, "Vencimiento", fontTextoNegrita, grisFondoCabecera, grisBorde);
            agregarCabeceraColumna(cuotasTable, "Capital (S/)", fontTextoNegrita, grisFondoCabecera, grisBorde);
            agregarCabeceraColumna(cuotasTable, "Interés (S/)", fontTextoNegrita, grisFondoCabecera, grisBorde);
            agregarCabeceraColumna(cuotasTable, "Total Cuota (S/)", fontTextoNegrita, grisFondoCabecera, grisBorde);
            agregarCabeceraColumna(cuotasTable, "Estado", fontTextoNegrita, grisFondoCabecera, grisBorde);

            for (CuotaFraccionamientoTributario c : cuotas) {
                agregarCeldaDato(cuotasTable, String.valueOf(c.getNumeroCuota()), Element.ALIGN_CENTER, fontTextoNormal, grisBorde);
                agregarCeldaDato(cuotasTable, c.getFechaVencimiento().format(FORMATO_FECHA), Element.ALIGN_CENTER, fontTextoNormal, grisBorde);
                agregarCeldaDato(cuotasTable, c.getImporteCapital().toString(), Element.ALIGN_RIGHT, fontTextoNormal, grisBorde);
                agregarCeldaDato(cuotasTable, c.getImporteInteres().toString(), Element.ALIGN_RIGHT, fontTextoNormal, grisBorde);
                agregarCeldaDato(cuotasTable, c.getImporteTotal().toString(), Element.ALIGN_RIGHT, fontTextoNegrita, grisBorde);
                agregarCeldaDato(cuotasTable, c.getEstado().getDescripcion(), Element.ALIGN_CENTER, fontTextoNormal, grisBorde);
            }

            document.add(cuotasTable);
            document.add(new Paragraph(" "));

            // 5. CLÁUSULAS Y VERIFICACIÓN QR
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{75f, 25f});

            PdfPCell cellClausulas = new PdfPCell();
            cellClausulas.setBorder(Rectangle.BOX);
            cellClausulas.setBorderColor(grisBorde);
            cellClausulas.setPadding(6f);

            Paragraph pClausulaHeader = new Paragraph("TÉRMINOS Y COMPROMISOS LEGALES:", fontTextoNegrita);
            cellClausulas.addElement(pClausulaHeader);

            Paragraph pC1 = new Paragraph("1. El contribuyente se compromete a cancelar puntualmente las cuotas según las fechas fijadas en el cronograma.", fontTextoPequeno);
            Paragraph pC2 = new Paragraph("2. La acumulación de dos (02) o más cuotas vencidas e impagas dará lugar a la declaración formal de pérdida del beneficio de fraccionamiento conforme a ley.", fontTextoPequeno);
            Paragraph pC3 = new Paragraph("3. La resolución por pérdida del fraccionamiento reactiva la exigibilidad coactiva del saldo de capital más los intereses moratorios correspondientes.", fontTextoPequeno);
            Paragraph pC4 = new Paragraph("4. Código de verificación digital: " + conv.getCodigoVerificacion(), fontTextoPequeno);

            cellClausulas.addElement(pC1);
            cellClausulas.addElement(pC2);
            cellClausulas.addElement(pC3);
            cellClausulas.addElement(pC4);

            footerTable.addCell(cellClausulas);

            PdfPCell cellQr = new PdfPCell();
            cellQr.setBorder(Rectangle.BOX);
            cellQr.setBorderColor(grisBorde);
            cellQr.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellQr.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellQr.setPadding(4f);

            String qrUrl = urlVerificacion != null ? urlVerificacion : ("https://quinua.gob.pe/verificar?c=" + conv.getCodigoVerificacion());
            byte[] qrBytes = qrCodeGenerator.generateQrCodePng(qrUrl, 100, 100);
            if (qrBytes != null) {
                Image qrImg = Image.getInstance(qrBytes);
                qrImg.setAlignment(Element.ALIGN_CENTER);
                cellQr.addElement(qrImg);
            }
            Paragraph pQr = new Paragraph("Validar documento", fontTextoPequeno);
            pQr.setAlignment(Element.ALIGN_CENTER);
            cellQr.addElement(pQr);

            footerTable.addCell(cellQr);
            document.add(footerTable);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF para convenio {}", conv.getCodigoConvenio(), e);
            throw new BusinessException("No fue posible generar el documento PDF del convenio: " + e.getMessage());
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
            return "";
        }
    }

    private void agregarCeldaTitulo(PdfPTable table, String texto, int colSpan, Font font, Color fondo, Color borde) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setColspan(colSpan);
        cell.setBackgroundColor(fondo);
        cell.setBorderColor(borde);
        cell.setPadding(4f);
        table.addCell(cell);
    }

    private void agregarCabeceraColumna(PdfPTable table, String texto, Font font, Color fondo, Color borde) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(fondo);
        cell.setBorderColor(borde);
        cell.setPadding(4f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void agregarCeldaDato(PdfPTable table, String texto, int alineacion, Font font, Color borde) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBorderColor(borde);
        cell.setPadding(3.5f);
        cell.setHorizontalAlignment(alineacion);
        table.addCell(cell);
    }

    private void agregarCampo(PdfPTable table, String etiqueta, String valor, Font fontEtiqueta, Font fontValor, Color borde) {
        PdfPCell cEtiqueta = new PdfPCell(new Phrase(etiqueta, fontEtiqueta));
        cEtiqueta.setBorderColor(borde);
        cEtiqueta.setPadding(3.5f);
        table.addCell(cEtiqueta);

        PdfPCell cValor = new PdfPCell(new Phrase(valor != null ? valor : "-", fontValor));
        cValor.setBorderColor(borde);
        cValor.setPadding(3.5f);
        table.addCell(cValor);
    }
}
