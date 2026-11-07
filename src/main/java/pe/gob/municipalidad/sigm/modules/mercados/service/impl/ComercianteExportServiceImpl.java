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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteFilter;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteResponse;
import pe.gob.municipalidad.sigm.modules.mercados.service.ComercianteConsultaService;
import pe.gob.municipalidad.sigm.modules.mercados.service.ComercianteExportService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComercianteExportServiceImpl implements ComercianteExportService {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final ComercianteConsultaService consultaService;
    private final AuditoriaAdministrativaService auditoriaService;

    @Override
    public byte[] exportarExcel(ComercianteFilter filter) {
        List<ComercianteResponse> comerciantes = consultaService.listarPaginado(
                filter, PageRequest.of(0, 10000)).getContent();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Comerciantes");
            sheet.setDisplayGridlines(true);

            // Estilos
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(new XSSFColor(new Color(29, 78, 216), new DefaultIndexedColorMap())); // #1d4ed8
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFFont headerFont = workbook.createFont();
            headerFont.setColor(new XSSFColor(Color.WHITE, new DefaultIndexedColorMap()));
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            XSSFCellStyle titleStyle = workbook.createCellStyle();
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            // Título
            Row titleRow = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA — PADRÓN DE COMERCIANTES");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            // Subtítulo
            Row subRow = sheet.createRow(1);
            subRow.createCell(0).setCellValue("Generado el: " + LocalDateTime.now().format(FORMATO_FECHA_HORA) + " | Total registros: " + comerciantes.size());

            // Cabeceras
            String[] headers = {
                    "Código", "Tipo Comerciante", "Titular / Razón Social", "Documento",
                    "Actividad Principal", "Teléfono", "Correo", "Estado", "Fecha Registro"
            };

            Row headerRow = sheet.createRow(3);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowIndex = 4;
            for (ComercianteResponse c : comerciantes) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(sanitize(c.getCodigoComerciante()));
                row.createCell(1).setCellValue(sanitize(c.getTipoComercianteDescripcion()));
                row.createCell(2).setCellValue(sanitize(c.getTitularNombre()));
                row.createCell(3).setCellValue(sanitize(c.getDocumentoEnmascarado()));
                row.createCell(4).setCellValue(sanitize(c.getActividadPrincipal()));
                row.createCell(5).setCellValue(sanitize(c.getTelefono() != null ? c.getTelefono() : "-"));
                row.createCell(6).setCellValue(sanitize(c.getCorreo() != null ? c.getCorreo() : "-"));
                row.createCell(7).setCellValue(sanitize(c.getEstadoDescripcion()));
                row.createCell(8).setCellValue(c.getFechaRegistro() != null ? c.getFechaRegistro().toString() : "-");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);

            auditoriaService.registrar("EXPORTAR_COMERCIANTES_EXCEL", "mercados_comerciantes", null, "EXITO",
                    "Exportación a Excel de padrón de comerciantes. Total: " + comerciantes.size());

            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error generando Excel de comerciantes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar archivo Excel de comerciantes", e);
        }
    }

    @Override
    public byte[] exportarPdf(ComercianteFilter filter) {
        List<ComercianteResponse> comerciantes = consultaService.listarPaginado(
                filter, PageRequest.of(0, 10000)).getContent();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 25, 25);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Tipografías
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(29, 78, 216));
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font tableBodyFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);

            // Encabezado
            Paragraph pTitle = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", titleFont);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitle);

            Paragraph pSub = new Paragraph("Subgerencia de Desarrollo Económico — Padrón General de Comerciantes Municipales", subTitleFont);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(4);
            document.add(pSub);

            Paragraph pMeta = new Paragraph("Fecha de emisión: " + LocalDateTime.now().format(FORMATO_FECHA_HORA) + " | Registros: " + comerciantes.size(),
                    FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY));
            pMeta.setAlignment(Element.ALIGN_RIGHT);
            pMeta.setSpacingAfter(10);
            document.add(pMeta);

            // Tabla
            float[] widths = {12f, 13f, 25f, 12f, 18f, 10f, 10f};
            PdfPTable table = new PdfPTable(widths);
            table.setWidthPercentage(100);

            String[] colHeaders = {"Código", "Modalidad", "Titular / Razón Social", "Documento", "Actividad Principal", "Estado", "Registro"};
            for (String ch : colHeaders) {
                PdfPCell cell = new PdfPCell(new Phrase(ch, tableHeaderFont));
                cell.setBackgroundColor(new Color(30, 41, 59)); // #1e293b
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (ComercianteResponse c : comerciantes) {
                table.addCell(new PdfPCell(new Phrase(c.getCodigoComerciante(), tableBodyFont)));
                table.addCell(new PdfPCell(new Phrase(c.getTipoComercianteDescripcion(), tableBodyFont)));
                table.addCell(new PdfPCell(new Phrase(c.getTitularNombre(), tableBodyFont)));
                table.addCell(new PdfPCell(new Phrase(c.getDocumentoEnmascarado(), tableBodyFont)));
                table.addCell(new PdfPCell(new Phrase(c.getActividadPrincipal(), tableBodyFont)));
                table.addCell(new PdfPCell(new Phrase(c.getEstadoDescripcion(), tableBodyFont)));
                table.addCell(new PdfPCell(new Phrase(c.getFechaRegistro() != null ? c.getFechaRegistro().toString() : "-", tableBodyFont)));
            }

            document.add(table);
            document.close();

            auditoriaService.registrar("EXPORTAR_COMERCIANTES_PDF", "mercados_comerciantes", null, "EXITO",
                    "Exportación a PDF de padrón de comerciantes. Total: " + comerciantes.size());

            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF de comerciantes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar archivo PDF de comerciantes", e);
        }
    }

    /**
     * Previene la inyección de fórmulas en hojas de cálculo escapando caracteres de control.
     */
    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        String clean = value.trim();
        if (clean.startsWith("=") || clean.startsWith("+") || clean.startsWith("-") || clean.startsWith("@")) {
            return "'" + clean;
        }
        return clean;
    }
}
