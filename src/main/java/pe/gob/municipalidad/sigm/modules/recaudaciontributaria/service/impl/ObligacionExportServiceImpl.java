package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import com.lowagie.text.*;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.CuentaCorrienteDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.MovimientoResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.ObligacionDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.ObligacionFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CuentaCorrienteTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuentaCorriente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.CuentaCorrienteTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ObligacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CuentaCorrienteTributariaService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ObligacionExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ObligacionTributariaService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.CuentaCorrienteSpecification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.ObligacionSpecification;

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
public class ObligacionExportServiceImpl implements ObligacionExportService {

    private final ObligacionTributariaRepository obligacionRepository;
    private final CuentaCorrienteTributariaRepository cuentaRepository;
    private final ObligacionTributariaService obligacionService;
    private final CuentaCorrienteTributariaService cuentaCorrienteService;

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarPadronObligacionesExcel(ObligacionFilterDto filter) {
        List<ObligacionTributaria> obligaciones = obligacionRepository.findAll(
                ObligacionSpecification.conFiltros(filter),
                Sort.by(Sort.Direction.DESC, "fechaRegistro")
        );

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Obligaciones Tributarias");
            sheet.setDisplayGridlines(true);

            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 13);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle moneyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            moneyStyle.setDataFormat(format.getFormat("S/ #,##0.00"));
            moneyStyle.setAlignment(HorizontalAlignment.RIGHT);

            // Título
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell tCell = titleRow.createCell(0);
            tCell.setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA - PADRÓN DE OBLIGACIONES TRIBUTARIAS");
            tCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));

            org.apache.poi.ss.usermodel.Row subRow = sheet.createRow(1);
            subRow.createCell(0).setCellValue("Generado el: " + LocalDateTime.now().format(FORMATO_FECHA_HORA));

            // Encabezados
            String[] headers = {
                    "Código Obligación", "Contribuyente", "Documento", "Concepto", "Predio",
                    "Ejercicio", "Cuota", "Imp. Determinado", "Imp. Pagado", "Saldo Deudor",
                    "Vencimiento", "Estado Adm.", "Exigibilidad"
            };

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(3);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            BigDecimal totalDet = BigDecimal.ZERO;
            BigDecimal totalSaldo = BigDecimal.ZERO;

            for (ObligacionTributaria o : obligaciones) {
                org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(sanitizar(o.getCodigoObligacion()));
                r.createCell(1).setCellValue(sanitizar(o.getContribuyente().getNombreRazonSocial()));
                r.createCell(2).setCellValue(sanitizar(o.getContribuyente().getTipoDocumento() + ": " + o.getContribuyente().getNumeroDocumento()));
                r.createCell(3).setCellValue(sanitizar(o.getConcepto().getNombre()));
                r.createCell(4).setCellValue(o.getPredio() != null ? sanitizar(o.getPredio().getCodigoPredio()) : "N/A");
                r.createCell(5).setCellValue(o.getPeriodo().getAnio());
                r.createCell(6).setCellValue(o.getCuotaNumero() + " de " + o.getTotalCuotas());

                org.apache.poi.ss.usermodel.Cell cDet = r.createCell(7);
                cDet.setCellValue(o.getImporteDeterminado().doubleValue());
                cDet.setCellStyle(moneyStyle);
                totalDet = totalDet.add(o.getImporteDeterminado());

                org.apache.poi.ss.usermodel.Cell cPag = r.createCell(8);
                cPag.setCellValue(o.getImportePagado().doubleValue());
                cPag.setCellStyle(moneyStyle);

                org.apache.poi.ss.usermodel.Cell cSal = r.createCell(9);
                cSal.setCellValue(o.getSaldo().doubleValue());
                cSal.setCellStyle(moneyStyle);
                totalSaldo = totalSaldo.add(o.getSaldo());

                r.createCell(10).setCellValue(o.getFechaVencimiento() != null ? o.getFechaVencimiento().format(FORMATO_FECHA) : "N/A");
                r.createCell(11).setCellValue(o.getEstado().getDescripcion());
                r.createCell(12).setCellValue(o.getEstadoExigibilidad().getDescripcion());
            }

            // Fila de Totales
            org.apache.poi.ss.usermodel.Row totalRow = sheet.createRow(rowIdx);
            totalRow.createCell(0).setCellValue("TOTALES:");
            org.apache.poi.ss.usermodel.Cell totDetCell = totalRow.createCell(7);
            totDetCell.setCellValue(totalDet.doubleValue());
            totDetCell.setCellStyle(moneyStyle);

            org.apache.poi.ss.usermodel.Cell totSalCell = totalRow.createCell(9);
            totSalCell.setCellValue(totalSaldo.doubleValue());
            totSalCell.setCellStyle(moneyStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar padrón de obligaciones a Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar archivo Excel de obligaciones: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarPadronObligacionesPdf(ObligacionFilterDto filter) {
        List<ObligacionTributaria> obligaciones = obligacionRepository.findAll(
                ObligacionSpecification.conFiltros(filter),
                Sort.by(Sort.Direction.DESC, "fechaRegistro")
        );

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 25, 25);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(29, 78, 216));
            com.lowagie.text.Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
            com.lowagie.text.Font tableHeadFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            com.lowagie.text.Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.BLACK);

            Paragraph pTitle = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", titleFont);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            doc.add(pTitle);

            Paragraph pSub = new Paragraph("PADRÓN INSTITUCIONAL DE OBLIGACIONES TRIBUTARIAS MUNICIPALES\nGenerado: " +
                    LocalDateTime.now().format(FORMATO_FECHA_HORA) + "\n\n", subFont);
            pSub.setAlignment(Element.ALIGN_CENTER);
            doc.add(pSub);

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 2.0f, 3.0f, 1.8f, 2.0f, 1.0f, 1.5f, 1.5f, 1.8f, 1.8f });

            String[] heads = { "Código", "Contribuyente", "Documento", "Concepto", "Año", "Determinado", "Saldo", "Estado", "Exigibilidad" };
            for (String h : heads) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeadFont));
                cell.setBackgroundColor(new Color(30, 41, 59));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(4);
                table.addCell(cell);
            }

            for (ObligacionTributaria o : obligaciones) {
                table.addCell(new Phrase(o.getCodigoObligacion(), rowFont));
                table.addCell(new Phrase(o.getContribuyente().getNombreRazonSocial(), rowFont));
                table.addCell(new Phrase(o.getContribuyente().getNumeroDocumento(), rowFont));
                table.addCell(new Phrase(o.getConcepto().getNombre(), rowFont));
                table.addCell(new Phrase(String.valueOf(o.getPeriodo().getAnio()), rowFont));
                table.addCell(new Phrase("S/ " + o.getImporteDeterminado().toString(), rowFont));
                table.addCell(new Phrase("S/ " + o.getSaldo().toString(), rowFont));
                table.addCell(new Phrase(o.getEstado().getDescripcion(), rowFont));
                table.addCell(new Phrase(o.getEstadoExigibilidad().getDescripcion(), rowFont));
            }

            doc.add(table);

            Paragraph footer = new Paragraph("\nNota Institucional: Este padrón refleja las obligaciones tributarias vigentes y registradas en el SIGM-QUINUA. No sustituye resoluciones tributarias oficiales.", subFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF de padrón de obligaciones: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar PDF de obligaciones: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarFichaObligacionPdf(UUID obligacionId) {
        ObligacionDetailResponse o = obligacionService.obtenerPorId(obligacionId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 25, 25, 30, 30);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, new Color(29, 78, 216));
            com.lowagie.text.Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            com.lowagie.text.Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(30, 41, 59));
            com.lowagie.text.Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
            com.lowagie.text.Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);

            Paragraph title = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph sub = new Paragraph("FICHA TÉCNICA DE OBLIGACIÓN TRIBUTARIA MUNICIPAL\nCódigo: " + o.getCodigoObligacion() + "\n\n", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            doc.add(sub);

            // Información General
            doc.add(new Paragraph("1. INFORMACIÓN DEL CONTRIBUYENTE Y CONCEPTO", sectionFont));
            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setSpacingBefore(6);
            info.setSpacingAfter(10);

            info.addCell(new Phrase("Contribuyente:", labelFont));
            info.addCell(new Phrase(o.getContribuyenteNombre() + " (" + o.getContribuyenteCodigo() + ")", valueFont));

            info.addCell(new Phrase("Documento de Identidad:", labelFont));
            info.addCell(new Phrase(o.getContribuyenteDocumento(), valueFont));

            info.addCell(new Phrase("Concepto Tributario:", labelFont));
            info.addCell(new Phrase(o.getConceptoNombre() + " (" + o.getConceptoCodigo() + ")", valueFont));

            info.addCell(new Phrase("Ejercicio Fiscal / Período:", labelFont));
            info.addCell(new Phrase(String.valueOf(o.getPeriodoAnio()), valueFont));

            info.addCell(new Phrase("Predio Asociado:", labelFont));
            info.addCell(new Phrase(o.getPredioCodigo() + " - " + o.getPredioDireccion(), valueFont));

            info.addCell(new Phrase("Importe Determinado:", labelFont));
            info.addCell(new Phrase("S/ " + o.getImporteDeterminado().toString(), labelFont));

            info.addCell(new Phrase("Saldo Pendiente Actual:", labelFont));
            info.addCell(new Phrase("S/ " + o.getSaldo().toString(), labelFont));

            info.addCell(new Phrase("Estado Administrativo:", labelFont));
            info.addCell(new Phrase(o.getEstado().getDescripcion(), valueFont));

            info.addCell(new Phrase("Exigibilidad Jurídica:", labelFont));
            info.addCell(new Phrase(o.getEstadoExigibilidad().getDescripcion(), valueFont));

            doc.add(info);

            // Cuotas
            if (o.getCuotas() != null && !o.getCuotas().isEmpty()) {
                doc.add(new Paragraph("2. CALENDARIO DE CUOTAS Y VENCIMIENTOS", sectionFont));
                PdfPTable cTable = new PdfPTable(5);
                cTable.setWidthPercentage(100);
                cTable.setSpacingBefore(6);
                cTable.setSpacingAfter(10);

                String[] cHeads = { "N° Cuota", "Importe", "Pagado", "Saldo", "Vencimiento" };
                for (String h : cHeads) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
                    cell.setBackgroundColor(new Color(30, 41, 59));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cTable.addCell(cell);
                }

                for (var cuota : o.getCuotas()) {
                    cTable.addCell(new Phrase(String.valueOf(cuota.getNumeroCuota()), valueFont));
                    cTable.addCell(new Phrase("S/ " + cuota.getImporte().toString(), valueFont));
                    cTable.addCell(new Phrase("S/ " + cuota.getImportePagado().toString(), valueFont));
                    cTable.addCell(new Phrase("S/ " + cuota.getSaldo().toString(), valueFont));
                    cTable.addCell(new Phrase(cuota.getFechaVencimiento() != null ? cuota.getFechaVencimiento().format(FORMATO_FECHA) : "N/A", valueFont));
                }
                doc.add(cTable);
            }

            Paragraph disclaimer = new Paragraph("\nDocumento de trazabilidad interna emitido por la Subgerencia de Administración Tributaria y Rentas.\n", subFont);
            disclaimer.setAlignment(Element.ALIGN_CENTER);
            doc.add(disclaimer);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF de ficha de obligación: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar PDF de ficha: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarEstadoCuentaPdf(UUID cuentaId) {
        CuentaCorrienteDetailResponse c = cuentaCorrienteService.obtenerPorId(cuentaId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 25, 25, 30, 30);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, new Color(29, 78, 216));
            com.lowagie.text.Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
            com.lowagie.text.Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 41, 59));
            com.lowagie.text.Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
            com.lowagie.text.Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            com.lowagie.text.Font thFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.WHITE);
            com.lowagie.text.Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.BLACK);

            Paragraph title = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph sub = new Paragraph("ESTADO DE CUENTA CORRIENTE TRIBUTARIA\nCuenta N°: " + c.getCodigoCuenta() +
                    " | Emisión: " + LocalDateTime.now().format(FORMATO_FECHA_HORA) + "\n\n", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            doc.add(sub);

            // Resumen del Contribuyente
            PdfPTable contrib = new PdfPTable(2);
            contrib.setWidthPercentage(100);
            contrib.setSpacingAfter(8);

            contrib.addCell(new Phrase("Contribuyente:", labelFont));
            contrib.addCell(new Phrase(c.getContribuyenteNombre() + " (" + c.getContribuyenteCodigo() + ")", valueFont));

            contrib.addCell(new Phrase("Documento:", labelFont));
            contrib.addCell(new Phrase(c.getContribuyenteDocumento(), valueFont));

            contrib.addCell(new Phrase("Domicilio Fiscal:", labelFont));
            contrib.addCell(new Phrase(c.getDomicilioFiscal(), valueFont));

            contrib.addCell(new Phrase("Saldo Total Deudor Exigible:", labelFont));
            contrib.addCell(new Phrase("S/ " + c.getSaldoActual().toString(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(225, 29, 72))));

            doc.add(contrib);

            // Tabla de Movimientos
            doc.add(new Paragraph("HISTORIAL DE MOVIMIENTOS CONTABLES", sectionFont));
            PdfPTable mTable = new PdfPTable(6);
            mTable.setWidthPercentage(100);
            mTable.setWidths(new float[] { 1.2f, 1.5f, 3.5f, 1.4f, 1.4f, 1.4f });
            mTable.setSpacingBefore(4);
            mTable.setSpacingAfter(10);

            String[] mHeads = { "Fecha", "Secuencia", "Descripción / Operación", "Débito (+)", "Crédito (-)", "Saldo" };
            for (String h : mHeads) {
                PdfPCell cell = new PdfPCell(new Phrase(h, thFont));
                cell.setBackgroundColor(new Color(30, 41, 59));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(3);
                mTable.addCell(cell);
            }

            for (MovimientoResponse m : c.getMovimientos()) {
                mTable.addCell(new Phrase(m.getFechaOperacion() != null ? m.getFechaOperacion().format(FORMATO_FECHA) : "N/A", rowFont));
                mTable.addCell(new Phrase("#" + m.getNumeroSecuencia(), rowFont));
                mTable.addCell(new Phrase(m.getDescripcion() != null ? m.getDescripcion() : m.getTipoMovimiento().getDescripcion(), rowFont));

                boolean esDebito = m.getNaturaleza() != null && m.getNaturaleza().name().equals("DEBITO");
                mTable.addCell(new Phrase(esDebito ? "S/ " + m.getImporte().toString() : "-", rowFont));
                mTable.addCell(new Phrase(!esDebito ? "S/ " + m.getImporte().toString() : "-", rowFont));
                mTable.addCell(new Phrase("S/ " + m.getSaldoPosterior().toString(), rowFont));
            }
            doc.add(mTable);

            // Deudas pendientes
            if (c.getObligacionesPendientes() != null && !c.getObligacionesPendientes().isEmpty()) {
                doc.add(new Paragraph("OBLIGACIONES PENDIENTES DE PAGO", sectionFont));
                PdfPTable dTable = new PdfPTable(5);
                dTable.setWidthPercentage(100);
                dTable.setWidths(new float[] { 2.0f, 2.5f, 1.0f, 1.5f, 1.5f });
                dTable.setSpacingBefore(4);
                dTable.setSpacingAfter(10);

                String[] dHeads = { "Código", "Concepto", "Año", "Determinado", "Saldo Pendiente" };
                for (String h : dHeads) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, thFont));
                    cell.setBackgroundColor(new Color(30, 41, 59));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(3);
                    dTable.addCell(cell);
                }

                for (var d : c.getObligacionesPendientes()) {
                    dTable.addCell(new Phrase(d.getCodigoObligacion(), rowFont));
                    dTable.addCell(new Phrase(d.getConceptoNombre(), rowFont));
                    dTable.addCell(new Phrase(String.valueOf(d.getPeriodoAnio()), rowFont));
                    dTable.addCell(new Phrase("S/ " + d.getImporteDeterminado().toString(), rowFont));
                    dTable.addCell(new Phrase("S/ " + d.getSaldo().toString(), rowFont));
                }
                doc.add(dTable);
            }

            Paragraph disclaimer = new Paragraph("\nAVISO LEGAL: El presente estado de cuenta constituye un informe administrativo de carácter informativo y contable.\nNo sustituye a las Órdenes de Pago ni a las Resoluciones de Determinación formalmente notificadas conforme al Código Tributario.", subFont);
            disclaimer.setAlignment(Element.ALIGN_CENTER);
            doc.add(disclaimer);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF de estado de cuenta: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar PDF de estado de cuenta: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarCuentasCorrientesExcel(String query, EstadoCuentaCorriente estado, Boolean conDeuda) {
        List<CuentaCorrienteTributaria> cuentas = cuentaRepository.findAll(
                CuentaCorrienteSpecification.conFiltros(query, estado, conDeuda),
                Sort.by(Sort.Direction.ASC, "codigoCuenta")
        );

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Cuentas Corrientes");
            sheet.setDisplayGridlines(true);

            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 13);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle moneyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            moneyStyle.setDataFormat(format.getFormat("S/ #,##0.00"));
            moneyStyle.setAlignment(HorizontalAlignment.RIGHT);

            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA - PADRÓN DE CUENTAS CORRIENTES");
            titleRow.getCell(0).setCellStyle(titleStyle);

            org.apache.poi.ss.usermodel.Row subRow = sheet.createRow(1);
            subRow.createCell(0).setCellValue("Generado el: " + LocalDateTime.now().format(FORMATO_FECHA_HORA));

            String[] headers = { "Código Cuenta", "Contribuyente", "Documento", "Moneda", "Total Cargos (+)", "Total Abonos (-)", "Saldo Actual", "Estado", "Apertura" };
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(3);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            BigDecimal totalSaldo = BigDecimal.ZERO;

            for (CuentaCorrienteTributaria c : cuentas) {
                org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(sanitizar(c.getCodigoCuenta()));
                r.createCell(1).setCellValue(sanitizar(c.getContribuyente().getNombreRazonSocial()));
                r.createCell(2).setCellValue(sanitizar(c.getContribuyente().getTipoDocumento() + ": " + c.getContribuyente().getNumeroDocumento()));
                r.createCell(3).setCellValue(c.getMoneda());

                org.apache.poi.ss.usermodel.Cell cCargos = r.createCell(4);
                cCargos.setCellValue(c.getTotalCargos().doubleValue());
                cCargos.setCellStyle(moneyStyle);

                org.apache.poi.ss.usermodel.Cell cAbonos = r.createCell(5);
                cAbonos.setCellValue(c.getTotalAbonos().doubleValue());
                cAbonos.setCellStyle(moneyStyle);

                org.apache.poi.ss.usermodel.Cell cSaldo = r.createCell(6);
                cSaldo.setCellValue(c.getSaldoActual().doubleValue());
                cSaldo.setCellStyle(moneyStyle);
                totalSaldo = totalSaldo.add(c.getSaldoActual());

                r.createCell(7).setCellValue(c.getEstado().getDescripcion());
                r.createCell(8).setCellValue(c.getFechaApertura().format(FORMATO_FECHA));
            }

            org.apache.poi.ss.usermodel.Row totalRow = sheet.createRow(rowIdx);
            totalRow.createCell(0).setCellValue("TOTAL SALDO DEUDOR CONSOLIDADO:");
            org.apache.poi.ss.usermodel.Cell totCell = totalRow.createCell(6);
            totCell.setCellValue(totalSaldo.doubleValue());
            totCell.setCellStyle(moneyStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar cuentas corrientes a Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Error al exportar cuentas corrientes a Excel: " + e.getMessage());
        }
    }

    private String sanitizar(String valor) {
        if (valor == null) return "";
        if (valor.startsWith("=") || valor.startsWith("+") || valor.startsWith("-") || valor.startsWith("@")) {
            return "'" + valor;
        }
        return valor;
    }
}
