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
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.PredioFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PredioTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PredioTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PredioExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.PredioSpecification;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredioExportServiceImpl implements PredioExportService {

    private final PredioTributarioRepository predioRepository;
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcel(PredioFilterDto filtro) {
        List<PredioTributario> lista = obtenerPredios(filtro);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Padrón de Predios");
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

            CellStyle numStyle = workbook.createCellStyle();
            numStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            numStyle.setAlignment(HorizontalAlignment.RIGHT);
            numStyle.setBorderBottom(BorderStyle.THIN);
            numStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());

            // Título
            Row rowTitle = sheet.createRow(0);
            Cell cellTitle = rowTitle.createCell(0);
            cellTitle.setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA - PADRÓN DE PREDIOS MUNICIPALES");
            cellTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            Row rowSubtitle = sheet.createRow(1);
            Cell cellSubtitle = rowSubtitle.createCell(0);
            cellSubtitle.setCellValue("Reporte generado el: " + LocalDateTime.now().format(FORMATO_FECHA_HORA) + " | Total registros: " + lista.size());
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 9));

            // Encabezados
            String[] headers = {
                    "N°", "CÓDIGO", "CÓD. CATASTRAL", "CLASIFICACIÓN", "USO",
                    "DIRECCIÓN / UBICACIÓN", "SECTOR / ZONA", "ÁREA TERRENO (M²)", "ÁREA CONST. (M²)", "ESTADO"
            };
            Row headerRow = sheet.createRow(3);
            headerRow.setHeightInPoints(24);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Filas
            int rowIdx = 4;
            int counter = 1;
            for (PredioTributario p : lista) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(counter++);
                row.getCell(0).setCellStyle(dataStyle);

                row.createCell(1).setCellValue(p.getCodigoPredio() != null ? p.getCodigoPredio() : "-");
                row.getCell(1).setCellStyle(dataStyle);

                row.createCell(2).setCellValue(p.getCodigoCatastral() != null ? p.getCodigoCatastral() : "-");
                row.getCell(2).setCellStyle(dataStyle);

                row.createCell(3).setCellValue(p.getClasificacionPredio() != null ? p.getClasificacionPredio().name() : (p.getTipoPredio() != null ? p.getTipoPredio().name() : "-"));
                row.getCell(3).setCellStyle(dataStyle);

                row.createCell(4).setCellValue(p.getUsoDeclarado() != null ? p.getUsoDeclarado().getNombre() : "-");
                row.getCell(4).setCellStyle(dataStyle);

                row.createCell(5).setCellValue(formatearDireccion(p));
                row.getCell(5).setCellStyle(dataStyle);

                row.createCell(6).setCellValue(formatearSectorZona(p));
                row.getCell(6).setCellStyle(dataStyle);

                BigDecimal at = p.getAreaTerrenoM2() != null ? p.getAreaTerrenoM2() : (p.getAreaTerreno() != null ? p.getAreaTerreno() : BigDecimal.ZERO);
                Cell cAreaT = row.createCell(7);
                cAreaT.setCellValue(at.doubleValue());
                cAreaT.setCellStyle(numStyle);

                BigDecimal ac = p.getAreaConstruida() != null ? p.getAreaConstruida() : BigDecimal.ZERO;
                Cell cAreaC = row.createCell(8);
                cAreaC.setCellValue(ac.doubleValue());
                cAreaC.setCellStyle(numStyle);

                row.createCell(9).setCellValue(p.getEstado() != null ? p.getEstado().name() : "-");
                row.getCell(9).setCellStyle(dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar padrón de predios a Excel", e);
            throw new RuntimeException("Error al generar el archivo Excel de predios", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarPdf(PredioFilterDto filtro) {
        List<PredioTributario> lista = obtenerPredios(filtro);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 25, 25);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Tipografías
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(16, 24, 39));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(100, 116, 139));
            Font headerTableFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(30, 41, 59));

            // Encabezado
            Paragraph title = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subTitle = new Paragraph("PADRÓN DE REGISTRO Y ADMINISTRACIÓN DE PREDIOS MUNICIPALES", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 58, 138)));
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);

            Paragraph meta = new Paragraph("Generado el: " + LocalDateTime.now().format(FORMATO_FECHA_HORA) + " | Registros mostrados: " + lista.size(), subtitleFont);
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(12);
            document.add(meta);

            // Tabla PDF
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 7f, 7f, 7f, 8f, 22f, 12f, 7f, 7f});

            String[] pdfHeaders = {
                    "N°", "CÓDIGO", "CÓD. CATASTRAL", "CLASIFICACIÓN", "USO",
                    "DIRECCIÓN / UBICACIÓN", "SECTOR / ZONA", "ÁREA T. (M²)", "ESTADO"
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
            for (PredioTributario p : lista) {
                Color bg = (n % 2 == 0) ? new Color(248, 250, 252) : Color.WHITE;

                PdfPCell cNum = new PdfPCell(new Phrase(String.valueOf(n++), cellFont));
                cNum.setBackgroundColor(bg);
                cNum.setHorizontalAlignment(Element.ALIGN_CENTER);
                cNum.setPadding(3);
                table.addCell(cNum);

                PdfPCell cCod = new PdfPCell(new Phrase(p.getCodigoPredio() != null ? p.getCodigoPredio() : "-", cellFont));
                cCod.setBackgroundColor(bg);
                cCod.setPadding(3);
                table.addCell(cCod);

                PdfPCell cCat = new PdfPCell(new Phrase(p.getCodigoCatastral() != null ? p.getCodigoCatastral() : "-", cellFont));
                cCat.setBackgroundColor(bg);
                cCat.setPadding(3);
                table.addCell(cCat);

                String clasif = p.getClasificacionPredio() != null ? p.getClasificacionPredio().name() : (p.getTipoPredio() != null ? p.getTipoPredio().name() : "-");
                PdfPCell cClas = new PdfPCell(new Phrase(clasif, cellFont));
                cClas.setBackgroundColor(bg);
                cClas.setPadding(3);
                table.addCell(cClas);

                String uso = p.getUsoDeclarado() != null ? p.getUsoDeclarado().getNombre() : "-";
                PdfPCell cUso = new PdfPCell(new Phrase(uso, cellFont));
                cUso.setBackgroundColor(bg);
                cUso.setPadding(3);
                table.addCell(cUso);

                PdfPCell cDir = new PdfPCell(new Phrase(formatearDireccion(p), cellFont));
                cDir.setBackgroundColor(bg);
                cDir.setPadding(3);
                table.addCell(cDir);

                PdfPCell cSec = new PdfPCell(new Phrase(formatearSectorZona(p), cellFont));
                cSec.setBackgroundColor(bg);
                cSec.setPadding(3);
                table.addCell(cSec);

                BigDecimal at = p.getAreaTerrenoM2() != null ? p.getAreaTerrenoM2() : (p.getAreaTerreno() != null ? p.getAreaTerreno() : BigDecimal.ZERO);
                PdfPCell cArea = new PdfPCell(new Phrase(at.stripTrailingZeros().toPlainString(), cellFont));
                cArea.setBackgroundColor(bg);
                cArea.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cArea.setPadding(3);
                table.addCell(cArea);

                PdfPCell cEst = new PdfPCell(new Phrase(p.getEstado() != null ? p.getEstado().name() : "-", cellFont));
                cEst.setBackgroundColor(bg);
                cEst.setHorizontalAlignment(Element.ALIGN_CENTER);
                cEst.setPadding(3);
                table.addCell(cEst);
            }

            document.add(table);

            Paragraph disclaimer = new Paragraph(
                    "Nota: El registro administrativo de un predio no acredita la propiedad legal ni sustituye procedimientos registrales o catastrales oficiales.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 6, new Color(148, 163, 184))
            );
            disclaimer.setSpacingBefore(10);
            document.add(disclaimer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar padrón de predios a PDF", e);
            throw new RuntimeException("Error al generar el archivo PDF de predios", e);
        }
    }

    private List<PredioTributario> obtenerPredios(PredioFilterDto filtro) {
        Specification<PredioTributario> spec = PredioSpecification.conFiltro(filtro);
        return predioRepository.findAll(spec, PageRequest.of(0, 10000, Sort.by("codigoPredio").ascending())).getContent();
    }

    private String formatearDireccion(PredioTributario p) {
        if (p.getDireccion() != null && !p.getDireccion().isBlank()) {
            return p.getDireccion();
        }
        StringBuilder sb = new StringBuilder();
        if (p.getTipoVia() != null && !p.getTipoVia().isBlank()) sb.append(p.getTipoVia()).append(" ");
        if (p.getNombreVia() != null && !p.getNombreVia().isBlank()) sb.append(p.getNombreVia()).append(" ");
        if (p.getNumeroMunicipal() != null && !p.getNumeroMunicipal().isBlank()) sb.append("N° ").append(p.getNumeroMunicipal()).append(" ");
        if (p.getManzana() != null && !p.getManzana().isBlank()) sb.append("Mz. ").append(p.getManzana()).append(" ");
        if (p.getLote() != null && !p.getLote().isBlank()) sb.append("Lt. ").append(p.getLote()).append(" ");
        if (p.getReferenciaUbicacion() != null && !p.getReferenciaUbicacion().isBlank()) sb.append("(").append(p.getReferenciaUbicacion()).append(")");
        String result = sb.toString().trim();
        return result.isEmpty() ? "Sin dirección especificada" : result;
    }

    private String formatearSectorZona(PredioTributario p) {
        StringBuilder sb = new StringBuilder();
        if (p.getSectorNombre() != null && !p.getSectorNombre().isBlank()) sb.append("Sec: ").append(p.getSectorNombre()).append(" ");
        if (p.getZona() != null && !p.getZona().isBlank()) sb.append("Zon: ").append(p.getZona()).append(" ");
        if (p.getComunidadCentroPoblado() != null && !p.getComunidadCentroPoblado().isBlank()) sb.append("C.P: ").append(p.getComunidadCentroPoblado());
        String result = sb.toString().trim();
        return result.isEmpty() ? "-" : result;
    }
}
