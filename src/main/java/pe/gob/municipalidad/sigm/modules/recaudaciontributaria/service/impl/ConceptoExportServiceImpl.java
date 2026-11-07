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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ConceptoFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ConceptoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ConfiguracionConceptoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ConceptoExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.ConceptoSpecification;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConceptoExportServiceImpl implements ConceptoExportService {

    private final ConceptoTributarioRepository conceptoRepository;
    private final ConfiguracionConceptoTributarioRepository configuracionRepository;
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcel(ConceptoFilterDto filtro) {
        List<ConceptoTributario> conceptos = conceptoRepository.findAll(
                ConceptoSpecification.conFiltro(filtro),
                Sort.by(Sort.Direction.ASC, "codigo")
        );

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Conceptos Tributarios");
            sheet.setDisplayGridlines(true);

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
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFont(titleFont);

            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setBorderBottom(BorderStyle.THIN);
            textStyle.setBorderTop(BorderStyle.THIN);
            textStyle.setBorderLeft(BorderStyle.THIN);
            textStyle.setBorderRight(BorderStyle.THIN);

            // Título
            Row r0 = sheet.createRow(0);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA — RECAUDACIÓN TRIBUTARIA MUNICIPAL");
            c0.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Catálogo de Conceptos Tributarios, Tasas, Arbitrios y Parámetros");

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Fecha de exportación: " + LocalDateTime.now().format(FORMATO_FECHA_HORA));

            // Encabezados
            String[] headers = {
                    "N°", "Código", "Denominación del Concepto", "Naturaleza Jurídica",
                    "Tipo de Concepto", "Periodicidad", "Afecta Predio", "Estado", "Inicio Vigencia"
            };

            Row headerRow = sheet.createRow(4);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowIdx = 5;
            int num = 1;
            for (ConceptoTributario c : conceptos) {
                Row row = sheet.createRow(rowIdx++);
                crearCeldaSegura(row, 0, String.valueOf(num++), textStyle);
                crearCeldaSegura(row, 1, c.getCodigo(), textStyle);
                crearCeldaSegura(row, 2, c.getNombre(), textStyle);
                crearCeldaSegura(row, 3, c.getNaturalezaTributaria() != null ? c.getNaturalezaTributaria().getDescripcion() : "IMPUESTO", textStyle);
                crearCeldaSegura(row, 4, c.getTipoConcepto() != null ? c.getTipoConcepto().getDescripcion() : "", textStyle);
                crearCeldaSegura(row, 5, c.getPeriodicidad() != null ? c.getPeriodicidad().getDescripcion() : "", textStyle);
                crearCeldaSegura(row, 6, Boolean.TRUE.equals(c.getAplicaAPredio()) ? "SÍ" : "NO", textStyle);
                crearCeldaSegura(row, 7, c.getEstado() != null ? c.getEstado().getDescripcion() : "ACTIVO", textStyle);
                crearCeldaSegura(row, 8, c.getFechaInicioVigencia() != null ? c.getFechaInicioVigencia().toString() : "-", textStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i) + 1200, 3000));
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar reporte Excel de conceptos tributarios: {}", e.getMessage(), e);
            throw new RuntimeException("Error al exportar catálogo de conceptos en formato Excel", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarPdf(ConceptoFilterDto filtro) {
        List<ConceptoTributario> conceptos = conceptoRepository.findAll(
                ConceptoSpecification.conFiltro(filtro),
                Sort.by(Sort.Direction.ASC, "codigo")
        );

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 25, 25, 30, 30);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(15, 23, 42));
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font fontTh = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font fontTd = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.DARK_GRAY);

            Paragraph title = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA — HUAMANGA, AYACUCHO", fontTitulo);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Catálogo Institucional de Conceptos Tributarios, Tasas y Arbitrios\nGenerado: " + LocalDateTime.now().format(FORMATO_FECHA_HORA) + "\n\n", fontSub);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.8f, 4.0f, 2.5f, 2.5f, 1.8f, 1.2f, 1.8f, 1.8f});

            String[] ths = {"Código", "Concepto Tributario", "Naturaleza", "Tipo Concepto", "Periodicidad", "Afecta", "Estado", "Vigencia"};
            for (String h : ths) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontTh));
                cell.setBackgroundColor(new Color(30, 41, 59));
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (ConceptoTributario c : conceptos) {
                table.addCell(new PdfPCell(new Phrase(c.getCodigo(), fontTd)));
                table.addCell(new PdfPCell(new Phrase(c.getNombre(), fontTd)));
                table.addCell(new PdfPCell(new Phrase(c.getNaturalezaTributaria() != null ? c.getNaturalezaTributaria().getDescripcion() : "IMPUESTO", fontTd)));
                table.addCell(new PdfPCell(new Phrase(c.getTipoConcepto() != null ? c.getTipoConcepto().getDescripcion() : "", fontTd)));
                table.addCell(new PdfPCell(new Phrase(c.getPeriodicidad() != null ? c.getPeriodicidad().getDescripcion() : "", fontTd)));
                table.addCell(new PdfPCell(new Phrase(Boolean.TRUE.equals(c.getAplicaAPredio()) ? "SÍ" : "NO", fontTd)));
                table.addCell(new PdfPCell(new Phrase(c.getEstado() != null ? c.getEstado().getDescripcion() : "ACTIVO", fontTd)));
                table.addCell(new PdfPCell(new Phrase(c.getFechaInicioVigencia() != null ? c.getFechaInicioVigencia().toString() : "-", fontTd)));
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF de conceptos tributarios: {}", e.getMessage(), e);
            throw new RuntimeException("Error al exportar conceptos en formato PDF", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarFichaConceptoPdf(UUID conceptoId) {
        ConceptoTributario c = conceptoRepository.findById(conceptoId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el concepto tributario ID: " + conceptoId));

        List<ConfiguracionConceptoTributario> configuraciones = configuracionRepository.findByConceptoIdOrderByNumeroVersionDesc(c.getId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 30, 30, 30, 30);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(15, 23, 42));
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 41, 59));
            Font fontTd = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.DARK_GRAY);
            Font fontTh = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.WHITE);

            Paragraph title = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph sub = new Paragraph("FICHA TÉCNICA Y NORMATIVA DE CONCEPTO TRIBUTARIO\n\n", fontSub);
            sub.setAlignment(Element.ALIGN_CENTER);
            document.add(sub);

            PdfPTable datos = new PdfPTable(2);
            datos.setWidthPercentage(100);
            datos.setWidths(new float[]{1.5f, 3.5f});

            agregarFilaInfo(datos, "Código:", c.getCodigo(), fontSub, fontTd);
            agregarFilaInfo(datos, "Denominación:", c.getNombre(), fontSub, fontTd);
            agregarFilaInfo(datos, "Descripción:", c.getDescripcion() != null ? c.getDescripcion() : "Sin descripción", fontSub, fontTd);
            agregarFilaInfo(datos, "Naturaleza Jurídica:", c.getNaturalezaTributaria() != null ? c.getNaturalezaTributaria().getDescripcion() : "IMPUESTO", fontSub, fontTd);
            agregarFilaInfo(datos, "Tipo de Concepto:", c.getTipoConcepto() != null ? c.getTipoConcepto().getDescripcion() : "-", fontSub, fontTd);
            agregarFilaInfo(datos, "Periodicidad:", c.getPeriodicidad() != null ? c.getPeriodicidad().getDescripcion() : "ANUAL", fontSub, fontTd);
            agregarFilaInfo(datos, "Cuenta Presupuestal:", c.getCuentaPresupuestal() != null ? c.getCuentaPresupuestal() : "-", fontSub, fontTd);
            agregarFilaInfo(datos, "Vigencia:", (c.getFechaInicioVigencia() != null ? c.getFechaInicioVigencia().toString() : "-") + " hasta " + (c.getFechaFinVigencia() != null ? c.getFechaFinVigencia().toString() : "Indeterminada"), fontSub, fontTd);
            agregarFilaInfo(datos, "Norma Legal Base:", c.getNormaLegal() != null ? c.getNormaLegal().getNumeroNorma() + " - " + c.getNormaLegal().getDescripcion() : "Pendiente de vinculación", fontSub, fontTd);

            document.add(datos);

            // Versiones configuradas
            Paragraph pVers = new Paragraph("\nVERSIONES CONFIGURADAS Y PARÁMETROS VIGENTES", fontSub);
            document.add(pVers);

            PdfPTable tVers = new PdfPTable(6);
            tVers.setWidthPercentage(100);
            tVers.setWidths(new float[]{1.0f, 1.2f, 1.5f, 2.0f, 1.5f, 1.5f});

            String[] hVers = {"Versión", "Año", "Estado", "Norma Sustento", "Vigencia", "Parámetros"};
            for (String h : hVers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontTh));
                cell.setBackgroundColor(new Color(30, 41, 59));
                cell.setPadding(4);
                tVers.addCell(cell);
            }

            for (ConfiguracionConceptoTributario cfg : configuraciones) {
                tVers.addCell(new PdfPCell(new Phrase("v" + cfg.getNumeroVersion(), fontTd)));
                tVers.addCell(new PdfPCell(new Phrase(String.valueOf(cfg.getPeriodo().getAnio()), fontTd)));
                tVers.addCell(new PdfPCell(new Phrase(cfg.getEstado().getNombre(), fontTd)));
                tVers.addCell(new PdfPCell(new Phrase(cfg.getNorma() != null ? cfg.getNorma().getNumeroNorma() : "-", fontTd)));
                tVers.addCell(new PdfPCell(new Phrase(cfg.getFechaInicioVigencia().toString(), fontTd)));
                tVers.addCell(new PdfPCell(new Phrase(String.valueOf(cfg.getParametros().size()) + " regs.", fontTd)));
            }

            document.add(tVers);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar ficha técnica del concepto tributario: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar la ficha técnica en PDF", e);
        }
    }

    private void agregarFilaInfo(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setBackgroundColor(new Color(241, 245, 249));
        c1.setPadding(5);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value != null ? value : "-", valueFont));
        c2.setPadding(5);
        table.addCell(c2);
    }

    private void crearCeldaSegura(Row row, int colIdx, String valor, CellStyle style) {
        Cell cell = row.createCell(colIdx);
        if (valor == null) {
            cell.setCellValue("");
        } else {
            // Protección contra inyección de fórmulas CSV/Excel
            String v = valor.trim();
            if (v.startsWith("=") || v.startsWith("+") || v.startsWith("-") || v.startsWith("@")) {
                v = "'" + v;
            }
            cell.setCellValue(v);
        }
        cell.setCellStyle(style);
    }
}
