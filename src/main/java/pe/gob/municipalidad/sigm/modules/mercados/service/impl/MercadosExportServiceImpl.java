package pe.gob.municipalidad.sigm.modules.mercados.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
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
import org.springframework.stereotype.Service;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.MercadoMunicipalService;
import pe.gob.municipalidad.sigm.modules.mercados.service.MercadosExportService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class MercadosExportServiceImpl implements MercadosExportService {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MercadoMunicipalService mercadoService;
    private final AuditoriaAdministrativaService auditoriaService;

    public MercadosExportServiceImpl(
            MercadoMunicipalService mercadoService,
            AuditoriaAdministrativaService auditoriaService) {
        this.mercadoService = mercadoService;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public byte[] exportarExcel(MercadoFilter filter) {
        MercadoFilter exportFilter = (filter != null) ? filter : MercadoFilter.builder().build();
        exportFilter.setPage(0);
        exportFilter.setSize(10000); // Exportar hasta 10,000 registros

        List<MercadoResponse> mercados = mercadoService.listarMercados(exportFilter).getContent();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Mercados Municipales");
            sheet.setDisplayGridlines(true);

            // Estilos
            byte[] azulOscuroRgb = new byte[]{(byte) 16, (byte) 24, (byte) 39};   // #101827
            byte[] azulPrimarioRgb = new byte[]{(byte) 29, (byte) 78, (byte) 216}; // #1d4ed8
            byte[] grisFondoRgb = new byte[]{(byte) 241, (byte) 245, (byte) 249};   // #f1f5f9

            XSSFColor colorHeader = new XSSFColor(azulPrimarioRgb, null);
            XSSFColor colorAlt = new XSSFColor(grisFondoRgb, null);

            // Título
            Row rowTitulo = sheet.createRow(0);
            Cell cellTitulo = rowTitulo.createCell(0);
            cellTitulo.setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA — REPORTE OFICIAL DE MERCADOS MUNICIPALES");
            XSSFCellStyle styleTitulo = workbook.createCellStyle();
            XSSFFont fontTitulo = workbook.createFont();
            fontTitulo.setBold(true);
            fontTitulo.setFontHeightInPoints((short) 14);
            fontTitulo.setColor(new XSSFColor(azulOscuroRgb, null));
            styleTitulo.setFont(fontTitulo);
            cellTitulo.setCellStyle(styleTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            // Metadatos
            Row rowMeta = sheet.createRow(1);
            Cell cellMeta = rowMeta.createCell(0);
            cellMeta.setCellValue("Generado el: " + LocalDateTime.now().format(FORMATO_FECHA_HORA) + " | Total registros: " + mercados.size());
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

            // Encabezados de tabla
            Row rowHeader = sheet.createRow(3);
            String[] headers = {
                    "N°", "CÓDIGO", "NOMBRE DEL MERCADO", "DIRECCIÓN", "DISTRITO",
                    "OFICINA ADMINISTRADORA", "CAPACIDAD", "PUESTOS", "ESTADO"
            };

            XSSFCellStyle styleHeader = workbook.createCellStyle();
            styleHeader.setFillForegroundColor(colorHeader);
            styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleHeader.setAlignment(HorizontalAlignment.CENTER);
            styleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
            XSSFFont fontHeader = workbook.createFont();
            fontHeader.setBold(true);
            fontHeader.setColor(IndexedColors.WHITE.getIndex());
            styleHeader.setFont(fontHeader);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = rowHeader.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(styleHeader);
            }

            // Datos
            XSSFCellStyle styleData = workbook.createCellStyle();
            styleData.setVerticalAlignment(VerticalAlignment.CENTER);

            XSSFCellStyle styleDataAlt = workbook.createCellStyle();
            styleDataAlt.setFillForegroundColor(colorAlt);
            styleDataAlt.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleDataAlt.setVerticalAlignment(VerticalAlignment.CENTER);

            int rowIdx = 4;
            int counter = 1;
            for (MercadoResponse m : mercados) {
                Row row = sheet.createRow(rowIdx++);
                boolean isAlt = (counter % 2 == 0);
                XSSFCellStyle currentStyle = isAlt ? styleDataAlt : styleData;

                createCell(row, 0, String.valueOf(counter++), currentStyle);
                createCell(row, 1, sanitizeFormula(m.getCodigo()), currentStyle);
                createCell(row, 2, sanitizeFormula(m.getNombre()), currentStyle);
                createCell(row, 3, sanitizeFormula(m.getDireccion()), currentStyle);
                createCell(row, 4, sanitizeFormula(m.getDistrito()), currentStyle);
                createCell(row, 5, sanitizeFormula(m.getOficinaAdministradoraNombre() != null ? m.getOficinaAdministradoraNombre() : "SIN ASIGNAR"), currentStyle);
                createCell(row, 6, String.valueOf(m.getCapacidadReferencial() != null ? m.getCapacidadReferencial() : 0), currentStyle);
                createCell(row, 7, String.valueOf(m.getTotalPuestos()), currentStyle);
                createCell(row, 8, m.getEstado() != null ? m.getEstado().name() : "", currentStyle);
            }

            // Auto-dimensionar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 1200, 15000));
            }

            workbook.write(baos);

            auditoriaService.registrar("EXPORTAR_MERCADOS_EXCEL", "mercados_mercados", null, "EXITO",
                    "Exportación a Excel de " + mercados.size() + " mercados municipales");

            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error al generar reporte Excel de mercados: {}", e.getMessage(), e);
            throw new RuntimeException("Error en la exportación Excel de mercados municipales", e);
        }
    }

    @Override
    public byte[] exportarPdf(MercadoFilter filter) {
        MercadoFilter exportFilter = (filter != null) ? filter : MercadoFilter.builder().build();
        exportFilter.setPage(0);
        exportFilter.setSize(10000);

        List<MercadoResponse> mercados = mercadoService.listarMercados(exportFilter).getContent();

        Document document = new Document(PageSize.A4.rotate(), 30, 30, 36, 36);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Tipografías
            Font fontHeaderOrg = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(16, 24, 39));
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 116, 139));
            Font fontColHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font fontCell = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(15, 23, 42));

            // Encabezado institucional
            Paragraph pTitulo = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontHeaderOrg);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);

            Paragraph pSub = new Paragraph("SISTEMA INTEGRAL DE GESTIÓN MUNICIPAL — PADRÓN OFICIAL DE MERCADOS MUNICIPALES", fontSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(4f);
            document.add(pSub);

            Paragraph pFecha = new Paragraph("Fecha de emisión: " + LocalDateTime.now().format(FORMATO_FECHA_HORA) + " | Registros: " + mercados.size(), fontSub);
            pFecha.setAlignment(Element.ALIGN_CENTER);
            pFecha.setSpacingAfter(14f);
            document.add(pFecha);

            // Tabla de datos
            float[] colWidths = {25f, 55f, 120f, 130f, 65f, 100f, 45f, 45f, 60f};
            PdfPTable table = new PdfPTable(colWidths);
            table.setWidthPercentage(100);

            String[] headers = {
                    "N°", "CÓDIGO", "NOMBRE DEL MERCADO", "DIRECCIÓN", "DISTRITO",
                    "OFICINA ADMINISTRADORA", "CAP.", "PUESTOS", "ESTADO"
            };

            Color colorHeaderPdf = new Color(29, 78, 216); // #1d4ed8
            Color colorAltPdf = new Color(248, 250, 252);   // #f8fafc

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontColHeader));
                cell.setBackgroundColor(colorHeaderPdf);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6f);
                table.addCell(cell);
            }

            int count = 1;
            for (MercadoResponse m : mercados) {
                Color bg = (count % 2 == 0) ? colorAltPdf : Color.WHITE;

                addCell(table, String.valueOf(count++), fontCell, bg, Element.ALIGN_CENTER);
                addCell(table, m.getCodigo(), fontCell, bg, Element.ALIGN_LEFT);
                addCell(table, m.getNombre(), fontCell, bg, Element.ALIGN_LEFT);
                addCell(table, m.getDireccion(), fontCell, bg, Element.ALIGN_LEFT);
                addCell(table, m.getDistrito(), fontCell, bg, Element.ALIGN_LEFT);
                addCell(table, m.getOficinaAdministradoraNombre() != null ? m.getOficinaAdministradoraNombre() : "SIN ASIGNAR", fontCell, bg, Element.ALIGN_LEFT);
                addCell(table, String.valueOf(m.getCapacidadReferencial() != null ? m.getCapacidadReferencial() : 0), fontCell, bg, Element.ALIGN_RIGHT);
                addCell(table, String.valueOf(m.getTotalPuestos()), fontCell, bg, Element.ALIGN_RIGHT);
                addCell(table, m.getEstado() != null ? m.getEstado().name() : "", fontCell, bg, Element.ALIGN_CENTER);
            }

            document.add(table);
            document.close();

            auditoriaService.registrar("EXPORTAR_MERCADOS_PDF", "mercados_mercados", null, "EXITO",
                    "Exportación a PDF oficial de " + mercados.size() + " mercados municipales");

            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Error al generar reporte PDF de mercados: {}", e.getMessage(), e);
            throw new RuntimeException("Error en la exportación PDF de mercados municipales", e);
        }
    }

    private void addCell(PdfPTable table, String text, Font font, Color bg, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    /**
     * Prevención estricta de Inyección de Fórmulas en archivos Excel (CSV/XLSX Formula Injection).
     * Si un valor inicia con =, +, -, @ o tab, se antepone una comilla simple.
     */
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
