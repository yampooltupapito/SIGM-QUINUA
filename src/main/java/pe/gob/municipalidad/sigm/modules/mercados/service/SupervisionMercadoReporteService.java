package pe.gob.municipalidad.sigm.modules.mercados.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.EstadisticasSupervisionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.InspeccionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMedidaCorrectiva;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoInspeccionMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.HallazgoInspeccionRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.InspeccionMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MedidaCorrectivaMercadoRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SupervisionMercadoReporteService {

    private final InspeccionMercadoRepository inspeccionRepository;
    private final HallazgoInspeccionRepository hallazgoRepository;
    private final MedidaCorrectivaMercadoRepository medidaRepository;
    private final InspeccionMercadoService inspeccionService;

    public SupervisionMercadoReporteService(InspeccionMercadoRepository inspeccionRepository,
                                            HallazgoInspeccionRepository hallazgoRepository,
                                            MedidaCorrectivaMercadoRepository medidaRepository,
                                            InspeccionMercadoService inspeccionService) {
        this.inspeccionRepository = inspeccionRepository;
        this.hallazgoRepository = hallazgoRepository;
        this.medidaRepository = medidaRepository;
        this.inspeccionService = inspeccionService;
    }

    @Transactional(readOnly = true)
    public EstadisticasSupervisionResponse obtenerEstadisticas() {
        LocalDate hoy = LocalDate.now();
        LocalDate proximosDias = hoy.plusDays(7);

        long totalInspecciones = inspeccionRepository.count();
        long programadas = inspeccionRepository.countByEstado(EstadoInspeccion.PROGRAMADA) + inspeccionRepository.countByEstado(EstadoInspeccion.ASIGNADA);
        long enProceso = inspeccionRepository.countByEstado(EstadoInspeccion.EN_PROCESO) + inspeccionRepository.countByEstado(EstadoInspeccion.PENDIENTE_REVISION);
        long finalizadas = inspeccionRepository.countByEstado(EstadoInspeccion.FINALIZADA);
        long canceladas = inspeccionRepository.countByEstado(EstadoInspeccion.CANCELADA);
        long inspeccionesHoy = inspeccionRepository.countInspeccionesDelDia(hoy);

        long totalHallazgos = hallazgoRepository.count();
        long hallazgosAbiertos = hallazgoRepository.countByEstado("ABIERTO") + hallazgoRepository.countByEstado("CON_MEDIDA_CORRECTIVA");

        long totalMedidas = medidaRepository.count();
        long medidasPendientes = medidaRepository.countByEstado(EstadoMedidaCorrectiva.PENDIENTE) +
                medidaRepository.countByEstado(EstadoMedidaCorrectiva.EN_PROCESO) +
                medidaRepository.countByEstado(EstadoMedidaCorrectiva.PRESENTADA_PARA_VERIFICACION);
        long medidasSubsanadas = medidaRepository.countByEstado(EstadoMedidaCorrectiva.SUBSANADA);
        long medidasVencidas = medidaRepository.countVencidas(hoy);
        long medidasPorVencer = medidaRepository.countPorVencer(hoy, proximosDias);

        Map<String, Long> porTipo = new HashMap<>();
        for (TipoInspeccionMercado t : TipoInspeccionMercado.values()) {
            porTipo.put(t.name(), inspeccionRepository.countByTipoInspeccion(t));
        }

        Map<String, Long> porEstado = new HashMap<>();
        for (EstadoInspeccion e : EstadoInspeccion.values()) {
            porEstado.put(e.name(), inspeccionRepository.countByEstado(e));
        }

        Map<String, Long> medidasPorEstado = new HashMap<>();
        for (EstadoMedidaCorrectiva m : EstadoMedidaCorrectiva.values()) {
            medidasPorEstado.put(m.name(), medidaRepository.countByEstado(m));
        }

        return EstadisticasSupervisionResponse.builder()
                .totalInspecciones(totalInspecciones)
                .programadas(programadas)
                .enProceso(enProceso)
                .finalizadas(finalizadas)
                .canceladas(canceladas)
                .inspeccionesHoy(inspeccionesHoy)
                .totalHallazgos(totalHallazgos)
                .hallazgosAbiertos(hallazgosAbiertos)
                .totalMedidas(totalMedidas)
                .medidasPendientes(medidasPendientes)
                .medidasSubsanadas(medidasSubsanadas)
                .medidasVencidas(medidasVencidas)
                .medidasPorVencer(medidasPorVencer)
                .inspeccionesPorTipo(porTipo)
                .inspeccionesPorEstado(porEstado)
                .medidasPorEstado(medidasPorEstado)
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] exportarInspeccionesExcel(LocalDate desde, LocalDate hasta) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Inspecciones Mercados");

            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Código", "Tipo Inspección", "Mercado", "Puesto", "Comerciante", "Fecha Prog.", "Hora", "Estado", "Inspector", "Motivo"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Page<InspeccionResponse> page = inspeccionService.buscarPaginado(null, null, null, null, null, null, null, null, desde, hasta, null, PageRequest.of(0, 1000));
            int rowIdx = 1;
            for (InspeccionResponse r : page.getContent()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getCodigoInspeccion());
                row.createCell(1).setCellValue(r.getTipoInspeccion() != null ? r.getTipoInspeccion().getDescripcion() : "");
                row.createCell(2).setCellValue(r.getMercadoNombre() != null ? r.getMercadoNombre() : "");
                row.createCell(3).setCellValue(r.getPuestoCodigo() != null ? r.getPuestoCodigo() : "GENERAL");
                row.createCell(4).setCellValue(r.getComercianteNombre() != null ? r.getComercianteNombre() : "-");
                row.createCell(5).setCellValue(r.getFechaProgramada() != null ? r.getFechaProgramada().toString() : "");
                row.createCell(6).setCellValue(r.getHoraProgramada() != null ? r.getHoraProgramada().toString() : "");
                row.createCell(7).setCellValue(r.getEstado() != null ? r.getEstado().name() : "");
                row.createCell(8).setCellValue(r.getInspectorResponsableNombre() != null ? r.getInspectorResponsableNombre() : "-");
                row.createCell(9).setCellValue(r.getMotivo() != null ? r.getMotivo() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar inspecciones a Excel: {}", e.getMessage(), e);
            throw new BusinessException("No fue posible generar el reporte Excel: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportarInspeccionesPdf(LocalDate desde, LocalDate hasta) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 25, 25, 30, 30);
            PdfWriter.getInstance(document, out);
            document.open();

            Color colorHeader = new Color(29, 78, 216);
            com.lowagie.text.Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, colorHeader);
            com.lowagie.text.Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            com.lowagie.text.Font fontTh = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            com.lowagie.text.Font fontTd = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);

            Paragraph pTit = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo);
            pTit.setAlignment(Element.ALIGN_CENTER);
            document.add(pTit);

            Paragraph pSub = new Paragraph("Reporte Consolidado de Inspecciones y Supervisión de Mercados\n" +
                    "Generado: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(14);
            document.add(pSub);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{14f, 15f, 18f, 12f, 16f, 10f, 15f, 12f});

            String[] cols = {"Código", "Tipo", "Mercado", "Puesto", "Fecha Prog.", "Hora", "Inspector", "Estado"};
            for (String col : cols) {
                PdfPCell th = new PdfPCell(new Phrase(col, fontTh));
                th.setBackgroundColor(colorHeader);
                th.setPadding(5);
                table.addCell(th);
            }

            Page<InspeccionResponse> page = inspeccionService.buscarPaginado(null, null, null, null, null, null, null, null, desde, hasta, null, PageRequest.of(0, 500));
            for (InspeccionResponse r : page.getContent()) {
                table.addCell(new Phrase(r.getCodigoInspeccion(), fontTd));
                table.addCell(new Phrase(r.getTipoInspeccion() != null ? r.getTipoInspeccion().name() : "-", fontTd));
                table.addCell(new Phrase(r.getMercadoNombre() != null ? r.getMercadoNombre() : "-", fontTd));
                table.addCell(new Phrase(r.getPuestoCodigo() != null ? r.getPuestoCodigo() : "GENERAL", fontTd));
                table.addCell(new Phrase(r.getFechaProgramada() != null ? r.getFechaProgramada().toString() : "-", fontTd));
                table.addCell(new Phrase(r.getHoraProgramada() != null ? r.getHoraProgramada().toString() : "-", fontTd));
                table.addCell(new Phrase(r.getInspectorResponsableNombre() != null ? r.getInspectorResponsableNombre() : "-", fontTd));
                table.addCell(new Phrase(r.getEstado() != null ? r.getEstado().name() : "-", fontTd));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar inspecciones a PDF: {}", e.getMessage(), e);
            throw new BusinessException("No fue posible generar el reporte PDF: " + e.getMessage());
        }
    }
}
