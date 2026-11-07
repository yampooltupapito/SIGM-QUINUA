package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ExportacionExcelService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ReporteMesaPartesService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class ExportacionExcelServiceImpl implements ExportacionExcelService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final int MAX_REGISTROS_EXCEL = 10000;

    private final ReporteMesaPartesService reporteService;
    private final ReporteExportacionRepository exportacionRepository;
    private final UsuarioRepository usuarioRepository;

    public ExportacionExcelServiceImpl(ReporteMesaPartesService reporteService,
                                       ReporteExportacionRepository exportacionRepository,
                                       UsuarioRepository usuarioRepository) {
        this.reporteService = reporteService;
        this.exportacionRepository = exportacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public byte[] exportarReporteExcel(FiltroReporteRequest filtro, String username, String ip, String userAgent) {
        long inicioMs = System.currentTimeMillis();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Usuario no autenticado: " + username));

        // Obtener datos respetando filtros y permisos
        PageRequest pageRequest = PageRequest.of(0, MAX_REGISTROS_EXCEL, Sort.by("createdAt").descending());
        Page<ExpedienteReporteItemDto> pagina = reporteService.listarExpedientes(filtro, pageRequest, username);

        ResumenEstadisticoDto resumen = reporteService.obtenerResumen(filtro, username);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Expedientes Mesa de Partes");
            sheet.setDisplayGridlines(true);

            // ==================== PALETA DE ESTILOS ====================
            byte[] rgbHeaderBg = new byte[]{30, 41, 59};      // #1e293b Slate Dark
            byte[] rgbSubBg = new byte[]{29, 78, (byte) 0xD8};        // #1d4ed8 Blue
            byte[] rgbKpiBg = new byte[]{32, 44, 61};         // #202c3d Card Dark
            byte[] rgbZebra = new byte[]{(byte) 0xF8, (byte) 0xFA, (byte) 0xFC}; // #f8fafc

            // 1. Estilo Título Institucional
            XSSFCellStyle styleTitle = workbook.createCellStyle();
            XSSFFont fontTitle = workbook.createFont();
            fontTitle.setFontName("Calibri");
            fontTitle.setFontHeightInPoints((short) 15);
            fontTitle.setBold(true);
            fontTitle.setColor(new XSSFColor(rgbSubBg, null));
            styleTitle.setFont(fontTitle);

            // 2. Estilo Subtítulo
            XSSFCellStyle styleSub = workbook.createCellStyle();
            XSSFFont fontSub = workbook.createFont();
            fontSub.setFontName("Calibri");
            fontSub.setFontHeightInPoints((short) 10);
            fontSub.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            styleSub.setFont(fontSub);

            // 3. Estilo Metadatos
            XSSFCellStyle styleMetaLabel = workbook.createCellStyle();
            XSSFFont fontMetaLabel = workbook.createFont();
            fontMetaLabel.setBold(true);
            fontMetaLabel.setFontHeightInPoints((short) 9);
            styleMetaLabel.setFont(fontMetaLabel);

            XSSFCellStyle styleMetaVal = workbook.createCellStyle();
            XSSFFont fontMetaVal = workbook.createFont();
            fontMetaVal.setFontHeightInPoints((short) 9);
            styleMetaVal.setFont(fontMetaVal);

            // 4. Estilo Encabezados de Tabla
            XSSFCellStyle styleHeader = workbook.createCellStyle();
            styleHeader.setFillForegroundColor(new XSSFColor(rgbHeaderBg, null));
            styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleHeader.setAlignment(HorizontalAlignment.CENTER);
            styleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
            styleHeader.setBorderBottom(BorderStyle.MEDIUM);
            styleHeader.setBorderTop(BorderStyle.THIN);
            styleHeader.setBorderLeft(BorderStyle.THIN);
            styleHeader.setBorderRight(BorderStyle.THIN);
            XSSFFont fontHeader = workbook.createFont();
            fontHeader.setFontName("Calibri");
            fontHeader.setBold(true);
            fontHeader.setColor(IndexedColors.WHITE.getIndex());
            fontHeader.setFontHeightInPoints((short) 10);
            styleHeader.setFont(fontHeader);

            // 5. Estilo Celdas de Datos
            CellStyle styleDataNormal = workbook.createCellStyle();
            styleDataNormal.setBorderBottom(BorderStyle.THIN);
            styleDataNormal.setBorderTop(BorderStyle.THIN);
            styleDataNormal.setBorderLeft(BorderStyle.THIN);
            styleDataNormal.setBorderRight(BorderStyle.THIN);
            styleDataNormal.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle styleDataCenter = workbook.createCellStyle();
            styleDataCenter.cloneStyleFrom(styleDataNormal);
            styleDataCenter.setAlignment(HorizontalAlignment.CENTER);

            CellStyle styleDataRight = workbook.createCellStyle();
            styleDataRight.cloneStyleFrom(styleDataNormal);
            styleDataRight.setAlignment(HorizontalAlignment.RIGHT);

            // ==================== ENCABEZADO INSTITUCIONAL ====================
            int rowIdx = 0;
            Row rowTitle = sheet.createRow(rowIdx++);
            Cell cellTitle = rowTitle.createCell(0);
            cellTitle.setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA — HUAMANGA, AYACUCHO");
            cellTitle.setCellStyle(styleTitle);

            Row rowSub = sheet.createRow(rowIdx++);
            Cell cellSub = rowSub.createCell(0);
            cellSub.setCellValue("Sistema Integral de Gestión Municipal (SIGM-QUINUA) | Reporte General de Mesa de Partes Virtual");
            cellSub.setCellStyle(styleSub);

            rowIdx++; // Línea en blanco

            // ==================== METADATOS Y PARÁMETROS ====================
            String strFechaGen = OffsetDateTime.now(ZONA_LIMA).format(FORMATO_FECHA_HORA);
            String nombreUsuario = usuario.getPersona() != null
                    ? usuario.getPersona().getNombres() + " " + usuario.getPersona().getApellidoPaterno()
                    : usuario.getUsername();

            OffsetDateTime[] fechas = ReporteFilterHelper.resolverRangoFechas(filtro);
            String periodoStr = fechas[0].format(FORMATO_FECHA) + " al " + fechas[1].format(FORMATO_FECHA);

            Row rMeta1 = sheet.createRow(rowIdx++);
            crearParMetadato(rMeta1, 0, "Fecha de Emisión:", strFechaGen, styleMetaLabel, styleMetaVal);
            crearParMetadato(rMeta1, 4, "Período Consultado:", periodoStr, styleMetaLabel, styleMetaVal);

            Row rMeta2 = sheet.createRow(rowIdx++);
            crearParMetadato(rMeta2, 0, "Generado por:", nombreUsuario, styleMetaLabel, styleMetaVal);
            crearParMetadato(rMeta2, 4, "Total Registros:", String.valueOf(pagina.getTotalElements()), styleMetaLabel, styleMetaVal);

            // Resumen de tarjetas
            Row rResumen = sheet.createRow(rowIdx++);
            Cell cRes = rResumen.createCell(0);
            cRes.setCellValue(String.format("Métricas Clave: Presentados: %d | Recibidos: %d | En Atención: %d | Observados: %d | Resueltos: %d | Pendientes: %d",
                    resumen.getTotalPresentados(), resumen.getTotalRecibidos(), resumen.getTotalEnAtencion(),
                    resumen.getTotalObservados(), resumen.getTotalResueltos(), resumen.getTotalPendientes()));
            cRes.setCellStyle(styleMetaLabel);

            rowIdx++; // Espacio

            // ==================== TABLA PRINCIPAL ====================
            String[] headers = {
                    "N°",
                    "N° Expediente",
                    "Fecha Ingreso",
                    "Tipo de Trámite",
                    "Asunto",
                    "Solicitante / Administrado",
                    "N° Documento",
                    "Dependencia Responsable",
                    "Prioridad",
                    "Canal",
                    "Estado Actual",
                    "Días Transc.",
                    "Fecha Cierre"
            };

            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(styleHeader);
            }

            int nro = 1;
            for (ExpedienteReporteItemDto item : pagina.getContent()) {
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(18);

                // 1. N°
                Cell c0 = row.createCell(0);
                c0.setCellValue(nro++);
                c0.setCellStyle(styleDataCenter);

                // 2. N° Expediente (Sanitizado)
                Cell c1 = row.createCell(1);
                c1.setCellValue(ReporteFilterHelper.sanitizarParaExcel(item.getNumeroExpediente()));
                c1.setCellStyle(styleDataCenter);

                // 3. Fecha Ingreso
                Cell c2 = row.createCell(2);
                c2.setCellValue(item.getFechaPresentacion() != null ? item.getFechaPresentacion().format(FORMATO_FECHA) : "-");
                c2.setCellStyle(styleDataCenter);

                // 4. Tipo de Trámite
                Cell c3 = row.createCell(3);
                c3.setCellValue(ReporteFilterHelper.sanitizarParaExcel(item.getTipoTramite()));
                c3.setCellStyle(styleDataNormal);

                // 5. Asunto
                Cell c4 = row.createCell(4);
                c4.setCellValue(ReporteFilterHelper.sanitizarParaExcel(item.getAsunto()));
                c4.setCellStyle(styleDataNormal);

                // 6. Solicitante
                Cell c5 = row.createCell(5);
                c5.setCellValue(ReporteFilterHelper.sanitizarParaExcel(item.getSolicitante()));
                c5.setCellStyle(styleDataNormal);

                // 7. N° Documento
                Cell c6 = row.createCell(6);
                c6.setCellValue(ReporteFilterHelper.sanitizarParaExcel(item.getSolicitanteDoc()));
                c6.setCellStyle(styleDataCenter);

                // 8. Dependencia Responsable
                Cell c7 = row.createCell(7);
                c7.setCellValue(ReporteFilterHelper.sanitizarParaExcel(item.getOficina()));
                c7.setCellStyle(styleDataNormal);

                // 9. Prioridad
                Cell c8 = row.createCell(8);
                c8.setCellValue(item.getPrioridad());
                c8.setCellStyle(styleDataCenter);

                // 10. Canal
                Cell c9 = row.createCell(9);
                c9.setCellValue(item.getCanalIngreso());
                c9.setCellStyle(styleDataCenter);

                // 11. Estado Actual
                Cell c10 = row.createCell(10);
                c10.setCellValue(item.getEstado());
                c10.setCellStyle(styleDataCenter);

                // 12. Días Transcurridos
                Cell c11 = row.createCell(11);
                c11.setCellValue(item.getDiasTranscurridos() != null ? item.getDiasTranscurridos() : 0);
                c11.setCellStyle(styleDataRight);

                // 13. Fecha Cierre
                Cell c12 = row.createCell(12);
                c12.setCellValue(item.getFechaCierre() != null ? item.getFechaCierre().format(FORMATO_FECHA) : "-");
                c12.setCellStyle(styleDataCenter);
            }

            // Congelar encabezados
            sheet.createFreezePane(0, 8);

            // Ajustar anchos automáticos
            sheet.setColumnWidth(0, 5 * 256);
            sheet.setColumnWidth(1, 18 * 256);
            sheet.setColumnWidth(2, 13 * 256);
            sheet.setColumnWidth(3, 28 * 256);
            sheet.setColumnWidth(4, 38 * 256);
            sheet.setColumnWidth(5, 26 * 256);
            sheet.setColumnWidth(6, 14 * 256);
            sheet.setColumnWidth(7, 26 * 256);
            sheet.setColumnWidth(8, 12 * 256);
            sheet.setColumnWidth(9, 12 * 256);
            sheet.setColumnWidth(10, 16 * 256);
            sheet.setColumnWidth(11, 12 * 256);
            sheet.setColumnWidth(12, 13 * 256);

            workbook.write(baos);
            byte[] bytes = baos.toByteArray();

            // Auditoría de Exportación
            long duracion = System.currentTimeMillis() - inicioMs;
            exportacionRepository.save(ReporteExportacion.builder()
                    .uuid(UUID.randomUUID())
                    .usuario(usuario)
                    .tipoReporte("REPORTE_GENERAL_EXPEDIENTES")
                    .formato("EXCEL")
                    .filtrosJson("periodo=" + (filtro != null ? filtro.getPeriodo() : "DEFAULT"))
                    .cantidadRegistros((int) pagina.getTotalElements())
                    .estado("COMPLETADO")
                    .tiempoGeneracionMs(duracion)
                    .ipOrigen(ip)
                    .userAgent(userAgent)
                    .fechaExportacion(OffsetDateTime.now())
                    .build());

            return bytes;
        } catch (IOException e) {
            log.error("Error generando archivo Excel de Mesa de Partes: {}", e.getMessage(), e);
            throw new RuntimeException("Error en la generación del archivo Excel", e);
        }
    }

    private void crearParMetadato(Row row, int col, String label, String valor, CellStyle styleLabel, CellStyle styleVal) {
        Cell cLabel = row.createCell(col);
        cLabel.setCellValue(label);
        cLabel.setCellStyle(styleLabel);

        Cell cVal = row.createCell(col + 1);
        cVal.setCellValue(valor);
        cVal.setCellStyle(styleVal);
    }
}
