package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SesionCajaMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PagoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.SesionCajaMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PagoExportService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.PagoSpecification;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoExportServiceImpl implements PagoExportService {

    private final PagoTributarioRepository pagoRepository;
    private final SesionCajaMunicipalRepository sesionRepository;

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarPagosExcel(String busqueda, UUID contribuyenteId, UUID sesionCajaId,
                                    MedioPagoTributario medioPago, EstadoPagoTributario estado,
                                    OffsetDateTime fechaDesde, OffsetDateTime fechaHasta) {
        List<PagoTributario> pagos = pagoRepository.findAll(
                PagoSpecification.conFiltros(busqueda, contribuyenteId, sesionCajaId, medioPago, estado, fechaDesde, fechaHasta),
                Sort.by(Sort.Direction.DESC, "fechaHoraPago")
        );

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Recaudación Tributaria");

            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 10);

            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            XSSFCellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("S/ #,##0.00"));
            currencyStyle.setAlignment(HorizontalAlignment.RIGHT);

            // Título
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA — REPORTE DE RECAUDACIÓN Y PAGOS TRIBUTARIOS");
            titleRow.setHeightInPoints(24);

            // Subtítulo
            org.apache.poi.ss.usermodel.Row subRow = sheet.createRow(1);
            subRow.createCell(0).setCellValue("Documento de control y auditoría interna. Prohibida su alteración. Emitido el: " +
                    OffsetDateTime.now().format(FORMATO_FECHA_HORA));

            // Encabezados
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(3);
            String[] headers = {
                    "N° Operación", "Fecha y Hora", "N° Doc Contribuyente", "Contribuyente",
                    "Medio de Pago", "Caja / Ventanilla", "Importe (PEN)", "Estado", "N° Op. Bancaria / Voucher"
            };

            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            BigDecimal totalRecaudado = BigDecimal.ZERO;

            for (PagoTributario p : pagos) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getCodigoPago());
                row.createCell(1).setCellValue(p.getFechaHoraPago().format(FORMATO_FECHA_HORA));
                row.createCell(2).setCellValue(p.getContribuyente().getNumeroDocumento());
                row.createCell(3).setCellValue(p.getContribuyente().getNombreRazonSocial());
                row.createCell(4).setCellValue(p.getMedioPago().getDescripcion());
                row.createCell(5).setCellValue(p.getSesionCaja() != null ? p.getSesionCaja().getCaja().getNombre() : "Externo / Depósito");

                org.apache.poi.ss.usermodel.Cell impCell = row.createCell(6);
                impCell.setCellValue(p.getImporte().doubleValue());
                impCell.setCellStyle(currencyStyle);

                row.createCell(7).setCellValue(p.getEstado().getDescripcion());
                row.createCell(8).setCellValue(p.getNumeroOperacionBancaria() != null ? p.getNumeroOperacionBancaria() : (p.getReferenciaExterna() != null ? p.getReferenciaExterna() : "-"));

                if (p.getEstado() == EstadoPagoTributario.CONFIRMADO || p.getEstado() == EstadoPagoTributario.PROCESADO) {
                    totalRecaudado = totalRecaudado.add(p.getImporte());
                }
            }

            // Fila de Total
            org.apache.poi.ss.usermodel.Row totalRow = sheet.createRow(rowIdx + 1);
            org.apache.poi.ss.usermodel.Cell totLabel = totalRow.createCell(5);
            totLabel.setCellValue("TOTAL CONFIRMADO:");
            org.apache.poi.ss.usermodel.Cell totVal = totalRow.createCell(6);
            totVal.setCellValue(totalRecaudado.doubleValue());
            totVal.setCellStyle(currencyStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar pagos a Excel", e);
            throw new BusinessException("No fue posible generar el archivo Excel de pagos: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarPagosPdf(String busqueda, UUID contribuyenteId, UUID sesionCajaId,
                                  MedioPagoTributario medioPago, EstadoPagoTributario estado,
                                  OffsetDateTime fechaDesde, OffsetDateTime fechaHasta) {
        List<PagoTributario> pagos = pagoRepository.findAll(
                PagoSpecification.conFiltros(busqueda, contribuyenteId, sesionCajaId, medioPago, estado, fechaDesde, fechaHasta),
                Sort.by(Sort.Direction.DESC, "fechaHoraPago")
        );

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 13, com.lowagie.text.Font.BOLD, new Color(15, 23, 42));
            com.lowagie.text.Font subFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8, com.lowagie.text.Font.NORMAL, new Color(100, 116, 139));
            com.lowagie.text.Font thFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8, com.lowagie.text.Font.BOLD, Color.WHITE);
            com.lowagie.text.Font tdFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 7, com.lowagie.text.Font.NORMAL, new Color(30, 41, 59));
            com.lowagie.text.Font tdBold = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 7, com.lowagie.text.Font.BOLD, new Color(30, 41, 59));

            Paragraph title = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph sub = new Paragraph("REPORTE DE RECAUDACIÓN Y PAGOS TRIBUTARIOS — RECAUDACIÓN Y CAJA MUNICIPAL", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(5);
            doc.add(sub);

            Paragraph nota = new Paragraph("AVISO: Reporte oficial de control interno y conciliación de caja. No constituye comprobante oficial definitivo de pago.", subFont);
            nota.setAlignment(Element.ALIGN_CENTER);
            nota.setSpacingAfter(10);
            doc.add(nota);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 1.8f, 1.4f, 3.5f, 2.0f, 1.8f, 1.5f, 1.5f});

            String[] ths = {"Código", "Fecha/Hora", "Documento", "Contribuyente", "Medio", "Caja/Ventanilla", "Importe (S/)", "Estado"};
            for (String h : ths) {
                PdfPCell cell = new PdfPCell(new Phrase(h, thFont));
                cell.setBackgroundColor(new Color(30, 58, 138));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(4);
                table.addCell(cell);
            }

            BigDecimal totalConfirmado = BigDecimal.ZERO;
            for (PagoTributario p : pagos) {
                table.addCell(crearCelda(p.getCodigoPago(), tdFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(p.getFechaHoraPago().format(FORMATO_FECHA_HORA), tdFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(p.getContribuyente().getNumeroDocumento(), tdFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(p.getContribuyente().getNombreRazonSocial(), tdFont, Element.ALIGN_LEFT));
                table.addCell(crearCelda(p.getMedioPago().getDescripcion(), tdFont, Element.ALIGN_LEFT));
                table.addCell(crearCelda(p.getSesionCaja() != null ? p.getSesionCaja().getCaja().getNombre() : "Externo", tdFont, Element.ALIGN_LEFT));
                table.addCell(crearCelda(String.format("S/ %.2f", p.getImporte()), tdBold, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(p.getEstado().getDescripcion(), tdFont, Element.ALIGN_CENTER));

                if (p.getEstado() == EstadoPagoTributario.CONFIRMADO || p.getEstado() == EstadoPagoTributario.PROCESADO) {
                    totalConfirmado = totalConfirmado.add(p.getImporte());
                }
            }

            doc.add(table);

            Paragraph totPar = new Paragraph(String.format("\nTotal Recaudación Confirmada: S/ %.2f", totalConfirmado), titleFont);
            totPar.setAlignment(Element.ALIGN_RIGHT);
            doc.add(totPar);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar pagos a PDF", e);
            throw new BusinessException("No fue posible generar el reporte PDF de pagos: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarCierreSesionPdf(UUID sesionCajaId) {
        SesionCajaMunicipal sesion = sesionRepository.findById(sesionCajaId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de caja no encontrada"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 25, 25, 25, 25);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD, new Color(15, 23, 42));
            com.lowagie.text.Font subFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.NORMAL, new Color(100, 116, 139));
            com.lowagie.text.Font boldFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.BOLD, new Color(15, 23, 42));
            com.lowagie.text.Font regularFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.NORMAL, new Color(30, 41, 59));

            Paragraph title = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph sub = new Paragraph("ACTA DE ARQUEO Y CIERRE DE SESIÓN DE CAJA MUNICIPAL", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(15);
            doc.add(sub);

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setWidths(new float[]{1.5f, 2.5f});

            agregarFilaInfo(info, "Código de Sesión:", sesion.getCodigoSesion(), boldFont, regularFont);
            agregarFilaInfo(info, "Caja Municipal:", sesion.getCaja().getNombre() + " (" + sesion.getCaja().getCodigo() + ")", boldFont, regularFont);
            agregarFilaInfo(info, "Cajero Responsable:", sesion.getCajero().getNombreCompleto(), boldFont, regularFont);
            agregarFilaInfo(info, "Fecha y Hora Apertura:", sesion.getFechaApertura().format(FORMATO_FECHA_HORA), boldFont, regularFont);
            agregarFilaInfo(info, "Fecha y Hora Cierre:", sesion.getFechaCierre() != null ? sesion.getFechaCierre().format(FORMATO_FECHA_HORA) : "En proceso", boldFont, regularFont);
            agregarFilaInfo(info, "Estado de Sesión:", sesion.getEstado().getDescripcion(), boldFont, regularFont);
            doc.add(info);

            Paragraph balanceTitle = new Paragraph("\nRESUMEN DE CUADRE Y ARQUEO FÍSICO", boldFont);
            balanceTitle.setSpacingAfter(8);
            doc.add(balanceTitle);

            PdfPTable bal = new PdfPTable(2);
            bal.setWidthPercentage(100);
            bal.setWidths(new float[]{2.5f, 1.5f});

            agregarFilaBalance(bal, "(+) Fondo Inicial de Apertura (Sencillo):", String.format("S/ %.2f", sesion.getMontoApertura()), regularFont);
            agregarFilaBalance(bal, "(+) Total Recaudación Efectivo:", String.format("S/ %.2f", sesion.getTotalRecaudadoEfectivo()), regularFont);
            agregarFilaBalance(bal, "(=) Efectivo Total Esperado en Caja:", String.format("S/ %.2f", sesion.getMontoCierreCalculado()), boldFont);
            agregarFilaBalance(bal, "(-) Efectivo Declarado en Arqueo Físico:", String.format("S/ %.2f", sesion.getMontoCierreDeclarado() != null ? sesion.getMontoCierreDeclarado() : BigDecimal.ZERO), boldFont);
            agregarFilaBalance(bal, "(=) Diferencia de Caja (Faltante / Sobrante):", String.format("S/ %.2f", sesion.getDiferencia()), boldFont);
            agregarFilaBalance(bal, "Total Operaciones Realizadas:", String.valueOf(sesion.getTotalOperaciones()), regularFont);
            agregarFilaBalance(bal, "Recaudación en Otros Medios (Bancos/Tarjetas):", String.format("S/ %.2f", sesion.getTotalRecaudadoOtrosMedios()), regularFont);
            agregarFilaBalance(bal, "Recaudación Total Global:", String.format("S/ %.2f", sesion.getTotalRecaudadoGeneral()), boldFont);
            doc.add(bal);

            if (sesion.getJustificacionDiferencia() != null && !sesion.getJustificacionDiferencia().trim().isEmpty()) {
                Paragraph justTitle = new Paragraph("\nJUSTIFICACIÓN Y SUSTENTO DE DIFERENCIAS:", boldFont);
                justTitle.setSpacingAfter(4);
                doc.add(justTitle);
                Paragraph justText = new Paragraph(sesion.getJustificacionDiferencia(), regularFont);
                doc.add(justText);
            }

            Paragraph firm = new Paragraph("\n\n\n\n______________________________                  ______________________________\n        Cajero en Turno                                     V°B° Tesorería / Supervisor", subFont);
            firm.setAlignment(Element.ALIGN_CENTER);
            doc.add(firm);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar acta de cierre a PDF", e);
            throw new BusinessException("No fue posible generar el acta de cierre: " + e.getMessage());
        }
    }

    private PdfPCell crearCelda(String texto, com.lowagie.text.Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(align);
        cell.setPadding(4);
        return cell;
    }

    private void agregarFilaInfo(PdfPTable table, String label, String value, com.lowagie.text.Font labelFont, com.lowagie.text.Font valFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setBorder(0);
        c1.setPadding(3);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, valFont));
        c2.setBorder(0);
        c2.setPadding(3);
        table.addCell(c2);
    }

    private void agregarFilaBalance(PdfPTable table, String label, String value, com.lowagie.text.Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font));
        c1.setPadding(4);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, font));
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setPadding(4);
        table.addCell(c2);
    }
}
