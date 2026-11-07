package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ArbitrioFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.ArbitrioDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.DetalleCalculoArbitrioResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CalculoArbitrioMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PredioTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.CalculoArbitrioMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ArbitrioExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ArbitrioMunicipalService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.ArbitrioSpecification;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArbitrioExportServiceImpl implements ArbitrioExportService {

    private final CalculoArbitrioMunicipalRepository calculoRepository;
    private final ArbitrioMunicipalService arbitrioService;
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcel(ArbitrioFilterDto filtro) {
        List<CalculoArbitrioMunicipal> lista = obtenerCalculos(filtro);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Determinaciones Arbitrios");
            sheet.setDisplayGridlines(true);

            // Styles
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 10);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 13);
            titleStyle.setFont(titleFont);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());

            CellStyle numStyle = workbook.createCellStyle();
            numStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            numStyle.setAlignment(HorizontalAlignment.RIGHT);
            numStyle.setBorderBottom(BorderStyle.THIN);
            numStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            DataFormat df = workbook.createDataFormat();
            numStyle.setDataFormat(df.getFormat("#,##0.00"));

            // Title
            Row rowTitle = sheet.createRow(0);
            Cell cellTitle = rowTitle.createCell(0);
            cellTitle.setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA - DETERMINACIONES Y CÁLCULOS DE ARBITRIOS MUNICIPALES");
            cellTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));

            Row rowSub = sheet.createRow(1);
            Cell cellSub = rowSub.createCell(0);
            cellSub.setCellValue("Reporte institucional generado el " + LocalDateTime.now().format(FORMATO_FECHA_HORA) + " | Registros: " + lista.size());

            // Header row
            Row rowHead = sheet.createRow(3);
            String[] headers = {
                    "N°", "Código Cálculo", "Ejercicio", "Contribuyente", "Doc. Identidad",
                    "Código Predio", "Dirección Predio", "Subtotal (S/)", "Beneficio (S/)",
                    "Importe Anual (S/)", "Cuota Periódica (S/)", "Estado", "Tipo Cálculo", "Fecha Cálculo"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = rowHead.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            int n = 1;
            for (CalculoArbitrioMunicipal c : lista) {
                Row row = sheet.createRow(rowIdx++);

                Cell cNum = row.createCell(0);
                cNum.setCellValue(n++);
                cNum.setCellStyle(dataStyle);

                Cell cCod = row.createCell(1);
                cCod.setCellValue(c.getCodigoCalculo() != null ? c.getCodigoCalculo() : "-");
                cCod.setCellStyle(dataStyle);

                Cell cEjer = row.createCell(2);
                cEjer.setCellValue(c.getPeriodo() != null ? c.getPeriodo().getAnio() : 0);
                cEjer.setCellStyle(dataStyle);

                ContribuyenteTributario cont = c.getContribuyente();
                Cell cCont = row.createCell(3);
                cCont.setCellValue(cont != null ? cont.getNombreRazonSocial() : "-");
                cCont.setCellStyle(dataStyle);

                Cell cDoc = row.createCell(4);
                cDoc.setCellValue(cont != null ? (cont.getNumeroDocumento() != null ? cont.getNumeroDocumento() : "-") : "-");
                cDoc.setCellStyle(dataStyle);

                PredioTributario pred = c.getPredio();
                Cell cPred = row.createCell(5);
                cPred.setCellValue(pred != null ? pred.getCodigoPredio() : "-");
                cPred.setCellStyle(dataStyle);

                Cell cDir = row.createCell(6);
                cDir.setCellValue(pred != null ? (pred.getDireccion() != null ? pred.getDireccion() : "-") : "-");
                cDir.setCellStyle(dataStyle);

                BigDecimal anual = c.getImporteAnualTotal() != null ? c.getImporteAnualTotal() : BigDecimal.ZERO;
                BigDecimal desc = c.getMontoDescuentoBeneficio() != null ? c.getMontoDescuentoBeneficio() : BigDecimal.ZERO;
                BigDecimal subtotal = anual.add(desc);

                Cell cSub = row.createCell(7);
                cSub.setCellValue(subtotal.doubleValue());
                cSub.setCellStyle(numStyle);

                Cell cBen = row.createCell(8);
                cBen.setCellValue(desc.doubleValue());
                cBen.setCellStyle(numStyle);

                Cell cImp = row.createCell(9);
                cImp.setCellValue(anual.doubleValue());
                cImp.setCellStyle(numStyle);

                Cell cCuo = row.createCell(10);
                cCuo.setCellValue(c.getImportePeriodoTotal() != null ? c.getImportePeriodoTotal().doubleValue() : 0.0);
                cCuo.setCellStyle(numStyle);

                Cell cEst = row.createCell(11);
                cEst.setCellValue(c.getEstado() != null ? c.getEstado().getDescripcion() : "-");
                cEst.setCellStyle(dataStyle);

                Cell cTip = row.createCell(12);
                cTip.setCellValue(c.getTipoCalculo() != null ? c.getTipoCalculo().getDescripcion() : "-");
                cTip.setCellStyle(dataStyle);

                Cell cFec = row.createCell(13);
                cFec.setCellValue(c.getFechaCalculo() != null ? c.getFechaCalculo().format(FORMATO_FECHA_HORA) : "-");
                cFec.setCellStyle(dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) < 2800) {
                    sheet.setColumnWidth(i, 2800);
                }
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar determinaciones de arbitrios a Excel", e);
            throw new RuntimeException("Error al generar el archivo Excel de arbitrios municipales", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarPdf(ArbitrioFilterDto filtro) {
        List<CalculoArbitrioMunicipal> lista = obtenerCalculos(filtro);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 25, 25);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(16, 24, 39));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(100, 116, 139));
            Font headerTableFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(30, 41, 59));
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(16, 24, 39));

            Paragraph title = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subTitle = new Paragraph("REPORTE DE DETERMINACIONES Y CÁLCULOS DE ARBITRIOS MUNICIPALES", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 58, 138)));
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);

            Paragraph meta = new Paragraph("Generado el: " + LocalDateTime.now().format(FORMATO_FECHA_HORA) + " | Total registros: " + lista.size(), subtitleFont);
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(10);
            document.add(meta);

            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 9f, 6f, 22f, 10f, 14f, 10f, 10f, 9f, 13f});

            String[] pdfHeaders = {
                    "N°", "CÓDIGO", "EJERC.", "CONTRIBUYENTE", "DOC. IDENT.",
                    "PREDIO", "SUBTOTAL (S/)", "ANUAL (S/)", "ESTADO", "FECHA CÁLCULO"
            };

            for (String h : pdfHeaders) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerTableFont));
                cell.setBackgroundColor(new Color(30, 58, 138));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(4);
                table.addCell(cell);
            }

            int n = 1;
            BigDecimal sumaSubtotal = BigDecimal.ZERO;
            BigDecimal sumaTotal = BigDecimal.ZERO;

            for (CalculoArbitrioMunicipal c : lista) {
                Color bg = (n % 2 == 0) ? new Color(248, 250, 252) : Color.WHITE;

                PdfPCell cNum = new PdfPCell(new Phrase(String.valueOf(n++), cellFont));
                cNum.setBackgroundColor(bg);
                cNum.setHorizontalAlignment(Element.ALIGN_CENTER);
                cNum.setPadding(3);
                table.addCell(cNum);

                PdfPCell cCod = new PdfPCell(new Phrase(c.getCodigoCalculo() != null ? c.getCodigoCalculo() : "-", cellFont));
                cCod.setBackgroundColor(bg);
                cCod.setPadding(3);
                table.addCell(cCod);

                int anio = c.getPeriodo() != null ? c.getPeriodo().getAnio() : 0;
                PdfPCell cEjer = new PdfPCell(new Phrase(String.valueOf(anio), cellFont));
                cEjer.setBackgroundColor(bg);
                cEjer.setHorizontalAlignment(Element.ALIGN_CENTER);
                cEjer.setPadding(3);
                table.addCell(cEjer);

                ContribuyenteTributario cont = c.getContribuyente();
                PdfPCell cNom = new PdfPCell(new Phrase(cont != null ? cont.getNombreRazonSocial() : "-", cellFont));
                cNom.setBackgroundColor(bg);
                cNom.setPadding(3);
                table.addCell(cNom);

                PdfPCell cDoc = new PdfPCell(new Phrase(cont != null && cont.getNumeroDocumento() != null ? cont.getNumeroDocumento() : "-", cellFont));
                cDoc.setBackgroundColor(bg);
                cDoc.setPadding(3);
                table.addCell(cDoc);

                PredioTributario pred = c.getPredio();
                PdfPCell cPred = new PdfPCell(new Phrase(pred != null ? pred.getCodigoPredio() : "-", cellFont));
                cPred.setBackgroundColor(bg);
                cPred.setPadding(3);
                table.addCell(cPred);

                BigDecimal anual = c.getImporteAnualTotal() != null ? c.getImporteAnualTotal() : BigDecimal.ZERO;
                BigDecimal desc = c.getMontoDescuentoBeneficio() != null ? c.getMontoDescuentoBeneficio() : BigDecimal.ZERO;
                BigDecimal sub = anual.add(desc);

                sumaSubtotal = sumaSubtotal.add(sub);
                PdfPCell cSub = new PdfPCell(new Phrase(String.format("%,.2f", sub), cellFont));
                cSub.setBackgroundColor(bg);
                cSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cSub.setPadding(3);
                table.addCell(cSub);

                sumaTotal = sumaTotal.add(anual);
                PdfPCell cImp = new PdfPCell(new Phrase(String.format("%,.2f", anual), cellFont));
                cImp.setBackgroundColor(bg);
                cImp.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cImp.setPadding(3);
                table.addCell(cImp);

                PdfPCell cEst = new PdfPCell(new Phrase(c.getEstado() != null ? c.getEstado().getDescripcion() : "-", cellFont));
                cEst.setBackgroundColor(bg);
                cEst.setPadding(3);
                table.addCell(cEst);

                PdfPCell cFec = new PdfPCell(new Phrase(c.getFechaCalculo() != null ? c.getFechaCalculo().format(FORMATO_FECHA) : "-", cellFont));
                cFec.setBackgroundColor(bg);
                cFec.setPadding(3);
                table.addCell(cFec);
            }

            // Totals row
            PdfPCell totLabel = new PdfPCell(new Phrase("TOTALES:", totalFont));
            totLabel.setColspan(6);
            totLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totLabel.setBackgroundColor(new Color(226, 232, 240));
            totLabel.setPadding(4);
            table.addCell(totLabel);

            PdfPCell totSub = new PdfPCell(new Phrase(String.format("%,.2f", sumaSubtotal), totalFont));
            totSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totSub.setBackgroundColor(new Color(226, 232, 240));
            totSub.setPadding(4);
            table.addCell(totSub);

            PdfPCell totImp = new PdfPCell(new Phrase(String.format("%,.2f", sumaTotal), totalFont));
            totImp.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totImp.setBackgroundColor(new Color(226, 232, 240));
            totImp.setPadding(4);
            table.addCell(totImp);

            PdfPCell totVacio = new PdfPCell(new Phrase("", totalFont));
            totVacio.setColspan(2);
            totVacio.setBackgroundColor(new Color(226, 232, 240));
            table.addCell(totVacio);

            document.add(table);

            Paragraph nota = new Paragraph("Marco Legal: TUO de la Ley de Tributación Municipal (D.S. 156-2004-EF) y Jurisprudencia vinculante del Tribunal Constitucional. Toda determinación requiere ordenanza ratificada y publicada.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, new Color(100, 116, 139)));
            nota.setSpacingBefore(12);
            document.add(nota);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar determinaciones de arbitrios a PDF", e);
            throw new RuntimeException("Error al generar el archivo PDF de arbitrios municipales", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarHojaLiquidacionPdf(UUID calculoId) {
        ArbitrioDetailResponse c = arbitrioService.obtenerCalculoPorId(calculoId);
        boolean esSimulacion = c.getEstado() == EstadoCalculoArbitrio.SIMULACION;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 25, 25, 25, 25);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(16, 24, 39));
            Font h2Font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(30, 58, 138));
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(51, 65, 85));
            Font valFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(15, 23, 42));
            Font thFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.WHITE);
            Font tdFont = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(30, 41, 59));

            // Banner if simulation
            if (esSimulacion) {
                PdfPTable simTable = new PdfPTable(1);
                simTable.setWidthPercentage(100);
                PdfPCell simCell = new PdfPCell(new Phrase("DOCUMENTO INFORMATIVO DE SIMULACIÓN - NO CONSTITUYE LIQUIDACIÓN OFICIAL NI DEUDA EXIGIBLE",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
                simCell.setBackgroundColor(new Color(220, 38, 38));
                simCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                simCell.setPadding(4);
                simTable.addCell(simCell);
                simTable.setSpacingAfter(6);
                document.add(simTable);
            }

            // Header
            Paragraph title = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("SUBGERENCIA DE ADMINISTRACIÓN Y RECAUDACIÓN TRIBUTARIA\nHOJA DE LIQUIDACIÓN Y DETERMINACIÓN DE ARBITRIOS MUNICIPALES",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 58, 138)));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(8);
            document.add(subtitle);

            // Table 1: Metadatos del cálculo
            PdfPTable metaTable = new PdfPTable(4);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{22f, 28f, 22f, 28f});

            addMetaCell(metaTable, "CÓDIGO DETERMINACIÓN:", c.getCodigoCalculo(), labelFont, valFont);
            addMetaCell(metaTable, "EJERCICIO FISCAL:", String.valueOf(c.getPeriodoAnio()), labelFont, valFont);
            addMetaCell(metaTable, "ESTADO:", c.getEstado().getDescripcion(), labelFont, valFont);
            addMetaCell(metaTable, "FECHA EMISIÓN:", c.getFechaCalculo() != null ? c.getFechaCalculo().format(FORMATO_FECHA_HORA) : "-", labelFont, valFont);
            addMetaCell(metaTable, "VERSIÓN CÁLCULO:", "v" + c.getVersionCalculo(), labelFont, valFont);
            addMetaCell(metaTable, "TIPO CÁLCULO:", c.getTipoCalculo().getDescripcion(), labelFont, valFont);
            metaTable.setSpacingAfter(10);
            document.add(metaTable);

            // Seccion Contribuyente
            Paragraph sec1 = new Paragraph("1. DATOS DEL CONTRIBUYENTE Y PREDIO OBJETO DE LIQUIDACIÓN", h2Font);
            sec1.setSpacingAfter(4);
            document.add(sec1);

            PdfPTable contribTable = new PdfPTable(4);
            contribTable.setWidthPercentage(100);
            contribTable.setWidths(new float[]{22f, 28f, 22f, 28f});

            addMetaCell(contribTable, "CONTRIBUYENTE:", c.getContribuyenteNombre(), labelFont, valFont);
            addMetaCell(contribTable, "DOC. IDENTIDAD:", c.getContribuyenteDocumento(), labelFont, valFont);
            addMetaCell(contribTable, "CÓDIGO PREDIO:", c.getPredioCodigo(), labelFont, valFont);
            addMetaCell(contribTable, "CLASIFICACIÓN:", c.getClasificacionPredio() != null ? c.getClasificacionPredio() : "-", labelFont, valFont);
            addMetaCell(contribTable, "DIRECCIÓN PREDIO:", c.getPredioDireccion(), labelFont, valFont);
            addMetaCell(contribTable, "TIPO PREDIO:", c.getTipoPredio() != null ? c.getTipoPredio() : "-", labelFont, valFont);
            addMetaCell(contribTable, "ÁREA TERRENO (M2):", c.getAreaTerreno() != null ? String.format("%,.2f m²", c.getAreaTerreno()) : "-", labelFont, valFont);
            addMetaCell(contribTable, "ÁREA CONST. (M2):", c.getAreaConstruida() != null ? String.format("%,.2f m²", c.getAreaConstruida()) : "-", labelFont, valFont);
            addMetaCell(contribTable, "PORCENTAJE CONDÓMINO:", String.format("%,.2f %%", c.getPorcentajeParticipacion()), labelFont, valFont);
            addMetaCell(contribTable, "BENEFICIO APLICADO:", c.getBeneficioAplicado() != null ? c.getBeneficioAplicado().getDescripcion() : "SIN BENEFICIO", labelFont, valFont);
            contribTable.setSpacingAfter(10);
            document.add(contribTable);

            // Seccion Servicios y Detalle
            Paragraph sec2 = new Paragraph("2. DESGLOSE DE SERVICIOS PÚBLICOS MUNICIPALES DETERMINADOS", h2Font);
            sec2.setSpacingAfter(4);
            document.add(sec2);

            PdfPTable detTable = new PdfPTable(7);
            detTable.setWidthPercentage(100);
            detTable.setWidths(new float[]{25f, 18f, 15f, 11f, 10f, 10f, 11f});

            String[] detHeaders = {"SERVICIO MUNICIPAL", "METODOLOGÍA", "CRITERIO / BASE", "COSTO ANUAL", "SUBTOTAL (S/)", "BENEF. (S/)", "ANUAL (S/)"};
            for (String h : detHeaders) {
                PdfPCell th = new PdfPCell(new Phrase(h, thFont));
                th.setBackgroundColor(new Color(30, 58, 138));
                th.setHorizontalAlignment(Element.ALIGN_CENTER);
                th.setVerticalAlignment(Element.ALIGN_MIDDLE);
                th.setPadding(3);
                detTable.addCell(th);
            }

            BigDecimal sumaSubtotalDet = BigDecimal.ZERO;
            BigDecimal sumaBeneficioDet = BigDecimal.ZERO;

            if (c.getDetalles() != null) {
                int dn = 0;
                for (DetalleCalculoArbitrioResponse det : c.getDetalles()) {
                    Color bg = (dn++ % 2 == 0) ? new Color(248, 250, 252) : Color.WHITE;

                    PdfPCell cNom = new PdfPCell(new Phrase(det.getNombreServicio(), tdFont));
                    cNom.setBackgroundColor(bg);
                    cNom.setPadding(3);
                    detTable.addCell(cNom);

                    String metStr = det.getTipoServicio() != null ? det.getTipoServicio().getDescripcion() : "-";
                    PdfPCell cMet = new PdfPCell(new Phrase(metStr, tdFont));
                    cMet.setBackgroundColor(bg);
                    cMet.setPadding(3);
                    detTable.addCell(cMet);

                    String critInfo = String.format("%s (%s %s)",
                            det.getCriterioAplicado() != null ? det.getCriterioAplicado() : "-",
                            det.getParametroBaseValor() != null ? String.format("%,.2f", det.getParametroBaseValor()) : "1.00",
                            det.getUnidadBase() != null ? det.getUnidadBase() : "");
                    PdfPCell cCrit = new PdfPCell(new Phrase(critInfo, tdFont));
                    cCrit.setBackgroundColor(bg);
                    cCrit.setPadding(3);
                    detTable.addCell(cCrit);

                    BigDecimal costo = det.getCostoAnualServicio() != null ? det.getCostoAnualServicio() : BigDecimal.ZERO;
                    PdfPCell cCos = new PdfPCell(new Phrase(String.format("%,.2f", costo), tdFont));
                    cCos.setBackgroundColor(bg);
                    cCos.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cCos.setPadding(3);
                    detTable.addCell(cCos);

                    BigDecimal subtotalVal = det.getCostoAnualServicio() != null ? det.getCostoAnualServicio() : BigDecimal.ZERO;
                    sumaSubtotalDet = sumaSubtotalDet.add(subtotalVal);
                    PdfPCell cSub = new PdfPCell(new Phrase(String.format("%,.2f", subtotalVal), tdFont));
                    cSub.setBackgroundColor(bg);
                    cSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cSub.setPadding(3);
                    detTable.addCell(cSub);

                    BigDecimal ben = det.getDescuentoAplicado() != null ? det.getDescuentoAplicado() : BigDecimal.ZERO;
                    sumaBeneficioDet = sumaBeneficioDet.add(ben);
                    PdfPCell cBen = new PdfPCell(new Phrase(String.format("%,.2f", ben), tdFont));
                    cBen.setBackgroundColor(bg);
                    cBen.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cBen.setPadding(3);
                    detTable.addCell(cBen);

                    BigDecimal tot = det.getImporteFinalAnual() != null ? det.getImporteFinalAnual() : BigDecimal.ZERO;
                    PdfPCell cTot = new PdfPCell(new Phrase(String.format("%,.2f", tot), tdFont));
                    cTot.setBackgroundColor(bg);
                    cTot.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cTot.setPadding(3);
                    detTable.addCell(cTot);
                }
            }

            // Totals row
            PdfPCell totTitle = new PdfPCell(new Phrase("TOTALES DETERMINADOS:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, new Color(16, 24, 39))));
            totTitle.setColspan(4);
            totTitle.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totTitle.setBackgroundColor(new Color(226, 232, 240));
            totTitle.setPadding(3);
            detTable.addCell(totTitle);

            PdfPCell cSubTot = new PdfPCell(new Phrase(String.format("%,.2f", sumaSubtotalDet), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7)));
            cSubTot.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cSubTot.setBackgroundColor(new Color(226, 232, 240));
            cSubTot.setPadding(3);
            detTable.addCell(cSubTot);

            PdfPCell cBenTot = new PdfPCell(new Phrase(String.format("%,.2f", sumaBeneficioDet), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7)));
            cBenTot.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cBenTot.setBackgroundColor(new Color(226, 232, 240));
            cBenTot.setPadding(3);
            detTable.addCell(cBenTot);

            BigDecimal totFinal = c.getImporteAnualTotal() != null ? c.getImporteAnualTotal() : BigDecimal.ZERO;
            PdfPCell cAnualTot = new PdfPCell(new Phrase(String.format("%,.2f", totFinal), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7)));
            cAnualTot.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cAnualTot.setBackgroundColor(new Color(226, 232, 240));
            cAnualTot.setPadding(3);
            detTable.addCell(cAnualTot);

            detTable.setSpacingAfter(10);
            document.add(detTable);

            // Resumen de Pagos Cuotas
            PdfPTable cuotaTable = new PdfPTable(2);
            cuotaTable.setWidthPercentage(100);
            cuotaTable.setWidths(new float[]{60f, 40f});

            String perDesc = c.getSubperiodo() != null ? c.getSubperiodo().toUpperCase() : "TRIMESTRAL";
            PdfPCell cLabelC = new PdfPCell(new Phrase("CUOTA PERIODICA ESTIMADA (" + perDesc + "):", labelFont));
            cLabelC.setPadding(4);
            cLabelC.setBackgroundColor(new Color(241, 245, 249));
            cuotaTable.addCell(cLabelC);

            BigDecimal impPeriodo = c.getImportePeriodoTotal() != null ? c.getImportePeriodoTotal() : BigDecimal.ZERO;
            PdfPCell cValC = new PdfPCell(new Phrase(String.format("S/ %,.2f cada cuota", impPeriodo), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(30, 58, 138))));
            cValC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cValC.setPadding(4);
            cValC.setBackgroundColor(new Color(241, 245, 249));
            cuotaTable.addCell(cValC);

            cuotaTable.setSpacingAfter(12);
            document.add(cuotaTable);

            // Cláusula Legal
            Paragraph clausula = new Paragraph("BASE NORMATIVA Y CONTROL CONSTITUCIONAL:\n" +
                    "La presente determinación se fundamenta en el TUO de la Ley de Tributación Municipal (D.S. 156-2004-EF), artículos 66 al 70, y en los criterios vinculantes sentados por el Tribunal Constitucional en los expedientes STC 0041-2004-AI/TC y 0053-2004-PI/TC, que exigen costos efectivamente sustentados y metodologías de distribución razonables y proporcionales. Esta liquidación no genera deudas exigibles hasta su registro y emisión formal conforme al ordenamiento municipal.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, new Color(71, 85, 105)));
            clausula.setSpacingAfter(25);
            document.add(clausula);

            // Firmas
            PdfPTable signTable = new PdfPTable(2);
            signTable.setWidthPercentage(100);
            signTable.setWidths(new float[]{50f, 50f});

            PdfPCell sign1 = new PdfPCell(new Phrase("_____________________________________\nRESPONSABLE DE LIQUIDACIÓN Y CÁLCULO\nOficina de Recaudación Tributaria", tdFont));
            sign1.setBorder(0);
            sign1.setHorizontalAlignment(Element.ALIGN_CENTER);
            signTable.addCell(sign1);

            PdfPCell sign2 = new PdfPCell(new Phrase("_____________________________________\nSUBGERENTE DE ADMINISTRACIÓN TRIBUTARIA\nMunicipalidad Distrital de Quinua", tdFont));
            sign2.setBorder(0);
            sign2.setHorizontalAlignment(Element.ALIGN_CENTER);
            signTable.addCell(sign2);

            document.add(signTable);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar hoja de liquidación de arbitrios a PDF", e);
            throw new RuntimeException("Error al generar hoja de liquidación de arbitrios", e);
        }
    }

    private void addMetaCell(PdfPTable table, String label, String val, Font labelFont, Font valFont) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, labelFont));
        cLabel.setBackgroundColor(new Color(248, 250, 252));
        cLabel.setPadding(3);
        table.addCell(cLabel);

        PdfPCell cVal = new PdfPCell(new Phrase(val != null ? val : "-", valFont));
        cVal.setPadding(3);
        table.addCell(cVal);
    }

    private List<CalculoArbitrioMunicipal> obtenerCalculos(ArbitrioFilterDto filtro) {
        Specification<CalculoArbitrioMunicipal> spec = ArbitrioSpecification.conFiltros(filtro);
        return calculoRepository.findAll(spec, PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "fechaCalculo"))).getContent();
    }
}
