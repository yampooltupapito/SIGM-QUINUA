package pe.gob.municipalidad.sigm.modules.mercados.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoReporteMercado;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("mercadoExportacionExcelServiceImpl")
public class ExportacionExcelServiceImpl implements ExportacionExcelService {

    private final IndicadoresMercadosService indicadoresService;
    private final ReporteOcupacionService reporteOcupacionService;
    private final ReporteMercadosService reporteMercadosService;
    private final ReporteRecaudacionService reporteRecaudacionService;
    private final ReporteOperativoService reporteOperativoService;

    public ExportacionExcelServiceImpl(
            IndicadoresMercadosService indicadoresService,
            ReporteOcupacionService reporteOcupacionService,
            ReporteMercadosService reporteMercadosService,
            ReporteRecaudacionService reporteRecaudacionService,
            ReporteOperativoService reporteOperativoService) {
        this.indicadoresService = indicadoresService;
        this.reporteOcupacionService = reporteOcupacionService;
        this.reporteMercadosService = reporteMercadosService;
        this.reporteRecaudacionService = reporteRecaudacionService;
        this.reporteOperativoService = reporteOperativoService;
    }

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public byte[] generarReporteExcel(TipoReporteMercado tipoReporte, ReporteMercadoFilter filtro, boolean puedeVerDocumentos) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(tipoReporte != null ? tipoReporte.name() : "REPORTE");
            sheet.setDisplayGridlines(true);

            // Estilos
            StylesHolder styles = createStyles(workbook);

            // Encabezado institucional
            int rowIdx = crearEncabezadoInstitucional(sheet, styles, tipoReporte != null ? tipoReporte.getTitulo() : "REPORTE GENERAL");

            // Generar contenido por tipo de reporte
            switch (tipoReporte != null ? tipoReporte : TipoReporteMercado.EJECUTIVO) {
                case EJECUTIVO:
                    generarContenidoEjecutivo(sheet, styles, filtro, rowIdx);
                    break;
                case OCUPACION:
                    generarContenidoOcupacion(sheet, styles, filtro, rowIdx);
                    break;
                case PUESTOS:
                    generarContenidoPuestos(sheet, styles, filtro, rowIdx);
                    break;
                case COMERCIANTES:
                    generarContenidoComerciantes(sheet, styles, filtro, rowIdx, puedeVerDocumentos);
                    break;
                case ASIGNACIONES:
                case CONTRATOS:
                    generarContenidoContratos(sheet, styles, filtro, rowIdx);
                    break;
                case AUTORIZACIONES:
                    generarContenidoAutorizaciones(sheet, styles, filtro, rowIdx);
                    break;
                case VENCIMIENTOS:
                    generarContenidoVencimientos(sheet, styles, filtro, rowIdx);
                    break;
                case OBLIGACIONES:
                case DEUDAS:
                    generarContenidoDeudas(sheet, styles, filtro, rowIdx);
                    break;
                case RECAUDACION:
                    generarContenidoRecaudacion(sheet, styles, filtro, rowIdx);
                    break;
                case INSPECCIONES:
                    generarContenidoInspecciones(sheet, styles, filtro, rowIdx);
                    break;
                case INCIDENCIAS:
                    generarContenidoIncidencias(sheet, styles, filtro, rowIdx);
                    break;
                case RENOVACIONES:
                    generarContenidoRenovaciones(sheet, styles, filtro, rowIdx);
                    break;
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error al generar reporte Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar archivo Excel", e);
        }
    }

    private int crearEncabezadoInstitucional(Sheet sheet, StylesHolder styles, String tituloReporte) {
        Row row0 = sheet.createRow(0);
        Cell cell0 = row0.createCell(0);
        cell0.setCellValue("MUNICIPALIDAD DISTRITAL DE QUINUA");
        cell0.setCellStyle(styles.titleStyle);

        Row row1 = sheet.createRow(1);
        Cell cell1 = row1.createCell(0);
        cell1.setCellValue("SISTEMA INTEGRAL DE GESTIÓN MUNICIPAL — MERCADOS MUNICIPALES");
        cell1.setCellStyle(styles.subtitleStyle);

        Row row2 = sheet.createRow(2);
        Cell cell2 = row2.createCell(0);
        cell2.setCellValue(tituloReporte.toUpperCase());
        cell2.setCellStyle(styles.sectionStyle);

        Row row3 = sheet.createRow(3);
        Cell cell3 = row3.createCell(0);
        cell3.setCellValue("Generado el: " + LocalDateTime.now().format(FORMATO_FECHA_HORA));
        cell3.setCellStyle(styles.metaStyle);

        return 5; // Retorna índice de fila inicial para los datos
    }

    private void generarContenidoEjecutivo(Sheet sheet, StylesHolder styles, ReporteMercadoFilter filtro, int startRow) {
        DashboardMercadosResponse db = indicadoresService.obtenerDashboard(filtro);
        String[] columnas = {"INDICADOR INSTITUCIONAL", "VALOR REGISTRADO", "UNIDAD / DETALLE"};
        crearFilaEncabezado(sheet, startRow, columnas, styles.headerStyle);

        int r = startRow + 1;
        crearFilaKpi(sheet, r++, "Total de Mercados Registrados", String.valueOf(db.getTotalMercados()), "Mercados", styles);
        crearFilaKpi(sheet, r++, "Mercados Activos", String.valueOf(db.getMercadosActivos()), "Mercados en operación", styles);
        crearFilaKpi(sheet, r++, "Total de Sectores Comerciales", String.valueOf(db.getTotalSectores()), "Sectores", styles);
        crearFilaKpi(sheet, r++, "Total de Pabellones", String.valueOf(db.getTotalPabellones()), "Pabellones", styles);
        crearFilaKpi(sheet, r++, "Total de Puestos Comerciales", String.valueOf(db.getTotalPuestos()), "Puestos catastrados", styles);
        crearFilaKpi(sheet, r++, "Puestos Ocupados", String.valueOf(db.getPuestosOcupados()), "Puestos con comerciante", styles);
        crearFilaKpi(sheet, r++, "Puestos Disponibles", String.valueOf(db.getPuestosDisponibles()), "Puestos habilitados", styles);
        crearFilaKpi(sheet, r++, "Tasa de Ocupación Global", db.getPorcentajeOcupacionTexto(), "Ocupación efectiva", styles);
        crearFilaKpi(sheet, r++, "Comerciantes Padrón Oficial", String.valueOf(db.getTotalComerciantes()), "Comerciantes titulares", styles);
        crearFilaKpi(sheet, r++, "Contratos Vigentes", String.valueOf(db.getContratosVigentes()), "Instrumentos jurídicos", styles);
        crearFilaKpi(sheet, r++, "Autorizaciones Vigentes", String.valueOf(db.getAutorizacionesVigentes()), "Licencias de puesto", styles);
        crearFilaKpi(sheet, r++, "Obligaciones Pendientes", String.valueOf(db.getObligacionesPendientes()), "Recibos por amortizar", styles);
        crearFilaKpi(sheet, r++, "Recaudación Neta Confirmada", "S/ " + db.getRecaudacionPeriodo().toPlainString(), "Monto neto auditado", styles);
        crearFilaKpi(sheet, r++, "Inspecciones Realizadas", String.valueOf(db.getInspeccionesRealizadas()), "Operativos de control", styles);
        crearFilaKpi(sheet, r++, "Incidencias Abiertas", String.valueOf(db.getIncidenciasAbiertas()), "En atención / pendientes", styles);

        autoAjustarColumnas(sheet, columnas.length);
    }

    private void generarContenidoOcupacion(Sheet sheet, StylesHolder styles, ReporteMercadoFilter filtro, int startRow) {
        IndicadorOcupacionResponse resp = reporteOcupacionService.obtenerIndicadorOcupacion(filtro);
        String[] columnas = {"MERCADO / SECTOR / PABELLÓN", "TOTAL PUESTOS", "OCUPADOS", "DISPONIBLES", "MANTENIMIENTO", "TASA OCUPACIÓN"};
        crearFilaEncabezado(sheet, startRow, columnas, styles.headerStyle);

        int r = startRow + 1;
        // Fila resumen global
        Row rowTotal = sheet.createRow(r++);
        crearCeldaSegura(rowTotal, 0, "CONSOLIDADO SELECCIONADO", styles.boldStyle);
        crearCeldaSegura(rowTotal, 1, String.valueOf(resp.getTotalPuestos()), styles.boldStyle);
        crearCeldaSegura(rowTotal, 2, String.valueOf(resp.getPuestosOcupados()), styles.boldStyle);
        crearCeldaSegura(rowTotal, 3, String.valueOf(resp.getPuestosDisponibles()), styles.boldStyle);
        crearCeldaSegura(rowTotal, 4, String.valueOf(resp.getPuestosMantenimiento() + resp.getPuestosClausurados()), styles.boldStyle);
        crearCeldaSegura(rowTotal, 5, resp.getPorcentajeOcupacionTexto(), styles.boldStyle);

        // Desglose por mercado
        for (Map<String, Object> item : resp.getOcupacionPorMercado()) {
            Row row = sheet.createRow(r++);
            CellStyle style = (r % 2 == 0) ? styles.altStyle : styles.normalStyle;
            crearCeldaSegura(row, 0, String.valueOf(item.getOrDefault("mercadoNombre", "-")), style);
            crearCeldaSegura(row, 1, String.valueOf(item.getOrDefault("total", 0)), style);
            crearCeldaSegura(row, 2, String.valueOf(item.getOrDefault("ocupados", 0)), style);
            crearCeldaSegura(row, 3, String.valueOf(item.getOrDefault("disponibles", 0)), style);
            crearCeldaSegura(row, 4, String.valueOf(item.getOrDefault("mantenimiento", 0)), style);
            crearCeldaSegura(row, 5, String.valueOf(item.getOrDefault("porcentaje", "0")) + "%", style);
        }

        autoAjustarColumnas(sheet, columnas.length);
    }

    private void generarContenidoPuestos(Sheet sheet, StylesHolder styles, ReporteMercadoFilter filtro, int startRow) {
        List<PuestoReporteItem> lista = reporteOcupacionService.listarTodosPuestos(filtro);
        String[] columnas = {"N°", "CÓDIGO", "MERCADO", "SECTOR", "PABELLÓN", "TIPO PUESTO", "NIVEL", "ÁREA (M²)", "ESTADO FÍSICO", "ESTADO", "OCUPANTE ACTUAL"};
        crearFilaEncabezado(sheet, startRow, columnas, styles.headerStyle);

        int r = startRow + 1;
        int count = 1;
        for (PuestoReporteItem p : lista) {
            Row row = sheet.createRow(r++);
            CellStyle style = (count % 2 == 0) ? styles.altStyle : styles.normalStyle;
            crearCeldaSegura(row, 0, String.valueOf(count++), style);
            crearCeldaSegura(row, 1, p.getCodigo(), style);
            crearCeldaSegura(row, 2, p.getMercadoNombre(), style);
            crearCeldaSegura(row, 3, p.getSectorNombre(), style);
            crearCeldaSegura(row, 4, p.getPabellonNombre(), style);
            crearCeldaSegura(row, 5, p.getTipoPuesto(), style);
            crearCeldaSegura(row, 6, p.getNivel() != null ? p.getNivel().toString() : "1", style);
            crearCeldaSegura(row, 7, p.getArea() != null ? p.getArea().toPlainString() : "0.00", style);
            crearCeldaSegura(row, 8, p.getEstadoFisico(), style);
            crearCeldaSegura(row, 9, p.getEstado(), style);
            crearCeldaSegura(row, 10, p.getOcupanteActual(), style);
        }

        autoAjustarColumnas(sheet, columnas.length);
    }

    private void generarContenidoComerciantes(Sheet sheet, StylesHolder styles, ReporteMercadoFilter filtro, int startRow, boolean puedeVerDoc) {
        List<ComercianteReporteItem> lista = reporteMercadosService.listarTodosComerciantes(filtro, puedeVerDoc);
        String[] columnas = {"N°", "CÓDIGO", "TIPO PERSONA", "DOC. IDENTIDAD", "NOMBRES / RAZÓN SOCIAL", "TELÉFONO", "MERCADO", "PUESTOS", "ASIGNACIONES VIGENTES", "ESTADO"};
        crearFilaEncabezado(sheet, startRow, columnas, styles.headerStyle);

        int r = startRow + 1;
        int count = 1;
        for (ComercianteReporteItem c : lista) {
            Row row = sheet.createRow(r++);
            CellStyle style = (count % 2 == 0) ? styles.altStyle : styles.normalStyle;
            crearCeldaSegura(row, 0, String.valueOf(count++), style);
            crearCeldaSegura(row, 1, c.getCodigo(), style);
            crearCeldaSegura(row, 2, c.getTipoPersona(), style);
            crearCeldaSegura(row, 3, c.getNumeroDocumento(), style);
            crearCeldaSegura(row, 4, c.getRazonSocialONombre(), style);
            crearCeldaSegura(row, 5, c.getTelefono(), style);
            crearCeldaSegura(row, 6, c.getMercadoNombre(), style);
            crearCeldaSegura(row, 7, String.valueOf(c.getPuestosAsignados()), style);
            crearCeldaSegura(row, 8, String.valueOf(c.getAsignacionesVigentes()), style);
            crearCeldaSegura(row, 9, c.getEstado(), style);
        }

        autoAjustarColumnas(sheet, columnas.length);
    }

    private void generarContenidoContratos(Sheet sheet, StylesHolder styles, ReporteMercadoFilter filtro, int startRow) {
        List<ContratoReporteItem> lista = reporteMercadosService.listarTodosContratos(filtro);
        String[] columnas = {"N°", "CÓDIGO CONTRATO", "MERCADO", "PUESTO", "COMERCIANTE", "DOC. IDENTIDAD", "MODALIDAD", "INICIO", "FIN", "MERCED COND.", "ESTADO"};
        crearFilaEncabezado(sheet, startRow, columnas, styles.headerStyle);

        int r = startRow + 1;
        int count = 1;
        BigDecimal totalMerced = BigDecimal.ZERO;
        for (ContratoReporteItem c : lista) {
            Row row = sheet.createRow(r++);
            CellStyle style = (count % 2 == 0) ? styles.altStyle : styles.normalStyle;
            crearCeldaSegura(row, 0, String.valueOf(count++), style);
            crearCeldaSegura(row, 1, c.getCodigo(), style);
            crearCeldaSegura(row, 2, c.getMercadoNombre(), style);
            crearCeldaSegura(row, 3, c.getPuestoCodigo(), style);
            crearCeldaSegura(row, 4, c.getComercianteNombre(), style);
            crearCeldaSegura(row, 5, c.getNumeroDocumento(), style);
            crearCeldaSegura(row, 6, c.getModalidad(), style);
            crearCeldaSegura(row, 7, formatearFecha(c.getFechaInicio()), style);
            crearCeldaSegura(row, 8, formatearFecha(c.getFechaFin()), style);
            crearCeldaSegura(row, 9, c.getImporteMerced() != null ? "S/ " + c.getImporteMerced().toPlainString() : "S/ 0.00", style);
            crearCeldaSegura(row, 10, c.getEstado(), style);
            if (c.getImporteMerced() != null) {
                totalMerced = totalMerced.add(c.getImporteMerced());
            }
        }

        // Fila total
        Row rTot = sheet.createRow(r);
        crearCeldaSegura(rTot, 0, "TOTAL GENERAL", styles.boldStyle);
        crearCeldaSegura(rTot, 9, "S/ " + totalMerced.toPlainString(), styles.boldStyle);

        autoAjustarColumnas(sheet, columnas.length);
    }

    private void generarContenidoAutorizaciones(Sheet sheet, StylesHolder styles, ReporteMercadoFilter filtro, int startRow) {
        List<ContratoReporteItem> lista = reporteMercadosService.listarTodasAutorizaciones(filtro);
        String[] columnas = {"N°", "CÓDIGO AUTORIZACIÓN", "MERCADO", "PUESTO", "TITULAR", "DOC. IDENTIDAD", "TIPO AUTORIZACIÓN", "EMISIÓN", "VIGENCIA HASTA", "ESTADO"};
        crearFilaEncabezado(sheet, startRow, columnas, styles.headerStyle);

        int r = startRow + 1;
        int count = 1;
        for (ContratoReporteItem a : lista) {
            Row row = sheet.createRow(r++);
            CellStyle style = (count % 2 == 0) ? styles.altStyle : styles.normalStyle;
            crearCeldaSegura(row, 0, String.valueOf(count++), style);
            crearCeldaSegura(row, 1, a.getCodigo(), style);
            crearCeldaSegura(row, 2, a.getMercadoNombre(), style);
            crearCeldaSegura(row, 3, a.getPuestoCodigo(), style);
            crearCeldaSegura(row, 4, a.getComercianteNombre(), style);
            crearCeldaSegura(row, 5, a.getNumeroDocumento(), style);
            crearCeldaSegura(row, 6, a.getModalidad(), style);
            crearCeldaSegura(row, 7, formatearFecha(a.getFechaInicio()), style);
            crearCeldaSegura(row, 8, formatearFecha(a.getFechaFin()), style);
            crearCeldaSegura(row, 9, a.getEstado(), style);
        }

        autoAjustarColumnas(sheet, columnas.length);
    }

    private void generarContenidoVencimientos(Sheet sheet, StylesHolder styles, ReporteMercadoFilter filtro, int startRow) {
        List<ContratoReporteItem> lista = reporteMercadosService.listarTodosVencimientos(filtro);
        String[] columnas = {"N°", "INSTRUMENTO", "CÓDIGO", "MERCADO", "PUESTO", "COMERCIANTE", "FECHA VENCIMIENTO", "DÍAS RESTANTES", "ESTADO"};
        crearFilaEncabezado(sheet, startRow, columnas, styles.headerStyle);

        int r = startRow + 1;
        int count = 1;
        for (ContratoReporteItem v : lista) {
            Row row = sheet.createRow(r++);
            CellStyle style = (count % 2 == 0) ? styles.altStyle : styles.normalStyle;
            crearCeldaSegura(row, 0, String.valueOf(count++), style);
            crearCeldaSegura(row, 1, v.getModalidad(), style);
            crearCeldaSegura(row, 2, v.getCodigo(), style);
            crearCeldaSegura(row, 3, v.getMercadoNombre(), style);
            crearCeldaSegura(row, 4, v.getPuestoCodigo(), style);
            crearCeldaSegura(row, 5, v.getComercianteNombre(), style);
            crearCeldaSegura(row, 6, formatearFecha(v.getFechaFin()), style);
            crearCeldaSegura(row, 7, String.valueOf(v.getDiasRestantes()), style);
            crearCeldaSegura(row, 8, v.getEstado(), style);
        }

        autoAjustarColumnas(sheet, columnas.length);
    }

    private void generarContenidoRecaudacion(Sheet sheet, StylesHolder styles, ReporteMercadoFilter filtro, int startRow) {
        List<RecaudacionReporteItem> lista = reporteRecaudacionService.listarTodaRecaudacion(filtro);
        String[] columnas = {"N°", "FECHA PAGO", "N° OPERACIÓN", "MERCADO", "PUESTO", "COMERCIANTE", "CONCEPTO", "MEDIO DE PAGO", "MONTO APLICADO (S/)", "CAJA"};
        crearFilaEncabezado(sheet, startRow, columnas, styles.headerStyle);

        int r = startRow + 1;
        int count = 1;
        BigDecimal sumaTotal = BigDecimal.ZERO;
        for (RecaudacionReporteItem item : lista) {
            Row row = sheet.createRow(r++);
            CellStyle style = (count % 2 == 0) ? styles.altStyle : styles.normalStyle;
            crearCeldaSegura(row, 0, String.valueOf(count++), style);
            crearCeldaSegura(row, 1, formatearOffsetDate(item.getFechaPago()), style);
            crearCeldaSegura(row, 2, item.getReferenciaOperacion() != null ? item.getReferenciaOperacion() : item.getCodigoPago(), style);
            crearCeldaSegura(row, 3, item.getMercadoNombre(), style);
            crearCeldaSegura(row, 4, item.getPuestoCodigo(), style);
            crearCeldaSegura(row, 5, item.getComercianteNombre(), style);
            crearCeldaSegura(row, 6, item.getConceptoNombre(), style);
            crearCeldaSegura(row, 7, item.getMedioPago(), style);
            crearCeldaSegura(row, 8, item.getMontoAplicado() != null ? "S/ " + item.getMontoAplicado().toPlainString() : "S/ 0.00", style);
            crearCeldaSegura(row, 9, item.getSesionCajaCodigo() != null ? item.getSesionCajaCodigo() : "-", style);
            if (item.getMontoAplicado() != null) {
                sumaTotal = sumaTotal.add(item.getMontoAplicado());
            }
        }

        Row rTot = sheet.createRow(r);
        crearCeldaSegura(rTot, 0, "RECAUDACIÓN TOTAL CONFIRMADA", styles.boldStyle);
        crearCeldaSegura(rTot, 8, "S/ " + sumaTotal.toPlainString(), styles.boldStyle);

        autoAjustarColumnas(sheet, columnas.length);
    }

    private void generarContenidoDeudas(Sheet sheet, StylesHolder styles, ReporteMercadoFilter filtro, int startRow) {
        List<DeudaReporteItem> lista = reporteRecaudacionService.listarTodasDeudas(filtro);
        String[] columnas = {"N°", "COMERCIANTE", "DOC. IDENTIDAD", "MERCADO", "PUESTO", "CONCEPTO", "FECHA EMISIÓN", "MONTO ORIGINAL", "SALDO PENDIENTE", "VENCIMIENTO", "ANTIGÜEDAD", "ESTADO"};
        crearFilaEncabezado(sheet, startRow, columnas, styles.headerStyle);

        int r = startRow + 1;
        int count = 1;
        BigDecimal totalDeuda = BigDecimal.ZERO;
        for (DeudaReporteItem d : lista) {
            Row row = sheet.createRow(r++);
            CellStyle style = (count % 2 == 0) ? styles.altStyle : styles.normalStyle;
            crearCeldaSegura(row, 0, String.valueOf(count++), style);
            crearCeldaSegura(row, 1, d.getComercianteNombre(), style);
            crearCeldaSegura(row, 2, d.getDocumentoComerciante(), style);
            crearCeldaSegura(row, 3, d.getMercadoNombre(), style);
            crearCeldaSegura(row, 4, d.getPuestoCodigo(), style);
            crearCeldaSegura(row, 5, d.getConceptoNombre(), style);
            crearCeldaSegura(row, 6, formatearFecha(d.getFechaEmision()), style);
            crearCeldaSegura(row, 7, d.getMontoOriginal() != null ? "S/ " + d.getMontoOriginal().toPlainString() : "S/ 0.00", style);
            crearCeldaSegura(row, 8, d.getSaldoPendiente() != null ? "S/ " + d.getSaldoPendiente().toPlainString() : "S/ 0.00", style);
            crearCeldaSegura(row, 9, formatearFecha(d.getFechaVencimiento()), style);
            crearCeldaSegura(row, 10, d.getRangoAntiguedad(), style);
            crearCeldaSegura(row, 11, d.getEstado(), style);
            if (d.getSaldoPendiente() != null) {
                totalDeuda = totalDeuda.add(d.getSaldoPendiente());
            }
        }

        Row rTot = sheet.createRow(r);
        crearCeldaSegura(rTot, 0, "TOTAL DEUDA PENDIENTE", styles.boldStyle);
        crearCeldaSegura(rTot, 8, "S/ " + totalDeuda.toPlainString(), styles.boldStyle);

        autoAjustarColumnas(sheet, columnas.length);
    }

    private void generarContenidoInspecciones(Sheet sheet, StylesHolder styles, ReporteMercadoFilter filtro, int startRow) {
        List<InspeccionReporteItem> lista = reporteOperativoService.listarTodasInspecciones(filtro);
        String[] columnas = {"N°", "CÓDIGO ACTA", "MERCADO", "TIPO INSPECCIÓN", "INSPECTOR", "FECHA PROGRAMADA", "CALIFICACIÓN", "HALLAZGOS", "MEDIDAS PENDIENTES", "ESTADO"};
        crearFilaEncabezado(sheet, startRow, columnas, styles.headerStyle);

        int r = startRow + 1;
        int count = 1;
        for (InspeccionReporteItem ins : lista) {
            Row row = sheet.createRow(r++);
            CellStyle style = (count % 2 == 0) ? styles.altStyle : styles.normalStyle;
            crearCeldaSegura(row, 0, String.valueOf(count++), style);
            crearCeldaSegura(row, 1, ins.getCodigoInspeccion(), style);
            crearCeldaSegura(row, 2, ins.getMercadoNombre(), style);
            crearCeldaSegura(row, 3, ins.getTipoInspeccion(), style);
            crearCeldaSegura(row, 4, ins.getInspectorNombre(), style);
            crearCeldaSegura(row, 5, formatearFecha(ins.getFechaProgramada()), style);
            crearCeldaSegura(row, 6, ins.getCalificacionFinal() != null ? ins.getCalificacionFinal() : ins.getEstado(), style);
            crearCeldaSegura(row, 7, String.valueOf(ins.getCantidadHallazgos()), style);
            crearCeldaSegura(row, 8, String.valueOf(ins.getCantidadMedidasPendientes()), style);
            crearCeldaSegura(row, 9, ins.getEstado(), style);
        }

        autoAjustarColumnas(sheet, columnas.length);
    }

    private void generarContenidoIncidencias(Sheet sheet, StylesHolder styles, ReporteMercadoFilter filtro, int startRow) {
        List<IncidenciaReporteItem> lista = reporteOperativoService.listarTodasIncidencias(filtro);
        String[] columnas = {"N°", "CÓDIGO", "MERCADO", "TIPO INCIDENCIA", "PRIORIDAD", "ORIGEN / REPORTANTE", "FECHA REPORTE", "ESTADO", "TIEMPO HORAS"};
        crearFilaEncabezado(sheet, startRow, columnas, styles.headerStyle);

        int r = startRow + 1;
        int count = 1;
        for (IncidenciaReporteItem inc : lista) {
            Row row = sheet.createRow(r++);
            CellStyle style = (count % 2 == 0) ? styles.altStyle : styles.normalStyle;
            crearCeldaSegura(row, 0, String.valueOf(count++), style);
            crearCeldaSegura(row, 1, inc.getCodigoIncidencia(), style);
            crearCeldaSegura(row, 2, inc.getMercadoNombre(), style);
            crearCeldaSegura(row, 3, inc.getTipoIncidencia(), style);
            crearCeldaSegura(row, 4, inc.getPrioridad(), style);
            crearCeldaSegura(row, 5, inc.getOrigen() != null ? inc.getOrigen() : "-", style);
            crearCeldaSegura(row, 6, formatearOffsetDate(inc.getFechaReporte()), style);
            crearCeldaSegura(row, 7, inc.getEstado(), style);
            crearCeldaSegura(row, 8, inc.getTiempoAtencionHoras() != null ? inc.getTiempoAtencionHoras().toString() : "-", style);
        }

        autoAjustarColumnas(sheet, columnas.length);
    }

    private void generarContenidoRenovaciones(Sheet sheet, StylesHolder styles, ReporteMercadoFilter filtro, int startRow) {
        List<RenovacionReporteItem> lista = reporteOperativoService.listarTodasRenovaciones(filtro);
        String[] columnas = {"N°", "CÓDIGO TRÁMITE", "TIPO TRÁMITE", "MERCADO", "PUESTO", "COMERCIANTE TITULAR", "NUEVO TITULAR", "FECHA SOLICITUD", "ESTADO", "RESULTADO"};
        crearFilaEncabezado(sheet, startRow, columnas, styles.headerStyle);

        int r = startRow + 1;
        int count = 1;
        for (RenovacionReporteItem ren : lista) {
            Row row = sheet.createRow(r++);
            CellStyle style = (count % 2 == 0) ? styles.altStyle : styles.normalStyle;
            crearCeldaSegura(row, 0, String.valueOf(count++), style);
            crearCeldaSegura(row, 1, ren.getCodigoSolicitud(), style);
            crearCeldaSegura(row, 2, ren.getTipoSolicitud(), style);
            crearCeldaSegura(row, 3, ren.getMercadoNombre(), style);
            crearCeldaSegura(row, 4, ren.getPuestoCodigo(), style);
            crearCeldaSegura(row, 5, ren.getComercianteActual(), style);
            crearCeldaSegura(row, 6, ren.getComerciantePropuesto() != null ? ren.getComerciantePropuesto() : "-", style);
            crearCeldaSegura(row, 7, formatearFecha(ren.getFechaPresentacion()), style);
            crearCeldaSegura(row, 8, ren.getEstado(), style);
            crearCeldaSegura(row, 9, ren.getResultado() != null ? ren.getResultado() : "-", style);
        }

        autoAjustarColumnas(sheet, columnas.length);
    }

    private void crearFilaKpi(Sheet sheet, int rowIdx, String kpi, String valor, String unidad, StylesHolder styles) {
        Row row = sheet.createRow(rowIdx);
        CellStyle style = (rowIdx % 2 == 0) ? styles.altStyle : styles.normalStyle;
        crearCeldaSegura(row, 0, kpi, style);
        crearCeldaSegura(row, 1, valor, styles.boldStyle);
        crearCeldaSegura(row, 2, unidad, style);
    }

    private void crearFilaEncabezado(Sheet sheet, int rowIdx, String[] columnas, CellStyle headerStyle) {
        Row row = sheet.createRow(rowIdx);
        for (int i = 0; i < columnas.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void crearCeldaSegura(Row row, int colIdx, String valor, CellStyle style) {
        Cell cell = row.createCell(colIdx);
        cell.setCellValue(sanitizarFormula(valor));
        cell.setCellStyle(style);
    }

    private String sanitizarFormula(String valor) {
        if (valor == null) {
            return "-";
        }
        String trimmed = valor.trim();
        if (!trimmed.isEmpty()) {
            char firstChar = trimmed.charAt(0);
            if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@' || firstChar == '\t' || firstChar == '\r') {
                return "'" + trimmed;
            }
        }
        return trimmed;
    }

    private String formatearFecha(LocalDate fecha) {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "-";
    }

    private String formatearOffsetDate(OffsetDateTime fecha) {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "-";
    }

    private void autoAjustarColumnas(Sheet sheet, int numCols) {
        for (int i = 0; i < numCols; i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) < 3000) {
                sheet.setColumnWidth(i, 3000);
            }
        }
    }

    private StylesHolder createStyles(XSSFWorkbook workbook) {
        StylesHolder s = new StylesHolder();

        // Fuentes
        XSSFFont titleFont = workbook.createFont();
        titleFont.setFontName("Calibri");
        titleFont.setFontHeightInPoints((short) 13);
        titleFont.setBold(true);
        titleFont.setColor(new XSSFColor(new java.awt.Color(29, 78, 216), null));

        XSSFFont subtitleFont = workbook.createFont();
        subtitleFont.setFontName("Calibri");
        subtitleFont.setFontHeightInPoints((short) 10);
        subtitleFont.setColor(new XSSFColor(new java.awt.Color(71, 85, 105), null));

        XSSFFont sectionFont = workbook.createFont();
        sectionFont.setFontName("Calibri");
        sectionFont.setFontHeightInPoints((short) 11);
        sectionFont.setBold(true);
        sectionFont.setColor(new XSSFColor(new java.awt.Color(15, 23, 42), null));

        XSSFFont headerFont = workbook.createFont();
        headerFont.setFontName("Calibri");
        headerFont.setFontHeightInPoints((short) 9);
        headerFont.setBold(true);
        headerFont.setColor(new XSSFColor(new java.awt.Color(255, 255, 255), null));

        XSSFFont regularFont = workbook.createFont();
        regularFont.setFontName("Calibri");
        regularFont.setFontHeightInPoints((short) 9);

        XSSFFont boldFont = workbook.createFont();
        boldFont.setFontName("Calibri");
        boldFont.setFontHeightInPoints((short) 9);
        boldFont.setBold(true);

        // Estilos
        s.titleStyle = workbook.createCellStyle();
        s.titleStyle.setFont(titleFont);

        s.subtitleStyle = workbook.createCellStyle();
        s.subtitleStyle.setFont(subtitleFont);

        s.sectionStyle = workbook.createCellStyle();
        s.sectionStyle.setFont(sectionFont);

        s.metaStyle = workbook.createCellStyle();
        s.metaStyle.setFont(subtitleFont);

        s.headerStyle = workbook.createCellStyle();
        s.headerStyle.setFont(headerFont);
        s.headerStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(30, 41, 59), null));
        s.headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.headerStyle.setAlignment(HorizontalAlignment.CENTER);
        s.headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        s.normalStyle = workbook.createCellStyle();
        s.normalStyle.setFont(regularFont);
        s.normalStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        s.altStyle = workbook.createCellStyle();
        s.altStyle.setFont(regularFont);
        s.altStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(241, 245, 249), null));
        s.altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.altStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        s.boldStyle = workbook.createCellStyle();
        s.boldStyle.setFont(boldFont);
        s.boldStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        return s;
    }

    private static class StylesHolder {
        XSSFCellStyle titleStyle;
        XSSFCellStyle subtitleStyle;
        XSSFCellStyle sectionStyle;
        XSSFCellStyle metaStyle;
        XSSFCellStyle headerStyle;
        XSSFCellStyle normalStyle;
        XSSFCellStyle altStyle;
        XSSFCellStyle boldStyle;
    }
}
