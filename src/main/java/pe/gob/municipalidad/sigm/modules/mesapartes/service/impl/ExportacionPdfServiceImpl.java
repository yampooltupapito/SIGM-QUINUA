package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ReporteDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ReporteExportacion;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ReporteExportacionRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ExportacionPdfService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ReporteMesaPartesService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class ExportacionPdfServiceImpl implements ExportacionPdfService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final int MAX_REGISTROS_PDF = 2000;

    private final ReporteMesaPartesService reporteService;
    private final ReporteExportacionRepository exportacionRepository;
    private final UsuarioRepository usuarioRepository;

    public ExportacionPdfServiceImpl(ReporteMesaPartesService reporteService,
                                     ReporteExportacionRepository exportacionRepository,
                                     UsuarioRepository usuarioRepository) {
        this.reporteService = reporteService;
        this.exportacionRepository = exportacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public byte[] exportarReportePdf(FiltroReporteRequest filtro, String username, String ip, String userAgent) {
        long inicioMs = System.currentTimeMillis();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Usuario no autenticado: " + username));

        PageRequest pageRequest = PageRequest.of(0, MAX_REGISTROS_PDF, Sort.by("createdAt").descending());
        Page<ExpedienteReporteItemDto> pagina = reporteService.listarExpedientes(filtro, pageRequest, username);
        ResumenEstadisticoDto resumen = reporteService.obtenerResumen(filtro, username);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Documento en orientación horizontal (Landscape) para tabla completa
            Document document = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPdfVersion(PdfWriter.PDF_VERSION_1_7);

            // Pie de página institucional mediante PageEvent
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document doc) {
                    PdfPTable footer = new PdfPTable(2);
                    try {
                        footer.setWidths(new float[]{80, 20});
                        footer.setTotalWidth(doc.getPageSize().getWidth() - 60);
                        footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                        Font fontDesc = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, new Color(100, 116, 139));
                        Font fontPag = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(100, 116, 139));

                        PdfPCell c1 = new PdfPCell(new Phrase("Reporte administrativo de supervisión para uso interno. No constituye certificación oficial de trámite según TUO Ley N° 27444.", fontDesc));
                        c1.setBorder(Rectangle.TOP);
                        c1.setBorderColor(new Color(226, 232, 240));
                        c1.setPaddingTop(4);
                        footer.addCell(c1);

                        PdfPCell c2 = new PdfPCell(new Phrase("Página " + writer.getPageNumber(), fontPag));
                        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        c2.setBorder(Rectangle.TOP);
                        c2.setBorderColor(new Color(226, 232, 240));
                        c2.setPaddingTop(4);
                        footer.addCell(c2);

                        footer.writeSelectedRows(0, -1, 30, 25, writer.getDirectContent());
                    } catch (Exception ex) {
                        log.warn("Error agregando pie de página PDF: {}", ex.getMessage());
                    }
                }
            });

            document.open();

            // Estilos tipográficos institucionales
            Font fontMuni = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(29, 78, 216));
            Font fontSubMuni = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(100, 116, 139));
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(15, 23, 42));
            Font fontMetaLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(30, 41, 59));
            Font fontMetaVal = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(71, 85, 105));
            Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font fontDataTabla = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(30, 41, 59));
            Font fontDataTablaBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, new Color(30, 41, 59));

            // Encabezado institucional
            Paragraph muniHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontMuni);
            muniHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(muniHeader);

            Paragraph muniSub = new Paragraph("Capital de la Independencia Americana | Huamanga - Ayacucho | SIGM-QUINUA", fontSubMuni);
            muniSub.setAlignment(Element.ALIGN_CENTER);
            muniSub.setSpacingAfter(6);
            document.add(muniSub);

            LineSeparator line = new LineSeparator(1, 100, new Color(226, 232, 240), Element.ALIGN_CENTER, -2);
            document.add(line);

            // Título del reporte
            Paragraph pTitulo = new Paragraph("REPORTE OFICIAL DE EXPEDIENTES DE MESA DE PARTES VIRTUAL", fontTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            pTitulo.setSpacingBefore(6);
            pTitulo.setSpacingAfter(8);
            document.add(pTitulo);

            // Metadatos
            OffsetDateTime[] fechas = ReporteFilterHelper.resolverRangoFechas(filtro);
            String periodoStr = fechas[0].format(FORMATO_FECHA) + " al " + fechas[1].format(FORMATO_FECHA);
            String strFechaGen = OffsetDateTime.now(ZONA_LIMA).format(FORMATO_FECHA_HORA);
            String nombreUsuario = usuario.getPersona() != null
                    ? usuario.getPersona().getNombres() + " " + usuario.getPersona().getApellidoPaterno()
                    : usuario.getUsername();

            PdfPTable metaTable = new PdfPTable(4);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{18, 32, 18, 32});
            metaTable.setSpacingAfter(8);

            agregarCeldaMeta(metaTable, "Período Consultado:", fontMetaLabel);
            agregarCeldaMeta(metaTable, periodoStr, fontMetaVal);
            agregarCeldaMeta(metaTable, "Fecha de Emisión:", fontMetaLabel);
            agregarCeldaMeta(metaTable, strFechaGen, fontMetaVal);

            agregarCeldaMeta(metaTable, "Responsable:", fontMetaLabel);
            agregarCeldaMeta(metaTable, nombreUsuario, fontMetaVal);
            agregarCeldaMeta(metaTable, "Total Expedientes:", fontMetaLabel);
            agregarCeldaMeta(metaTable, String.valueOf(pagina.getTotalElements()), fontMetaVal);

            document.add(metaTable);

            // Tarjetas de Resumen Cuantitativo
            PdfPTable kpiTable = new PdfPTable(6);
            kpiTable.setWidthPercentage(100);
            kpiTable.setWidths(new float[]{16, 16, 16, 16, 16, 20});
            kpiTable.setSpacingAfter(10);

            agregarKpiCell(kpiTable, "PRESENTADOS", String.valueOf(resumen.getTotalPresentados()), new Color(59, 130, 246));
            agregarKpiCell(kpiTable, "RECIBIDOS", String.valueOf(resumen.getTotalRecibidos()), new Color(6, 182, 212));
            agregarKpiCell(kpiTable, "EN ATENCIÓN", String.valueOf(resumen.getTotalEnAtencion()), new Color(139, 92, 246));
            agregarKpiCell(kpiTable, "OBSERVADOS", String.valueOf(resumen.getTotalObservados()), new Color(239, 68, 68));
            agregarKpiCell(kpiTable, "RESUELTOS", String.valueOf(resumen.getTotalResueltos()), new Color(16, 185, 129));
            agregarKpiCell(kpiTable, "TOTAL PENDIENTES", String.valueOf(resumen.getTotalPendientes()), new Color(245, 158, 11));

            document.add(kpiTable);

            // Tabla de datos
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 15, 9, 22, 16, 14, 8, 8, 4});

            String[] cols = {"N°", "N° Expediente", "Fecha", "Tipo Trámite", "Solicitante", "Oficina", "Estado", "Prioridad", "Días"};
            for (String col : cols) {
                PdfPCell cell = new PdfPCell(new Phrase(col, fontHeaderTabla));
                cell.setBackgroundColor(new Color(30, 41, 59));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(4);
                table.addCell(cell);
            }

            int itemNro = 1;
            Color zebraColor = new Color(248, 250, 252);
            for (ExpedienteReporteItemDto item : pagina.getContent()) {
                Color rowBg = (itemNro % 2 == 0) ? zebraColor : Color.WHITE;

                table.addCell(crearCeldaDato(String.valueOf(itemNro++), fontDataTabla, Element.ALIGN_CENTER, rowBg));
                table.addCell(crearCeldaDato(item.getNumeroExpediente(), fontDataTablaBold, Element.ALIGN_CENTER, rowBg));
                table.addCell(crearCeldaDato(item.getFechaPresentacion() != null ? item.getFechaPresentacion().format(FORMATO_FECHA) : "-", fontDataTabla, Element.ALIGN_CENTER, rowBg));
                table.addCell(crearCeldaDato(item.getTipoTramite(), fontDataTabla, Element.ALIGN_LEFT, rowBg));
                table.addCell(crearCeldaDato(item.getSolicitante(), fontDataTabla, Element.ALIGN_LEFT, rowBg));
                table.addCell(crearCeldaDato(item.getOficina(), fontDataTabla, Element.ALIGN_LEFT, rowBg));
                table.addCell(crearCeldaDato(item.getEstado(), fontDataTabla, Element.ALIGN_CENTER, rowBg));
                table.addCell(crearCeldaDato(item.getPrioridad(), fontDataTabla, Element.ALIGN_CENTER, rowBg));
                table.addCell(crearCeldaDato(String.valueOf(item.getDiasTranscurridos() != null ? item.getDiasTranscurridos() : 0), fontDataTabla, Element.ALIGN_RIGHT, rowBg));
            }

            document.add(table);
            document.close();

            byte[] pdfBytes = baos.toByteArray();

            // Auditoría
            long duracion = System.currentTimeMillis() - inicioMs;
            exportacionRepository.save(ReporteExportacion.builder()
                    .uuid(UUID.randomUUID())
                    .usuario(usuario)
                    .tipoReporte("REPORTE_GENERAL_EXPEDIENTES")
                    .formato("PDF")
                    .filtrosJson("periodo=" + (filtro != null ? filtro.getPeriodo() : "DEFAULT"))
                    .cantidadRegistros((int) pagina.getTotalElements())
                    .estado("COMPLETADO")
                    .tiempoGeneracionMs(duracion)
                    .ipOrigen(ip)
                    .userAgent(userAgent)
                    .fechaExportacion(OffsetDateTime.now())
                    .build());

            return pdfBytes;
        } catch (Exception e) {
            log.error("Error generando archivo PDF de Mesa de Partes: {}", e.getMessage(), e);
            throw new RuntimeException("Error en la generación del documento PDF", e);
        }
    }

    private void agregarCeldaMeta(PdfPTable table, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(2);
        table.addCell(cell);
    }

    private void agregarKpiCell(PdfPTable table, String label, String valor, Color accent) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new Color(203, 213, 225));
        cell.setBackgroundColor(new Color(241, 245, 249));
        cell.setPadding(4);

        Font fLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6, new Color(71, 85, 105));
        Font fVal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, accent);

        Paragraph p1 = new Paragraph(label, fLabel);
        p1.setAlignment(Element.ALIGN_CENTER);
        Paragraph p2 = new Paragraph(valor, fVal);
        p2.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(p1);
        cell.addElement(p2);
        table.addCell(cell);
    }

    private PdfPCell crearCeldaDato(String texto, Font font, int align, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(texto != null ? texto : "", font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(bg);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setPadding(3);
        return cell;
    }
}
