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
import pe.gob.municipalidad.sigm.modules.mercados.dto.EstadisticasIncidenciasResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.IncidenciaResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.TipoIncidenciaMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoEvaluacionInfraccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PrioridadIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.repository.IncidenciaMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.PresuntaInfraccionMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.TipoIncidenciaMercadoRepository;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class IncidenciasReporteService {

    private final IncidenciaMercadoRepository incidenciaRepository;
    private final PresuntaInfraccionMercadoRepository infraccionRepository;
    private final TipoIncidenciaMercadoRepository tipoRepository;
    private final IncidenciaMercadoService incidenciaService;

    public IncidenciasReporteService(
            IncidenciaMercadoRepository incidenciaRepository,
            PresuntaInfraccionMercadoRepository infraccionRepository,
            TipoIncidenciaMercadoRepository tipoRepository,
            IncidenciaMercadoService incidenciaService) {
        this.incidenciaRepository = incidenciaRepository;
        this.infraccionRepository = infraccionRepository;
        this.tipoRepository = tipoRepository;
        this.incidenciaService = incidenciaService;
    }

    @Transactional(readOnly = true)
    public EstadisticasIncidenciasResponse obtenerEstadisticas() {
        long totalIncidencias = incidenciaRepository.count();
        long abiertas = incidenciaRepository.countAbiertas();
        long enAtencion = incidenciaRepository.countByEstado(EstadoIncidencia.EN_ATENCION);
        long resueltas = incidenciaRepository.countByEstado(EstadoIncidencia.RESUELTA);
        long cerradas = incidenciaRepository.countByEstado(EstadoIncidencia.CERRADA);
        long criticas = incidenciaRepository.countCriticasAbiertas();
        long sinResponsable = incidenciaRepository.countByResponsableIsNullAndEstadoNotIn(
                Set.of(EstadoIncidencia.RESUELTA, EstadoIncidencia.CERRADA, EstadoIncidencia.DESCARTADA));

        long totalInfracciones = infraccionRepository.count();
        long infraccionesEnEval = infraccionRepository.countEnEvaluacionActiva();
        long infraccionesDerivadas = infraccionRepository.countByEstadoEvaluacion(EstadoEvaluacionInfraccion.DERIVADA_A_PROCEDIMIENTO);
        long infraccionesDescartadas = infraccionRepository.countByEstadoEvaluacion(EstadoEvaluacionInfraccion.DESCARTADA);

        Map<String, Long> porPrioridad = new HashMap<>();
        for (PrioridadIncidencia p : PrioridadIncidencia.values()) {
            porPrioridad.put(p.name(), incidenciaRepository.countByPrioridad(p));
        }

        Map<String, Long> porEstado = new HashMap<>();
        for (EstadoIncidencia e : EstadoIncidencia.values()) {
            porEstado.put(e.name(), incidenciaRepository.countByEstado(e));
        }

        Map<String, Long> porTipo = new HashMap<>();
        List<TipoIncidenciaMercado> tipos = tipoRepository.findByActivoTrueOrderByNombreAsc();
        for (TipoIncidenciaMercado t : tipos) {
            porTipo.put(t.getNombre(), 0L); // Default para agregación
        }

        Map<String, Long> infraccionesPorEstado = new HashMap<>();
        for (EstadoEvaluacionInfraccion ev : EstadoEvaluacionInfraccion.values()) {
            infraccionesPorEstado.put(ev.name(), infraccionRepository.countByEstadoEvaluacion(ev));
        }

        return EstadisticasIncidenciasResponse.builder()
                .totalIncidencias(totalIncidencias)
                .abiertas(abiertas)
                .enAtencion(enAtencion)
                .resueltas(resueltas)
                .cerradas(cerradas)
                .criticasAbiertas(criticas)
                .sinResponsable(sinResponsable)
                .totalPresuntasInfracciones(totalInfracciones)
                .infraccionesEnEvaluacion(infraccionesEnEval)
                .infraccionesDerivadas(infraccionesDerivadas)
                .infraccionesDescartadas(infraccionesDescartadas)
                .incidenciasPorPrioridad(porPrioridad)
                .incidenciasPorEstado(porEstado)
                .incidenciasPorTipo(porTipo)
                .infraccionesPorEstado(infraccionesPorEstado)
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] exportarIncidenciasExcel(LocalDate desde, LocalDate hasta) {
        log.info("Generando reporte Excel de incidencias de mercados");
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Incidencias");

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Cabeceras
            String[] headers = {
                    "Código", "Fecha Reporte", "Mercado", "Puesto", "Comerciante",
                    "Tipo Incidencia", "Origen", "Prioridad", "Estado", "Responsable", "Descripción"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            OffsetDateTime dtDesde = desde != null ? desde.atStartOfDay().atOffset(ZoneOffset.UTC) : null;
            OffsetDateTime dtHasta = hasta != null ? hasta.atTime(23, 59, 59).atOffset(ZoneOffset.UTC) : null;

            Page<IncidenciaResponse> page = incidenciaService.buscarPaginado(
                    null, null, null, null, null, null, null, null, null, null,
                    dtDesde, dtHasta, null, null, PageRequest.of(0, 2000, org.springframework.data.domain.Sort.by("fechaReporte").descending()));

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int rowIdx = 1;
            if (page.isEmpty()) {
                Row row = sheet.createRow(rowIdx++);
                Cell cell = row.createCell(0);
                cell.setCellValue("No se encontraron incidencias registradas en el período seleccionado.");
            } else {
                for (IncidenciaResponse inc : page.getContent()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(inc.getCodigoIncidencia() != null ? inc.getCodigoIncidencia() : "");
                    row.createCell(1).setCellValue(inc.getFechaReporte() != null ? inc.getFechaReporte().format(fmt) : "");
                    row.createCell(2).setCellValue(inc.getMercadoNombre() != null ? inc.getMercadoNombre() : "");
                    row.createCell(3).setCellValue(inc.getPuestoNumero() != null ? inc.getPuestoNumero() : "N/A");
                    row.createCell(4).setCellValue(inc.getComercianteNombre() != null ? inc.getComercianteNombre() : "N/A");
                    row.createCell(5).setCellValue(inc.getTipoIncidenciaNombre() != null ? inc.getTipoIncidenciaNombre() : "");
                    row.createCell(6).setCellValue(inc.getOrigen() != null ? inc.getOrigen().getLabel() : "");
                    row.createCell(7).setCellValue(inc.getPrioridad() != null ? inc.getPrioridad().getLabel() : "");
                    row.createCell(8).setCellValue(inc.getEstado() != null ? inc.getEstado().getLabel() : "");
                    row.createCell(9).setCellValue(inc.getResponsableNombre() != null ? inc.getResponsableNombre() : "Sin asignar");
                    row.createCell(10).setCellValue(inc.getDescripcion() != null ? inc.getDescripcion() : "");
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar reporte Excel de incidencias: {}", e.getMessage(), e);
            throw new BusinessException("No fue posible generar el archivo Excel de incidencias: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportarIncidenciasPdf(LocalDate desde, LocalDate hasta) {
        log.info("Generando reporte PDF de incidencias de mercados");
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 25, 25, 25, 25);
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
            Font fontCell = FontFactory.getFont(FontFactory.HELVETICA, 8);

            Paragraph pTit = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo);
            pTit.setAlignment(Element.ALIGN_CENTER);
            document.add(pTit);

            Paragraph pSub = new Paragraph("REPORTE OFICIAL DE INCIDENCIAS EN MERCADOS MUNICIPALES", fontSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(15);
            document.add(pSub);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{16f, 14f, 18f, 14f, 12f, 12f, 14f});

            String[] headers = {"Código", "Fecha", "Mercado", "Tipo", "Prioridad", "Estado", "Responsable"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontHeader));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            OffsetDateTime dtDesde = desde != null ? desde.atStartOfDay().atOffset(ZoneOffset.UTC) : null;
            OffsetDateTime dtHasta = hasta != null ? hasta.atTime(23, 59, 59).atOffset(ZoneOffset.UTC) : null;

            Page<IncidenciaResponse> page = incidenciaService.buscarPaginado(
                    null, null, null, null, null, null, null, null, null, null,
                    dtDesde, dtHasta, null, null, PageRequest.of(0, 1000, org.springframework.data.domain.Sort.by("fechaReporte").descending()));

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            if (page.isEmpty()) {
                PdfPCell cellEmpty = new PdfPCell(new Phrase("No se encontraron incidencias registradas en el período seleccionado.", fontCell));
                cellEmpty.setColspan(7);
                cellEmpty.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellEmpty.setPadding(10);
                table.addCell(cellEmpty);
            } else {
                for (IncidenciaResponse inc : page.getContent()) {
                    table.addCell(new Phrase(inc.getCodigoIncidencia() != null ? inc.getCodigoIncidencia() : "", fontCell));
                    table.addCell(new Phrase(inc.getFechaReporte() != null ? inc.getFechaReporte().format(fmt) : "", fontCell));
                    table.addCell(new Phrase(inc.getMercadoNombre() != null ? inc.getMercadoNombre() : "", fontCell));
                    table.addCell(new Phrase(inc.getTipoIncidenciaNombre() != null ? inc.getTipoIncidenciaNombre() : "", fontCell));
                    table.addCell(new Phrase(inc.getPrioridad() != null ? inc.getPrioridad().getLabel() : "", fontCell));
                    table.addCell(new Phrase(inc.getEstado() != null ? inc.getEstado().getLabel() : "", fontCell));
                    table.addCell(new Phrase(inc.getResponsableNombre() != null ? inc.getResponsableNombre() : "Sin asignar", fontCell));
                }
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar reporte PDF de incidencias: {}", e.getMessage(), e);
            throw new BusinessException("No fue posible generar el archivo PDF de incidencias: " + e.getMessage());
        }
    }
}
