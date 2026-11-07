package pe.gob.municipalidad.sigm.modules.mercados.service.impl;

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
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PuestoComercialResponse;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.service.PuestoComercialService;
import pe.gob.municipalidad.sigm.modules.mercados.service.PuestoExportService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PuestoExportServiceImpl implements PuestoExportService {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final PuestoComercialService puestoService;
    private final AuditoriaAdministrativaService auditoriaService;

    public PuestoExportServiceImpl(
            PuestoComercialService puestoService,
            AuditoriaAdministrativaService auditoriaService) {
        this.puestoService = puestoService;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public byte[] exportarExcel(UUID mercadoId, String termino, UUID sectorId, UUID pabellonId, UUID zonaId,
                                Long tipoPuestoId, EstadoFisicoPuesto estadoFisico, EstadoPuesto estado,
                                Integer nivel, BigDecimal minArea, BigDecimal maxArea) {
        List<PuestoComercialResponse> puestos = puestoService.listarPuestosPaginado(
                mercadoId, termino, sectorId, pabellonId, zonaId, tipoPuestoId, estadoFisico, estado,
                nivel, minArea, maxArea, PageRequest.of(0, 10000)).getContent();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Puestos Comerciales");
            sheet.setDisplayGridlines(true);

            byte[] azulOscuroRgb = new byte[]{(byte) 16, (byte) 24, (byte) 39};   // #101827
            byte[] azulPrimarioRgb = new byte[]{(byte) 29, (byte) 78, (byte) 216}; // #1d4ed8
            byte[] grisFondoRgb = new byte[]{(byte) 241, (byte) 245, (byte) 249};   // #f1f5f9

            XSSFColor colorHeader = new XSSFColor(azulPrimarioRgb, null);
            XSSFColor colorAlt = new XSSFColor(grisFondoRgb, null);

            // Título
            Row rowTitulo = sheet.createRow(0);
            Cell cellTitulo = rowTitulo.createCell(0);
            cellTitulo.setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA — PADRÓN OFICIAL DE PUESTOS COMERCIALES");
            XSSFCellStyle styleTitulo = workbook.createCellStyle();
            XSSFFont fontTitulo = workbook.createFont();
            fontTitulo.setBold(true);
            fontTitulo.setFontHeightInPoints((short) 13);
            fontTitulo.setColor(new XSSFColor(azulOscuroRgb, null));
            styleTitulo.setFont(fontTitulo);
            cellTitulo.setCellStyle(styleTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            // Metadatos
            Row rowSub = sheet.createRow(1);
            Cell cellSub = rowSub.createCell(0);
            cellSub.setCellValue("Reporte generado el: " + LocalDateTime.now().format(FORMATO_FECHA_HORA) + " | Total puestos: " + puestos.size());
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 9));

            // Encabezados
            Row headerRow = sheet.createRow(3);
            String[] columnas = {"N°", "Código", "Número", "Tipo", "Sector", "Pabellón", "Área (m²)", "Nivel", "Estado Físico", "Disponibilidad"};

            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(colorHeader);
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            XSSFFont fontHeader = workbook.createFont();
            fontHeader.setBold(true);
            fontHeader.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(fontHeader);

            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            XSSFCellStyle normalStyle = workbook.createCellStyle();
            normalStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            XSSFCellStyle altStyle = workbook.createCellStyle();
            altStyle.setFillForegroundColor(colorAlt);
            altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            altStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            int rowIdx = 4;
            int counter = 1;
            for (PuestoComercialResponse p : puestos) {
                Row row = sheet.createRow(rowIdx++);
                CellStyle currentStyle = (counter % 2 == 0) ? altStyle : normalStyle;

                createCell(row, 0, String.valueOf(counter++), currentStyle);
                createCell(row, 1, sanitizeFormula(p.getCodigo()), currentStyle);
                createCell(row, 2, sanitizeFormula(p.getNumero()), currentStyle);
                createCell(row, 3, sanitizeFormula(p.getTipoPuestoNombre() != null ? p.getTipoPuestoNombre() : "-"), currentStyle);
                createCell(row, 4, sanitizeFormula(p.getSectorNombre() != null ? p.getSectorNombre() : "-"), currentStyle);
                createCell(row, 5, sanitizeFormula(p.getPabellonNombre() != null ? p.getPabellonNombre() : "-"), currentStyle);
                createCell(row, 6, p.getAreaMetrosCuadrados() != null ? p.getAreaMetrosCuadrados().toString() : "0.00", currentStyle);
                createCell(row, 7, p.getNivel() != null ? p.getNivel().toString() : "1", currentStyle);
                createCell(row, 8, sanitizeFormula(p.getEstadoFisicoDescripcion() != null ? p.getEstadoFisicoDescripcion() : p.getEstadoFisico().name()), currentStyle);
                createCell(row, 9, sanitizeFormula(p.getEstadoDescripcion() != null ? p.getEstadoDescripcion() : p.getEstado().name()), currentStyle);
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            auditoriaService.registrar("EXPORTAR_PUESTOS_EXCEL", "mercados_puestos", null, "EXITO",
                    "Exportación Excel de puestos del mercado " + mercadoId + ". Registros: " + puestos.size());

            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error al exportar puestos a Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Error en la exportación Excel de puestos comerciales", e);
        }
    }

    @Override
    public byte[] exportarPdf(UUID mercadoId, String termino, UUID sectorId, UUID pabellonId, UUID zonaId,
                             Long tipoPuestoId, EstadoFisicoPuesto estadoFisico, EstadoPuesto estado,
                             Integer nivel, BigDecimal minArea, BigDecimal maxArea) {
        List<PuestoComercialResponse> puestos = puestoService.listarPuestosPaginado(
                mercadoId, termino, sectorId, pabellonId, zonaId, tipoPuestoId, estadoFisico, estado,
                nivel, minArea, maxArea, PageRequest.of(0, 10000)).getContent();

        Document document = new Document(PageSize.A4.rotate(), 30, 30, 36, 36);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontHeaderOrg = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(16, 24, 39));
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 116, 139));
            Font fontColHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font fontCell = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(15, 23, 42));

            Paragraph pTitulo = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontHeaderOrg);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);

            Paragraph pSub = new Paragraph("SISTEMA INTEGRAL DE GESTIÓN MUNICIPAL — PADRÓN OFICIAL DE PUESTOS COMERCIALES", fontSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(15);
            document.add(pSub);

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.6f, 1.3f, 1.0f, 1.5f, 1.5f, 1.5f, 1.0f, 1.3f, 1.3f});

            String[] headers = {"N°", "Código", "Número", "Tipo", "Sector", "Pabellón", "Área (m²)", "Est. Físico", "Disponibilidad"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontColHeader));
                cell.setBackgroundColor(new Color(29, 78, 216));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(5);
                table.addCell(cell);
            }

            int count = 1;
            for (PuestoComercialResponse p : puestos) {
                Color bgColor = (count % 2 == 0) ? new Color(241, 245, 249) : Color.WHITE;

                addCell(table, String.valueOf(count++), fontCell, bgColor, Element.ALIGN_CENTER);
                addCell(table, p.getCodigo(), fontCell, bgColor, Element.ALIGN_CENTER);
                addCell(table, p.getNumero(), fontCell, bgColor, Element.ALIGN_CENTER);
                addCell(table, p.getTipoPuestoNombre() != null ? p.getTipoPuestoNombre() : "-", fontCell, bgColor, Element.ALIGN_LEFT);
                addCell(table, p.getSectorNombre() != null ? p.getSectorNombre() : "-", fontCell, bgColor, Element.ALIGN_LEFT);
                addCell(table, p.getPabellonNombre() != null ? p.getPabellonNombre() : "-", fontCell, bgColor, Element.ALIGN_LEFT);
                addCell(table, p.getAreaMetrosCuadrados() != null ? p.getAreaMetrosCuadrados().toString() : "0.00", fontCell, bgColor, Element.ALIGN_RIGHT);
                addCell(table, p.getEstadoFisicoDescripcion() != null ? p.getEstadoFisicoDescripcion() : p.getEstadoFisico().name(), fontCell, bgColor, Element.ALIGN_CENTER);
                addCell(table, p.getEstadoDescripcion() != null ? p.getEstadoDescripcion() : p.getEstado().name(), fontCell, bgColor, Element.ALIGN_CENTER);
            }

            document.add(table);

            Paragraph pTotal = new Paragraph("\nTotal de puestos registrados en padrón: " + puestos.size(), fontSub);
            document.add(pTotal);

            document.close();
            auditoriaService.registrar("EXPORTAR_PUESTOS_PDF", "mercados_puestos", null, "EXITO",
                    "Exportación PDF de puestos del mercado " + mercadoId + ". Registros: " + puestos.size());

            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar puestos a PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error en la exportación PDF de puestos comerciales", e);
        }
    }

    private void addCell(PdfPTable table, String text, Font font, Color bgColor, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private String sanitizeFormula(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (!trimmed.isEmpty()) {
            char firstChar = trimmed.charAt(0);
            if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@' || firstChar == '\t' || firstChar == '\r') {
                return "'" + trimmed;
            }
        }
        return trimmed;
    }
}
