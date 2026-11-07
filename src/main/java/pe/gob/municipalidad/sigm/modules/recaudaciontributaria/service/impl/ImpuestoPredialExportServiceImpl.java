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
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoPredialFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.CalculoPredialDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.CalculoPredialDetalleResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response.CalculoPredialTramoResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CalculoImpuestoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.CalculoImpuestoPredialRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CalculoPredialService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ImpuestoPredialExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.CalculoPredialSpecification;

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
public class ImpuestoPredialExportServiceImpl implements ImpuestoPredialExportService {

    private final CalculoImpuestoPredialRepository calculoRepository;
    private final CalculoPredialService calculoService;
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcel(CalculoPredialFilterDto filtro) {
        List<CalculoImpuestoPredial> lista = obtenerCalculos(filtro);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Determinaciones Prediales");
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
            cellTitle.setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA - DETERMINACIONES Y CÁLCULOS DEL IMPUESTO PREDIAL");
            cellTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

            Row rowSubtitle = sheet.createRow(1);
            Cell cellSubtitle = rowSubtitle.createCell(0);
            cellSubtitle.setCellValue("Generado el: " + LocalDateTime.now().format(FORMATO_FECHA_HORA) + " | Total registros: " + lista.size());
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 10));

            // Headers
            String[] headers = {
                    "N°", "CÓDIGO CÁLCULO", "EJERCICIO", "CONTRIBUYENTE", "DOC. IDENTIDAD",
                    "CANT. PREDIOS", "BASE IMPONIBLE (S/)", "DEDUCCIÓN BENEFICIO (S/)", "IMPUESTO ANUAL (S/)", "ESTADO", "FECHA CÁLCULO"
            };
            Row headerRow = sheet.createRow(3);
            headerRow.setHeightInPoints(24);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            int counter = 1;
            for (CalculoImpuestoPredial c : lista) {
                Row row = sheet.createRow(rowIdx++);

                Cell cNum = row.createCell(0);
                cNum.setCellValue(counter++);
                cNum.setCellStyle(dataStyle);

                Cell cCod = row.createCell(1);
                cCod.setCellValue(c.getCodigoCalculo() != null ? c.getCodigoCalculo() : "-");
                cCod.setCellStyle(dataStyle);

                Cell cEjer = row.createCell(2);
                cEjer.setCellValue(c.getPeriodo() != null ? c.getPeriodo().getAnio() : 0);
                cEjer.setCellStyle(dataStyle);

                ContribuyenteTributario cont = c.getContribuyente();
                Cell cNom = row.createCell(3);
                cNom.setCellValue(cont != null ? cont.getNombreRazonSocial() : "-");
                cNom.setCellStyle(dataStyle);

                Cell cDoc = row.createCell(4);
                cDoc.setCellValue(cont != null ? (cont.getTipoDocumento() + " " + cont.getNumeroDocumento()) : "-");
                cDoc.setCellStyle(dataStyle);

                Cell cPred = row.createCell(5);
                cPred.setCellValue(c.getPrediosDetalle() != null ? c.getPrediosDetalle().size() : (c.getCantidadPredios() != null ? c.getCantidadPredios() : 0));
                cPred.setCellStyle(dataStyle);

                Cell cBase = row.createCell(6);
                cBase.setCellValue(c.getTotalAutovaluoBruto() != null ? c.getTotalAutovaluoBruto().doubleValue() : 0.0);
                cBase.setCellStyle(numStyle);

                Cell cDed = row.createCell(7);
                cDed.setCellValue(c.getTotalDeduccionesBeneficios() != null ? c.getTotalDeduccionesBeneficios().doubleValue() : 0.0);
                cDed.setCellStyle(numStyle);

                Cell cImp = row.createCell(8);
                cImp.setCellValue(c.getImpuestoAnualDeterminado() != null ? c.getImpuestoAnualDeterminado().doubleValue() : 0.0);
                cImp.setCellStyle(numStyle);

                Cell cEst = row.createCell(9);
                cEst.setCellValue(c.getEstado() != null ? c.getEstado().getDescripcion() : "-");
                cEst.setCellStyle(dataStyle);

                Cell cFec = row.createCell(10);
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
            log.error("Error al exportar determinaciones prediales a Excel", e);
            throw new RuntimeException("Error al generar el archivo Excel de cálculo predial", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarPdf(CalculoPredialFilterDto filtro) {
        List<CalculoImpuestoPredial> lista = obtenerCalculos(filtro);

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

            Paragraph subTitle = new Paragraph("REPORTE DE DETERMINACIONES Y CÁLCULOS DEL IMPUESTO PREDIAL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 58, 138)));
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);

            Paragraph meta = new Paragraph("Generado el: " + LocalDateTime.now().format(FORMATO_FECHA_HORA) + " | Total registros: " + lista.size(), subtitleFont);
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(10);
            document.add(meta);

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 9f, 6f, 25f, 10f, 10f, 10f, 11f, 16f});

            String[] pdfHeaders = {
                    "N°", "CÓDIGO", "EJERC.", "CONTRIBUYENTE", "DOC. IDENTIDAD",
                    "BASE IMPON. (S/)", "IMPUESTO (S/)", "ESTADO", "FECHA CÁLCULO"
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
            BigDecimal sumaBase = BigDecimal.ZERO;
            BigDecimal sumaImpuesto = BigDecimal.ZERO;

            for (CalculoImpuestoPredial c : lista) {
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

                PdfPCell cDoc = new PdfPCell(new Phrase(cont != null ? (cont.getTipoDocumento() + " " + cont.getNumeroDocumento()) : "-", cellFont));
                cDoc.setBackgroundColor(bg);
                cDoc.setPadding(3);
                table.addCell(cDoc);

                BigDecimal base = c.getTotalAutovaluoBruto() != null ? c.getTotalAutovaluoBruto() : BigDecimal.ZERO;
                sumaBase = sumaBase.add(base);
                PdfPCell cBase = new PdfPCell(new Phrase(String.format("%,.2f", base), cellFont));
                cBase.setBackgroundColor(bg);
                cBase.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cBase.setPadding(3);
                table.addCell(cBase);

                BigDecimal imp = c.getImpuestoAnualDeterminado() != null ? c.getImpuestoAnualDeterminado() : BigDecimal.ZERO;
                sumaImpuesto = sumaImpuesto.add(imp);
                PdfPCell cImp = new PdfPCell(new Phrase(String.format("%,.2f", imp), cellFont));
                cImp.setBackgroundColor(bg);
                cImp.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cImp.setPadding(3);
                table.addCell(cImp);

                PdfPCell cEst = new PdfPCell(new Phrase(c.getEstado() != null ? c.getEstado().name() : "-", cellFont));
                cEst.setBackgroundColor(bg);
                cEst.setHorizontalAlignment(Element.ALIGN_CENTER);
                cEst.setPadding(3);
                table.addCell(cEst);

                PdfPCell cFec = new PdfPCell(new Phrase(c.getFechaCalculo() != null ? c.getFechaCalculo().format(FORMATO_FECHA_HORA) : "-", cellFont));
                cFec.setBackgroundColor(bg);
                cFec.setHorizontalAlignment(Element.ALIGN_CENTER);
                cFec.setPadding(3);
                table.addCell(cFec);
            }

            // Totals Row
            PdfPCell totLabel = new PdfPCell(new Phrase("TOTALES:", totalFont));
            totLabel.setColspan(5);
            totLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totLabel.setBackgroundColor(new Color(226, 232, 240));
            totLabel.setPadding(4);
            table.addCell(totLabel);

            PdfPCell totBase = new PdfPCell(new Phrase(String.format("%,.2f", sumaBase), totalFont));
            totBase.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totBase.setBackgroundColor(new Color(226, 232, 240));
            totBase.setPadding(4);
            table.addCell(totBase);

            PdfPCell totImp = new PdfPCell(new Phrase(String.format("%,.2f", sumaImpuesto), totalFont));
            totImp.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totImp.setBackgroundColor(new Color(226, 232, 240));
            totImp.setPadding(4);
            table.addCell(totImp);

            PdfPCell totVacio = new PdfPCell(new Phrase("", totalFont));
            totVacio.setColspan(2);
            totVacio.setBackgroundColor(new Color(226, 232, 240));
            table.addCell(totVacio);

            document.add(table);

            Paragraph nota = new Paragraph("Marco Legal: TUO de la Ley de Tributación Municipal (D.S. 156-2004-EF). Toda determinación tributaria requiere parámetros autorizados.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, new Color(100, 116, 139)));
            nota.setSpacingBefore(12);
            document.add(nota);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar determinaciones prediales a PDF", e);
            throw new RuntimeException("Error al generar el archivo PDF de cálculo predial", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarHojaLiquidacionPdf(UUID calculoId) {
        CalculoPredialDetailResponse c = calculoService.obtenerCalculoPorId(calculoId);
        boolean esSimulacion = c.getEstado() == EstadoCalculoPredial.SIMULACION;

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

            Paragraph sub = new Paragraph("SUBGERENCIA DE ADMINISTRACIÓN Y RECAUDACIÓN TRIBUTARIA\nHOJA DE LIQUIDACIÓN Y DETERMINACIÓN DEL IMPUESTO PREDIAL",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 58, 138)));
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(8);
            document.add(sub);

            // General Information Box
            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{20f, 30f, 20f, 30f});

            addKeyVal(infoTable, "Código de Cálculo:", c.getCodigoCalculo(), labelFont, valFont);
            addKeyVal(infoTable, "Ejercicio Fiscal:", String.valueOf(c.getPeriodoAnio()), labelFont, valFont);
            addKeyVal(infoTable, "Estado:", c.getEstado() != null ? c.getEstado().getDescripcion().toUpperCase() : "-", labelFont, valFont);
            addKeyVal(infoTable, "Tipo Cálculo:", c.getTipoCalculo() != null ? c.getTipoCalculo().name() : "-", labelFont, valFont);
            addKeyVal(infoTable, "Fecha de Cálculo:", c.getFechaCalculo() != null ? c.getFechaCalculo().format(FORMATO_FECHA_HORA) : "-", labelFont, valFont);
            addKeyVal(infoTable, "UIT Aplicable:", String.format("S/ %,.2f", c.getUitAplicada() != null ? c.getUitAplicada() : BigDecimal.ZERO), labelFont, valFont);

            addKeyVal(infoTable, "Contribuyente:", c.getContribuyenteNombre() != null ? c.getContribuyenteNombre() : "-", labelFont, valFont);
            addKeyVal(infoTable, "Documento:", c.getContribuyenteDocumento() != null ? c.getContribuyenteDocumento() : "-", labelFont, valFont);
            addKeyVal(infoTable, "Código Contribuyente:", c.getContribuyenteCodigo() != null ? c.getContribuyenteCodigo() : "-", labelFont, valFont);
            addKeyVal(infoTable, "Moneda:", c.getMoneda() != null ? c.getMoneda() : "PEN", labelFont, valFont);

            infoTable.setSpacingAfter(10);
            document.add(infoTable);

            // Predios Breakdown Section
            Paragraph secPredios = new Paragraph("1. VALORIZACIÓN DE PREDIOS COMPRENDIDOS EN LA DETERMINACIÓN", h2Font);
            secPredios.setSpacingAfter(4);
            document.add(secPredios);

            PdfPTable tablePredios = new PdfPTable(7);
            tablePredios.setWidthPercentage(100);
            tablePredios.setWidths(new float[]{8f, 22f, 10f, 12f, 12f, 14f, 14f});

            String[] thsPred = {"CÓDIGO", "DIRECCIÓN / UBICACIÓN", "% PART.", "VALOR TERR.", "VALOR CONST.", "AUTOVALÚO", "VAL. AFECTO"};
            for (String h : thsPred) {
                PdfPCell cell = new PdfPCell(new Phrase(h, thFont));
                cell.setBackgroundColor(new Color(30, 58, 138));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(3);
                tablePredios.addCell(cell);
            }

            if (c.getPredios() != null) {
                for (CalculoPredialDetalleResponse det : c.getPredios()) {
                    tablePredios.addCell(newCell(det.getCodigoPredio() != null ? det.getCodigoPredio() : "-", tdFont, Element.ALIGN_CENTER));
                    tablePredios.addCell(newCell(det.getDireccionPredio() != null ? det.getDireccionPredio() : "-", tdFont, Element.ALIGN_LEFT));
                    tablePredios.addCell(newCell(String.format("%.2f %%", det.getPorcentajeParticipacion() != null ? det.getPorcentajeParticipacion() : BigDecimal.ZERO), tdFont, Element.ALIGN_RIGHT));
                    tablePredios.addCell(newCell(String.format("%,.2f", det.getValorTerreno() != null ? det.getValorTerreno() : BigDecimal.ZERO), tdFont, Element.ALIGN_RIGHT));
                    tablePredios.addCell(newCell(String.format("%,.2f", det.getValorConstrucciones() != null ? det.getValorConstrucciones() : BigDecimal.ZERO), tdFont, Element.ALIGN_RIGHT));
                    tablePredios.addCell(newCell(String.format("%,.2f", det.getValorAutovaluoTotal() != null ? det.getValorAutovaluoTotal() : BigDecimal.ZERO), tdFont, Element.ALIGN_RIGHT));
                    tablePredios.addCell(newCell(String.format("%,.2f", det.getValorAutovaluoImputable() != null ? det.getValorAutovaluoImputable() : BigDecimal.ZERO), tdFont, Element.ALIGN_RIGHT));
                }
            }
            tablePredios.setSpacingAfter(10);
            document.add(tablePredios);

            // Progressive Brackets Section
            Paragraph secTramos = new Paragraph("2. ESCALA PROGRESIVA Y LIQUIDACIÓN TRIBUTARIA (ART. 13 TUO D.S. 156-2004-EF)", h2Font);
            secTramos.setSpacingAfter(4);
            document.add(secTramos);

            PdfPTable tableTramos = new PdfPTable(6);
            tableTramos.setWidthPercentage(100);
            tableTramos.setWidths(new float[]{10f, 28f, 16f, 14f, 16f, 16f});

            String[] thsTram = {"TRAMO", "DESCRIPCIÓN", "BASE TRAMO (S/)", "ALÍCUOTA", "IMPUESTO (S/)", "BASE IMP. TOTAL (S/)"};
            for (String h : thsTram) {
                PdfPCell cell = new PdfPCell(new Phrase(h, thFont));
                cell.setBackgroundColor(new Color(30, 58, 138));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(3);
                tableTramos.addCell(cell);
            }

            if (c.getTramos() != null) {
                for (CalculoPredialTramoResponse tr : c.getTramos()) {
                    tableTramos.addCell(newCell("Tramo " + tr.getNumeroTramo(), tdFont, Element.ALIGN_CENTER));
                    tableTramos.addCell(newCell(tr.getDescripcionTramo() != null ? tr.getDescripcionTramo() : "-", tdFont, Element.ALIGN_LEFT));
                    tableTramos.addCell(newCell(String.format("%,.2f", tr.getBaseImponibleTramo() != null ? tr.getBaseImponibleTramo() : BigDecimal.ZERO), tdFont, Element.ALIGN_RIGHT));
                    tableTramos.addCell(newCell(String.format("%.2f %%", tr.getAlicuotaPorcentaje() != null ? tr.getAlicuotaPorcentaje() : BigDecimal.ZERO), tdFont, Element.ALIGN_RIGHT));
                    tableTramos.addCell(newCell(String.format("%,.2f", tr.getImpuestoTramo() != null ? tr.getImpuestoTramo() : BigDecimal.ZERO), tdFont, Element.ALIGN_RIGHT));
                    tableTramos.addCell(newCell(String.format("%,.2f", c.getBaseImponibleNeta() != null ? c.getBaseImponibleNeta() : BigDecimal.ZERO), tdFont, Element.ALIGN_RIGHT));
                }
            }
            tableTramos.setSpacingAfter(10);
            document.add(tableTramos);

            // Summary Totals Box
            PdfPTable sumTable = new PdfPTable(2);
            sumTable.setWidthPercentage(60);
            sumTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            sumTable.setWidths(new float[]{55f, 45f});

            addSummaryRow(sumTable, "Base Imponible Total:", String.format("S/ %,.2f", c.getTotalAutovaluoBruto() != null ? c.getTotalAutovaluoBruto() : BigDecimal.ZERO), false);
            if (c.getTotalDeduccionesBeneficios() != null && c.getTotalDeduccionesBeneficios().compareTo(BigDecimal.ZERO) > 0) {
                addSummaryRow(sumTable, "Deducción Beneficio (" + c.getBeneficioAplicado() + "):", String.format("- S/ %,.2f", c.getTotalDeduccionesBeneficios()), false);
            }
            if (c.getBaseImponibleNeta() != null) {
                addSummaryRow(sumTable, "Base Neta Afecta:", String.format("S/ %,.2f", c.getBaseImponibleNeta()), false);
            }
            addSummaryRow(sumTable, "Impuesto Escala Bruto:", String.format("S/ %,.2f", c.getImpuestoCalculadoTramos() != null ? c.getImpuestoCalculadoTramos() : BigDecimal.ZERO), false);
            if (c.isAplicaImpuestoMinimo()) {
                addSummaryRow(sumTable, "Tope Mínimo Legal (0.6% UIT):", String.format("S/ %,.2f", c.getImpuestoMinimoLegal() != null ? c.getImpuestoMinimoLegal() : BigDecimal.ZERO), false);
            }
            addSummaryRow(sumTable, "IMPUESTO ANUAL DETERMINADO:", String.format("S/ %,.2f", c.getImpuestoAnualDeterminado() != null ? c.getImpuestoAnualDeterminado() : BigDecimal.ZERO), true);

            sumTable.setSpacingAfter(12);
            document.add(sumTable);

            // Legal notice & Signatures
            Paragraph foot = new Paragraph(
                    "Fundamento Normativo: Artículos 8 al 20 del Texto Único Ordenado de la Ley de Tributación Municipal (Decreto Supremo N° 156-2004-EF) y modificatorias.\n" +
                    "Versión del Cálculo: v" + c.getVersionCalculo() + " | Fecha de corte: " + c.getFechaReferencia(),
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, new Color(71, 85, 105))
            );
            document.add(foot);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar hoja de liquidación predial a PDF", e);
            throw new RuntimeException("Error al generar hoja de liquidación de impuesto predial", e);
        }
    }

    private List<CalculoImpuestoPredial> obtenerCalculos(CalculoPredialFilterDto filtro) {
        Specification<CalculoImpuestoPredial> spec = CalculoPredialSpecification.conFiltro(filtro);
        return calculoRepository.findAll(spec, PageRequest.of(0, 10000, Sort.by("codigoCalculo").descending())).getContent();
    }

    private void addKeyVal(PdfPTable table, String key, String val, Font kf, Font vf) {
        PdfPCell cKey = new PdfPCell(new Phrase(key, kf));
        cKey.setBorderColor(new Color(226, 232, 240));
        cKey.setBackgroundColor(new Color(248, 250, 252));
        cKey.setPadding(3);
        table.addCell(cKey);

        PdfPCell cVal = new PdfPCell(new Phrase(val != null ? val : "-", vf));
        cVal.setBorderColor(new Color(226, 232, 240));
        cVal.setPadding(3);
        table.addCell(cVal);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, boolean isTotal) {
        Font fLabel = isTotal ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE) : FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(30, 41, 59));
        Font fVal = isTotal ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE) : FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(15, 23, 42));
        Color bg = isTotal ? new Color(30, 58, 138) : new Color(241, 245, 249);

        PdfPCell c1 = new PdfPCell(new Phrase(label, fLabel));
        c1.setBackgroundColor(bg);
        c1.setPadding(4);
        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, fVal));
        c2.setBackgroundColor(bg);
        c2.setPadding(4);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c2);
    }

    private PdfPCell newCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3);
        cell.setBorderColor(new Color(226, 232, 240));
        return cell;
    }
}
