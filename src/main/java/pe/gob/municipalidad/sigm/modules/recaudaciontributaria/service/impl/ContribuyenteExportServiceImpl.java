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
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ContribuyenteFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ContribuyenteTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ContribuyenteExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.ContribuyenteSpecification;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContribuyenteExportServiceImpl implements ContribuyenteExportService {

    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcel(ContribuyenteFilterDto filtro, boolean puedeVerDocumentosCompletos) {
        List<ContribuyenteTributario> lista = obtenerContribuyentes(filtro);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Padrón de Contribuyentes");
            sheet.setDisplayGridlines(true);

            // Estilos
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
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());

            // Título
            Row rowTitle = sheet.createRow(0);
            Cell cellTitle = rowTitle.createCell(0);
            cellTitle.setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA - PADRÓN DE CONTRIBUYENTES");
            cellTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            Row rowSubtitle = sheet.createRow(1);
            Cell cellSubtitle = rowSubtitle.createCell(0);
            cellSubtitle.setCellValue("Reporte generado el: " + LocalDateTime.now().format(FORMATO_FECHA_HORA) + " | Registros: " + lista.size());
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

            // Encabezados
            String[] headers = {"N°", "CÓDIGO", "TIPO", "DOC.", "N° DOCUMENTO", "NOMBRE O RAZÓN SOCIAL", "DOMICILIO FISCAL", "ESTADO"};
            Row headerRow = sheet.createRow(3);
            headerRow.setHeightInPoints(24);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Filas de datos
            int rowIdx = 4;
            int counter = 1;
            for (ContribuyenteTributario c : lista) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(counter++);
                row.createCell(1).setCellValue(c.getCodigoContribuyente());
                row.createCell(2).setCellValue(c.getTipoContribuyente() != null ? c.getTipoContribuyente().getDescripcion() : "");
                row.createCell(3).setCellValue(c.getTipoDocumento());

                String numDoc = puedeVerDocumentosCompletos ? c.getNumeroDocumento() : c.getNumeroDocumentoEnmascarado();
                row.createCell(4).setCellValue(numDoc);
                row.createCell(5).setCellValue(c.getNombreRazonSocial());
                row.createCell(6).setCellValue(c.getDomicilioFiscal() != null ? c.getDomicilioFiscal() : "NO DECLARADO");
                row.createCell(7).setCellValue(c.getEstado() != null ? c.getEstado().getDescripcion() : "");

                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generando Excel de contribuyentes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al exportar padrón a Excel: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarPdf(ContribuyenteFilterDto filtro, boolean puedeVerDocumentosCompletos) {
        List<ContribuyenteTributario> lista = obtenerContribuyentes(filtro);

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 25, 25);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(29, 78, 216));
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font fontTh = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font fontTd = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.BLACK);

            Paragraph pTitulo = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA - RECAUDACIÓN TRIBUTARIA", fontTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);

            Paragraph pSub = new Paragraph("PADRÓN OFICIAL DE CONTRIBUYENTES MUNICIPALES | Generado: " +
                    LocalDateTime.now().format(FORMATO_FECHA_HORA) + " | Total: " + lista.size() + " registros", fontSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(12);
            document.add(pSub);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 1.6f, 0.9f, 1.4f, 3.5f, 2.5f, 1.2f});

            String[] ths = {"CÓDIGO", "TIPO", "DOC", "NÚMERO", "CONTRIBUYENTE / RAZÓN SOCIAL", "DOMICILIO FISCAL", "ESTADO"};
            for (String th : ths) {
                PdfPCell cell = new PdfPCell(new Phrase(th, fontTh));
                cell.setBackgroundColor(new Color(30, 41, 59));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (ContribuyenteTributario c : lista) {
                table.addCell(createCell(c.getCodigoContribuyente(), fontTd, Element.ALIGN_CENTER));
                table.addCell(createCell(c.getTipoContribuyente() != null ? c.getTipoContribuyente().getDescripcion() : "", fontTd, Element.ALIGN_LEFT));
                table.addCell(createCell(c.getTipoDocumento(), fontTd, Element.ALIGN_CENTER));

                String doc = puedeVerDocumentosCompletos ? c.getNumeroDocumento() : c.getNumeroDocumentoEnmascarado();
                table.addCell(createCell(doc, fontTd, Element.ALIGN_CENTER));
                table.addCell(createCell(c.getNombreRazonSocial(), fontTd, Element.ALIGN_LEFT));
                table.addCell(createCell(c.getDomicilioFiscal() != null ? c.getDomicilioFiscal() : "NO DECLARADO", fontTd, Element.ALIGN_LEFT));
                table.addCell(createCell(c.getEstado() != null ? c.getEstado().getDescripcion() : "", fontTd, Element.ALIGN_CENTER));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF de contribuyentes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al exportar padrón a PDF: " + e.getMessage(), e);
        }
    }

    private List<ContribuyenteTributario> obtenerContribuyentes(ContribuyenteFilterDto filtro) {
        Specification<ContribuyenteTributario> spec = ContribuyenteSpecification.conFiltros(
                filtro != null ? filtro.getQuery() : null,
                filtro != null ? filtro.getTipo() : null,
                filtro != null ? filtro.getEstado() : null,
                filtro != null ? filtro.getFechaDesde() : null,
                filtro != null ? filtro.getFechaHasta() : null,
                filtro != null ? filtro.getOficinaId() : null);

        return contribuyenteRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "codigoContribuyente"));
    }

    private PdfPCell createCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        return cell;
    }
}
