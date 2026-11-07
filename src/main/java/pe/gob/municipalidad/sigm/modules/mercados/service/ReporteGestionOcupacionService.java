package pe.gob.municipalidad.sigm.modules.mercados.service;

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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.EstadisticasGestionOcupacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.SolicitudGestionOcupacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.SolicitudGestionOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoSolicitudGestion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.SolicitudGestionOcupacionRepository;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ReporteGestionOcupacionService {

    private final SolicitudGestionOcupacionRepository solicitudRepository;
    private final RenovacionOcupacionService renovacionService;

    public ReporteGestionOcupacionService(
            SolicitudGestionOcupacionRepository solicitudRepository,
            RenovacionOcupacionService renovacionService) {
        this.solicitudRepository = solicitudRepository;
        this.renovacionService = renovacionService;
    }

    @Transactional(readOnly = true)
    public EstadisticasGestionOcupacionResponse obtenerEstadisticas() {
        long total = solicitudRepository.count();

        long renPres = solicitudRepository.countByTipoSolicitudAndEstado(TipoSolicitudGestion.RENOVACION_ASIGNACION, EstadoSolicitudGestion.PRESENTADA)
                + solicitudRepository.countByTipoSolicitudAndEstado(TipoSolicitudGestion.RENOVACION_CONTRATO, EstadoSolicitudGestion.PRESENTADA)
                + solicitudRepository.countByTipoSolicitudAndEstado(TipoSolicitudGestion.RENOVACION_AUTORIZACION, EstadoSolicitudGestion.PRESENTADA);

        long renAprob = solicitudRepository.countByTipoSolicitudAndEstado(TipoSolicitudGestion.RENOVACION_ASIGNACION, EstadoSolicitudGestion.APROBADA)
                + solicitudRepository.countByTipoSolicitudAndEstado(TipoSolicitudGestion.RENOVACION_CONTRATO, EstadoSolicitudGestion.APROBADA)
                + solicitudRepository.countByTipoSolicitudAndEstado(TipoSolicitudGestion.RENOVACION_AUTORIZACION, EstadoSolicitudGestion.APROBADA);

        long renPend = solicitudRepository.countByTipoSolicitudAndEstado(TipoSolicitudGestion.RENOVACION_ASIGNACION, EstadoSolicitudGestion.EN_EVALUACION)
                + solicitudRepository.countByTipoSolicitudAndEstado(TipoSolicitudGestion.RENOVACION_CONTRATO, EstadoSolicitudGestion.EN_EVALUACION)
                + solicitudRepository.countByTipoSolicitudAndEstado(TipoSolicitudGestion.RENOVACION_AUTORIZACION, EstadoSolicitudGestion.EN_EVALUACION);

        long cambPres = solicitudRepository.countByTipoSolicitudAndEstado(TipoSolicitudGestion.CAMBIO_TITULAR, EstadoSolicitudGestion.PRESENTADA);
        long cambForm = solicitudRepository.countByTipoSolicitudAndEstado(TipoSolicitudGestion.CAMBIO_TITULAR, EstadoSolicitudGestion.FORMALIZADA);

        long tranEval = solicitudRepository.countByTipoSolicitudAndEstado(TipoSolicitudGestion.TRANSFERENCIA_AUTORIZADA, EstadoSolicitudGestion.EN_EVALUACION);
        long tranForm = solicitudRepository.countByTipoSolicitudAndEstado(TipoSolicitudGestion.TRANSFERENCIA_AUTORIZADA, EstadoSolicitudGestion.FORMALIZADA);

        long obs = solicitudRepository.countByEstado(EstadoSolicitudGestion.OBSERVADA);
        long rech = solicitudRepository.countByEstado(EstadoSolicitudGestion.RECHAZADA);

        Map<String, Long> porTipo = new HashMap<>();
        for (TipoSolicitudGestion t : TipoSolicitudGestion.values()) {
            porTipo.put(t.name(), solicitudRepository.countByTipoSolicitud(t));
        }

        Map<String, Long> porEstado = new HashMap<>();
        for (EstadoSolicitudGestion e : EstadoSolicitudGestion.values()) {
            porEstado.put(e.name(), solicitudRepository.countByEstado(e));
        }

        return EstadisticasGestionOcupacionResponse.builder()
                .totalSolicitudes(total)
                .renovacionesPresentadas(renPres)
                .renovacionesAprobadas(renAprob)
                .renovacionesPendientes(renPend)
                .cambiosTitularPresentados(cambPres)
                .cambiosTitularFormalizados(cambForm)
                .transferenciasEnEvaluacion(tranEval)
                .transferenciasFormalizadas(tranForm)
                .solicitudesObservadas(obs)
                .solicitudesRechazadas(rech)
                .porTipoSolicitud(porTipo)
                .porEstado(porEstado)
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcel(TipoSolicitudGestion tipo, EstadoSolicitudGestion estado, LocalDate desde, LocalDate hasta) {
        log.info("Generando reporte Excel de gestiones de ocupación (renovaciones/transferencias)");

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Solicitudes_Gestion");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {
                    "Código", "Tipo Solicitud", "Mercado", "Puesto", "Titular Actual",
                    "Titular Propuesto", "Fecha Presentación", "Estado", "Expediente", "Motivo"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Page<SolicitudGestionOcupacionResponse> page = renovacionService.buscarPaginado(
                    null, null, null, tipo, estado, desde, hasta, null, PageRequest.of(0, 1000));

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            int rowIdx = 1;
            for (SolicitudGestionOcupacionResponse s : page.getContent()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.getCodigoSolicitud());
                row.createCell(1).setCellValue(s.getTipoSolicitudLabel());
                row.createCell(2).setCellValue(s.getMercadoNombre());
                row.createCell(3).setCellValue(s.getPuestoNumero());
                row.createCell(4).setCellValue(s.getComercianteActualNombre() + " (" + s.getComercianteActualDocumento() + ")");
                row.createCell(5).setCellValue(s.getComerciantePropuestoNombre() != null ? s.getComerciantePropuestoNombre() : "N/A");
                row.createCell(6).setCellValue(s.getFechaPresentacion() != null ? s.getFechaPresentacion().format(fmt) : "");
                row.createCell(7).setCellValue(s.getEstadoLabel());
                row.createCell(8).setCellValue(s.getExpedienteId() != null ? s.getExpedienteId().toString() : "N/A");
                row.createCell(9).setCellValue(s.getMotivo());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar reporte Excel de gestiones de ocupación", e);
            throw new BusinessException("No se pudo generar el reporte en Excel: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf(TipoSolicitudGestion tipo, EstadoSolicitudGestion estado, LocalDate desde, LocalDate hasta) {
        log.info("Generando reporte PDF consolidado de gestiones de ocupación");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

            Paragraph pTitle = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", titleFont);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            doc.add(pTitle);

            Paragraph pSub = new Paragraph("Subgerencia de Servicios Municipales - Administración de Mercados\n" +
                    "Reporte Institucional de Solicitudes de Ocupación, Renovaciones y Transferencias\n\n", subFont);
            pSub.setAlignment(Element.ALIGN_CENTER);
            doc.add(pSub);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{16, 20, 14, 12, 18, 12, 8});

            String[] cols = {"Código", "Tipo Solicitud", "Mercado", "Puesto", "Titular Actual", "Fecha Pres.", "Estado"};
            for (String col : cols) {
                PdfPCell c = new PdfPCell(new Phrase(col, headerFont));
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setPadding(6);
                table.addCell(c);
            }

            Page<SolicitudGestionOcupacionResponse> page = renovacionService.buscarPaginado(
                    null, null, null, tipo, estado, desde, hasta, null, PageRequest.of(0, 1000));

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (SolicitudGestionOcupacionResponse s : page.getContent()) {
                table.addCell(new Phrase(s.getCodigoSolicitud(), rowFont));
                table.addCell(new Phrase(s.getTipoSolicitudLabel(), rowFont));
                table.addCell(new Phrase(s.getMercadoNombre(), rowFont));
                table.addCell(new Phrase("Puesto " + s.getPuestoNumero(), rowFont));
                table.addCell(new Phrase(s.getComercianteActualNombre(), rowFont));
                table.addCell(new Phrase(s.getFechaPresentacion() != null ? s.getFechaPresentacion().format(fmt) : "", rowFont));
                table.addCell(new Phrase(s.getEstadoLabel(), rowFont));
            }

            doc.add(table);
            doc.close();

            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar reporte PDF de gestiones de ocupación", e);
            throw new BusinessException("No se pudo generar el reporte en PDF: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public SolicitudGestionOcupacionResponse verificarTokenQr(String token) {
        SolicitudGestionOcupacion s = solicitudRepository.findByTokenVerificacionQr(token)
                .orElseThrow(() -> new ResourceNotFoundException("Constancia no encontrada para el token QR provisto: " + token));
        return renovacionService.toResponse(s);
    }
}
