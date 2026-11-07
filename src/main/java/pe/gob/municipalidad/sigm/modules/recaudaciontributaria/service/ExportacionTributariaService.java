package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.KpiDashboardTributarioDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteFilterRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteTributarioDtos.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.HistorialExportacionReporteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoExportacionReporte;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.FormatoExportacionReporte;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.HistorialExportacionReporteTributarioRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Servicio de exportación de reportes tributarios institucionales a PDF, Excel XLSX y CSV.
 * Implementa medidas de seguridad contra inyección de fórmulas, cálculo de checksums y trazabilidad completa.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportacionTributariaService {

    private final HistorialExportacionReporteTributarioRepository historialRepository;
    private final IndicadoresRecaudacionService indicadoresService;
    private final ReporteRecaudacionService recaudacionService;
    private final ReporteCarteraTributariaService carteraService;

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DecimalFormat FORMATO_MONEDA = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("es", "PE")));

    /**
     * Genera el Reporte Ejecutivo Consolidado en formato PDF.
     */
    @Transactional
    public byte[] exportarPdfConsolidado(ReporteFilterRequest filtro, Usuario usuario) {
        HistorialExportacionReporteTributario bitacora = registrarInicioExportacion("REP_CONSOLIDADO_EJECUTIVO", FormatoExportacionReporte.PDF, usuario);

        try {
            KpiDashboardTributarioDto kpis = indicadoresService.obtenerKpisDashboard();
            List<RecaudacionConceptoItemDto> conceptos = recaudacionService.obtenerRecaudacionPorConcepto(filtro);
            List<CarteraTramoItemDto> tramos = carteraService.obtenerDistribucionPorTramosAntiguedad();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            // 1. Encabezado Institucional
            com.lowagie.text.Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(16, 24, 39));
            com.lowagie.text.Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(75, 85, 99));
            com.lowagie.text.Font fontNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(17, 24, 39));
            com.lowagie.text.Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(55, 65, 81));
            com.lowagie.text.Font fontBlanco = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);

            Paragraph pTitulo = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);

            Paragraph pSub = new Paragraph("Gerencia de Administración Tributaria — Centro de Inteligencia y Reportes\nINFORME ESTADÍSTICO DE RECAUDACIÓN Y CARTERA FISCAL", fontSubtitulo);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(15);
            document.add(pSub);

            // Metadatos de la Exportación
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.addCell(crearCelda("Fecha y Hora de Corte: " + kpis.getFechaCorte().format(FORMATO_FECHA_HORA), fontNormal, false));
            metaTable.addCell(crearCelda("Generado por: " + (usuario != null ? usuario.getUsername() : "SISTEMA"), fontNormal, false));
            metaTable.addCell(crearCelda("Moneda: Soles (PEN) | Distrito: Quinua, Huamanga, Ayacucho", fontNormal, false));
            metaTable.addCell(crearCelda("Metodología: Registros de Caja y Cuentas Corrientes Validadas", fontNormal, false));
            metaTable.setSpacingAfter(15);
            document.add(metaTable);

            // 2. Tabla de Indicadores Principales (KPIs)
            Paragraph pKpi = new Paragraph("1. INDICADORES CLAVE DE DESEMPEÑO (KPIs)", fontNegrita);
            pKpi.setSpacingAfter(6);
            document.add(pKpi);

            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100);
            agregarCabecera(kpiTable, new String[]{"Concepto / Métrica", "Importe (PEN)", "Métrica / Comparativa", "Valor / Tasa"}, fontBlanco);

            agregarFila(kpiTable, new String[]{"Recaudación Confirmada Ejercicio", "S/ " + FORMATO_MONEDA.format(kpis.getRecaudacionAnioActual()), "Cumplimiento Meta Anual", kpis.getMetaDescripcion()}, fontNormal);
            agregarFila(kpiTable, new String[]{"Recaudación Mes Actual", "S/ " + FORMATO_MONEDA.format(kpis.getRecaudacionMesActual()), "Total Pagos Confirmados", String.valueOf(kpis.getTotalPagosConfirmados())}, fontNormal);
            agregarFila(kpiTable, new String[]{"Saldo Pendiente Total (Cartera)", "S/ " + FORMATO_MONEDA.format(kpis.getSaldoPendienteTotal()), "Tasa Morosidad Financiera", kpis.getTasaMorosidadFinanciera().toPlainString() + "%"}, fontNormal);
            agregarFila(kpiTable, new String[]{"Saldo Vencido", "S/ " + FORMATO_MONEDA.format(kpis.getSaldoVencido()), "Saldo Exigible Verificado", "S/ " + FORMATO_MONEDA.format(kpis.getSaldoExigible())}, fontNormal);
            agregarFila(kpiTable, new String[]{"Contribuyentes Registrados", String.valueOf(kpis.getTotalContribuyentesRegistrados()), "Predios Activos en Catastro", String.valueOf(kpis.getTotalPrediosRegistrados())}, fontNormal);
            kpiTable.setSpacingAfter(15);
            document.add(kpiTable);

            // 3. Tabla de Recaudación por Concepto
            Paragraph pCon = new Paragraph("2. RECAUDACIÓN CONFIRMADA POR CONCEPTO TRIBUTARIO", fontNegrita);
            pCon.setSpacingAfter(6);
            document.add(pCon);

            PdfPTable conTable = new PdfPTable(4);
            conTable.setWidthPercentage(100);
            agregarCabecera(conTable, new String[]{"Código", "Concepto Tributario", "Monto Recaudado (PEN)", "Participación (%)"}, fontBlanco);

            for (RecaudacionConceptoItemDto c : conceptos) {
                agregarFila(conTable, new String[]{
                        c.getConceptoCodigo(),
                        c.getConceptoNombre(),
                        "S/ " + FORMATO_MONEDA.format(c.getMontoRecaudado()),
                        c.getPorcentaje().toPlainString() + "%"
                }, fontNormal);
            }
            conTable.setSpacingAfter(15);
            document.add(conTable);

            // 4. Tabla de Cartera por Tramos de Antigüedad
            Paragraph pTra = new Paragraph("3. ESTRUCTURA DE CARTERA VENCIDA POR TRAMOS DE ANTIGÜEDAD", fontNegrita);
            pTra.setSpacingAfter(6);
            document.add(pTra);

            PdfPTable traTable = new PdfPTable(4);
            traTable.setWidthPercentage(100);
            agregarCabecera(traTable, new String[]{"Tramo Temporal", "Saldo Vencido (PEN)", "N° Obligaciones", "Participación (%)"}, fontBlanco);

            for (CarteraTramoItemDto t : tramos) {
                agregarFila(traTable, new String[]{
                        t.getTramo(),
                        "S/ " + FORMATO_MONEDA.format(t.getSaldo()),
                        String.valueOf(t.getCantidadObligaciones()),
                        t.getPorcentaje().toPlainString() + "%"
                }, fontNormal);
            }
            traTable.setSpacingAfter(20);
            document.add(traTable);

            // Nota de responsabilidad y cierre
            Paragraph nota = new Paragraph("Nota: Cifras oficiales emitidas con fines de gestión tributaria interna de la Municipalidad Distrital de Quinua. Este reporte refleja exclusivamente transacciones bancarias, de caja y cuentas corrientes validadas.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, Color.GRAY));
            nota.setAlignment(Element.ALIGN_CENTER);
            document.add(nota);

            document.close();
            byte[] bytes = baos.toByteArray();
            completarExportacion(bitacora, bytes, "Reporte_Tributario_Quinua.pdf");
            return bytes;

        } catch (Exception e) {
            marcarErrorExportacion(bitacora, e.getMessage());
            log.error("Error al generar PDF consolidado tributario", e);
            throw new BusinessException("No fue posible generar el reporte PDF: " + e.getMessage());
        }
    }

    /**
     * Genera el Reporte de Recaudación y Cartera en formato Excel XLSX con protección de fórmulas.
     */
    @Transactional
    public byte[] exportarExcelConsolidado(ReporteFilterRequest filtro, Usuario usuario) {
        HistorialExportacionReporteTributario bitacora = registrarInicioExportacion("REP_EXCEL_CONSOLIDADO", FormatoExportacionReporte.EXCEL, usuario);

        try (org.apache.poi.ss.usermodel.Workbook workbook = new XSSFWorkbook()) {
            KpiDashboardTributarioDto kpis = indicadoresService.obtenerKpisDashboard();
            List<RecaudacionConceptoItemDto> conceptos = recaudacionService.obtenerRecaudacionPorConcepto(filtro);
            List<CarteraTramoItemDto> tramos = carteraService.obtenerDistribucionPorTramosAntiguedad();

            // Estilos
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat df = workbook.createDataFormat();
            currencyStyle.setDataFormat(df.getFormat("#,##0.00"));

            // Hoja 1: Indicadores y Resumen
            Sheet sheet1 = workbook.createSheet("Resumen Ejecutivo");
            int rIdx = 0;

            org.apache.poi.ss.usermodel.Row rowTitle = sheet1.createRow(rIdx++);
            rowTitle.createCell(0).setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA - REPORTE TRIBUTARIO");

            org.apache.poi.ss.usermodel.Row rowMeta = sheet1.createRow(rIdx++);
            rowMeta.createCell(0).setCellValue("Fecha de Corte: " + kpis.getFechaCorte().format(FORMATO_FECHA_HORA));
            rIdx++;

            org.apache.poi.ss.usermodel.Row headerKpi = sheet1.createRow(rIdx++);
            headerKpi.createCell(0).setCellValue("Indicador / Métrica");
            headerKpi.createCell(1).setCellValue("Valor (PEN o Cantidad)");
            headerKpi.getCell(0).setCellStyle(headerStyle);
            headerKpi.getCell(1).setCellStyle(headerStyle);

            agregarFilaExcel(sheet1, rIdx++, "Recaudación Confirmada Anual", kpis.getRecaudacionAnioActual().doubleValue(), currencyStyle);
            agregarFilaExcel(sheet1, rIdx++, "Recaudación Mes Actual", kpis.getRecaudacionMesActual().doubleValue(), currencyStyle);
            agregarFilaExcel(sheet1, rIdx++, "Saldo Pendiente Cartera", kpis.getSaldoPendienteTotal().doubleValue(), currencyStyle);
            agregarFilaExcel(sheet1, rIdx++, "Saldo Vencido", kpis.getSaldoVencido().doubleValue(), currencyStyle);
            agregarFilaExcel(sheet1, rIdx++, "Saldo Exigible", kpis.getSaldoExigible().doubleValue(), currencyStyle);
            agregarFilaExcel(sheet1, rIdx++, "Tasa Morosidad Financiera (%)", kpis.getTasaMorosidadFinanciera().doubleValue(), null);
            agregarFilaExcel(sheet1, rIdx++, "Total Pagos Confirmados", (double) kpis.getTotalPagosConfirmados(), null);
            agregarFilaExcel(sheet1, rIdx++, "Contribuyentes Registrados", (double) kpis.getTotalContribuyentesRegistrados(), null);
            agregarFilaExcel(sheet1, rIdx++, "Predios Registrados", (double) kpis.getTotalPrediosRegistrados(), null);

            sheet1.autoSizeColumn(0);
            sheet1.autoSizeColumn(1);

            // Hoja 2: Conceptos
            Sheet sheet2 = workbook.createSheet("Recaudación por Concepto");
            int cIdx = 0;
            org.apache.poi.ss.usermodel.Row headConceptos = sheet2.createRow(cIdx++);
            String[] colsConceptos = {"Código", "Concepto Tributario", "Monto Recaudado (PEN)", "Participación (%)"};
            for (int i = 0; i < colsConceptos.length; i++) {
                Cell cell = headConceptos.createCell(i);
                cell.setCellValue(colsConceptos[i]);
                cell.setCellStyle(headerStyle);
            }

            for (RecaudacionConceptoItemDto c : conceptos) {
                org.apache.poi.ss.usermodel.Row r = sheet2.createRow(cIdx++);
                r.createCell(0).setCellValue(sanitizarFormula(c.getConceptoCodigo()));
                r.createCell(1).setCellValue(sanitizarFormula(c.getConceptoNombre()));
                Cell cellMonto = r.createCell(2);
                cellMonto.setCellValue(c.getMontoRecaudado().doubleValue());
                cellMonto.setCellStyle(currencyStyle);
                r.createCell(3).setCellValue(c.getPorcentaje().doubleValue());
            }

            for (int i = 0; i < colsConceptos.length; i++) {
                sheet2.autoSizeColumn(i);
            }

            // Hoja 3: Tramos de Cartera
            Sheet sheet3 = workbook.createSheet("Cartera por Tramos");
            int tIdx = 0;
            org.apache.poi.ss.usermodel.Row headTramos = sheet3.createRow(tIdx++);
            String[] colsTramos = {"Tramo Temporal", "Saldo Vencido (PEN)", "N° Obligaciones", "Participación (%)"};
            for (int i = 0; i < colsTramos.length; i++) {
                Cell cell = headTramos.createCell(i);
                cell.setCellValue(colsTramos[i]);
                cell.setCellStyle(headerStyle);
            }

            for (CarteraTramoItemDto t : tramos) {
                org.apache.poi.ss.usermodel.Row r = sheet3.createRow(tIdx++);
                r.createCell(0).setCellValue(sanitizarFormula(t.getTramo()));
                Cell cellSaldo = r.createCell(1);
                cellSaldo.setCellValue(t.getSaldo().doubleValue());
                cellSaldo.setCellStyle(currencyStyle);
                r.createCell(2).setCellValue(t.getCantidadObligaciones());
                r.createCell(3).setCellValue(t.getPorcentaje().doubleValue());
            }

            for (int i = 0; i < colsTramos.length; i++) {
                sheet3.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            byte[] bytes = baos.toByteArray();
            completarExportacion(bitacora, bytes, "Reporte_Tributario_Quinua.xlsx");
            return bytes;

        } catch (Exception e) {
            marcarErrorExportacion(bitacora, e.getMessage());
            log.error("Error al exportar Excel tributario", e);
            throw new BusinessException("No fue posible generar el archivo Excel: " + e.getMessage());
        }
    }

    /**
     * Prevención de inyección de fórmulas en hojas de cálculo (CSV Injection / Formula Injection).
     */
    public String sanitizarFormula(String valor) {
        if (valor == null) return "";
        String trimmed = valor.trim();
        if (trimmed.startsWith("=") || trimmed.startsWith("+") || trimmed.startsWith("-") || trimmed.startsWith("@")) {
            return "'" + trimmed;
        }
        return trimmed;
    }

    private void agregarFilaExcel(Sheet sheet, int rowIndex, String label, double value, CellStyle style) {
        org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowIndex);
        r.createCell(0).setCellValue(label);
        Cell cVal = r.createCell(1);
        cVal.setCellValue(value);
        if (style != null) {
            cVal.setCellStyle(style);
        }
    }

    private PdfPCell crearCelda(String texto, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        return cell;
    }

    private void agregarCabecera(PdfPTable table, String[] encabezados, com.lowagie.text.Font font) {
        for (String h : encabezados) {
            PdfPCell c = new PdfPCell(new Phrase(h, font));
            c.setBackgroundColor(new Color(29, 78, 216));
            c.setPadding(5);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c);
        }
    }

    private void agregarFila(PdfPTable table, String[] datos, com.lowagie.text.Font font) {
        for (String d : datos) {
            PdfPCell c = new PdfPCell(new Phrase(d != null ? d : "-", font));
            c.setPadding(4);
            table.addCell(c);
        }
    }

    private HistorialExportacionReporteTributario registrarInicioExportacion(String codigo, FormatoExportacionReporte formato, Usuario usuario) {
        HistorialExportacionReporteTributario h = HistorialExportacionReporteTributario.builder()
                .reporteCodigo(codigo)
                .formato(formato)
                .usuario(usuario)
                .estado(EstadoExportacionReporte.PENDIENTE)
                .fechaSolicitud(OffsetDateTime.now())
                .build();
        return historialRepository.save(h);
    }

    private void completarExportacion(HistorialExportacionReporteTributario h, byte[] bytes, String nombreArchivo) {
        h.setEstado(EstadoExportacionReporte.COMPLETADO);
        h.setFechaFinalizacion(OffsetDateTime.now());
        h.setTamanioBytes((long) bytes.length);
        h.setNombreArchivo(nombreArchivo);
        h.setChecksumSha256(calcularSha256(bytes));
        historialRepository.save(h);
    }

    private void marcarErrorExportacion(HistorialExportacionReporteTributario h, String error) {
        h.setEstado(EstadoExportacionReporte.FALLIDO);
        h.setFechaFinalizacion(OffsetDateTime.now());
        h.setMensajeError(error != null && error.length() > 500 ? error.substring(0, 499) : error);
        historialRepository.save(h);
    }

    private String calcularSha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "CHECKSUM_ERROR";
        }
    }
}

