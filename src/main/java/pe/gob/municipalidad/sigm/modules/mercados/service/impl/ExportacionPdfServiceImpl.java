package pe.gob.municipalidad.sigm.modules.mercados.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoReporteMercado;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("mercadoExportacionPdfServiceImpl")
public class ExportacionPdfServiceImpl implements ExportacionPdfService {

    private final IndicadoresMercadosService indicadoresService;
    private final ReporteOcupacionService reporteOcupacionService;
    private final ReporteMercadosService reporteMercadosService;
    private final ReporteRecaudacionService reporteRecaudacionService;
    private final ReporteOperativoService reporteOperativoService;

    public ExportacionPdfServiceImpl(
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
    public byte[] generarReportePdf(TipoReporteMercado tipoReporte, ReporteMercadoFilter filtro, boolean puedeVerDocumentos) {
        // Landscape A4 for tabular reports
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 25, 25);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent(tipoReporte != null ? tipoReporte.getTitulo() : "REPORTE GENERAL"));
            document.open();

            // Tipografías
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(29, 78, 216));
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, new Color(100, 116, 139));
            Font fontTh = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, Color.WHITE);
            Font fontTd = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, new Color(15, 23, 42));
            Font fontTdBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, new Color(15, 23, 42));

            // Encabezado
            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(pHeader);

            Paragraph pDep = new Paragraph("SUBGERENCIA DE SERVICIOS MUNICIPALES Y DESARROLLO ECONÓMICO LOCAL", fontSub);
            pDep.setAlignment(Element.ALIGN_CENTER);
            document.add(pDep);

            String tituloStr = (tipoReporte != null ? tipoReporte.getTitulo() : "REPORTE").toUpperCase();
            Paragraph pRep = new Paragraph("MERCADOS MUNICIPALES — " + tituloStr, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 41, 59)));
            pRep.setAlignment(Element.ALIGN_CENTER);
            document.add(pRep);

            String filtrosStr = "Período: " + (filtro != null && filtro.getPeriodo() != null ? filtro.getPeriodo().name() : "TODOS")
                    + " | Fecha de corte: " + LocalDateTime.now().format(FORMATO_FECHA_HORA);
            Paragraph pMeta = new Paragraph(filtrosStr, fontSub);
            pMeta.setAlignment(Element.ALIGN_CENTER);
            pMeta.setSpacingAfter(12);
            document.add(pMeta);

            // Generar tabla según el tipo
            switch (tipoReporte != null ? tipoReporte : TipoReporteMercado.EJECUTIVO) {
                case EJECUTIVO:
                    renderizarEjecutivo(document, filtro, fontTh, fontTd, fontTdBold);
                    break;
                case OCUPACION:
                    renderizarOcupacion(document, filtro, fontTh, fontTd, fontTdBold);
                    break;
                case PUESTOS:
                    renderizarPuestos(document, filtro, fontTh, fontTd);
                    break;
                case COMERCIANTES:
                    renderizarComerciantes(document, filtro, puedeVerDocumentos, fontTh, fontTd);
                    break;
                case ASIGNACIONES:
                case CONTRATOS:
                    renderizarContratos(document, filtro, fontTh, fontTd, fontTdBold);
                    break;
                case AUTORIZACIONES:
                    renderizarAutorizaciones(document, filtro, fontTh, fontTd);
                    break;
                case VENCIMIENTOS:
                    renderizarVencimientos(document, filtro, fontTh, fontTd);
                    break;
                case OBLIGACIONES:
                case DEUDAS:
                    renderizarDeudas(document, filtro, fontTh, fontTd, fontTdBold);
                    break;
                case RECAUDACION:
                    renderizarRecaudacion(document, filtro, fontTh, fontTd, fontTdBold);
                    break;
                case INSPECCIONES:
                    renderizarInspecciones(document, filtro, fontTh, fontTd);
                    break;
                case INCIDENCIAS:
                    renderizarIncidencias(document, filtro, fontTh, fontTd);
                    break;
                case RENOVACIONES:
                    renderizarRenovaciones(document, filtro, fontTh, fontTd);
                    break;
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar reporte PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar archivo PDF", e);
        }
    }

    private void renderizarEjecutivo(Document document, ReporteMercadoFilter filtro, Font fontTh, Font fontTd, Font fontTdBold) throws DocumentException {
        DashboardMercadosResponse db = indicadoresService.obtenerDashboard(filtro);
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(95);
        table.setWidths(new float[]{45f, 25f, 30f});
        table.setHeaderRows(1);

        agregarCabeceras(table, new String[]{"INDICADOR INSTITUCIONAL", "VALOR REGISTRADO", "ALCANCE / DETALLE"}, fontTh);

        agregarFilaKpi(table, "Total de Mercados Municipales", String.valueOf(db.getTotalMercados()), "Catastro Municipal", fontTd, fontTdBold);
        agregarFilaKpi(table, "Mercados Activos en Operación", String.valueOf(db.getMercadosActivos()), "Infraestructura Operativa", fontTd, fontTdBold);
        agregarFilaKpi(table, "Total de Sectores Comerciales", String.valueOf(db.getTotalSectores()), "Zonificación Interna", fontTd, fontTdBold);
        agregarFilaKpi(table, "Total de Pabellones", String.valueOf(db.getTotalPabellones()), "Estructura Física", fontTd, fontTdBold);
        agregarFilaKpi(table, "Total de Puestos Comerciales", String.valueOf(db.getTotalPuestos()), "Puestos Registrados", fontTd, fontTdBold);
        agregarFilaKpi(table, "Puestos Ocupados", String.valueOf(db.getPuestosOcupados()), "Con Conductor Activo", fontTd, fontTdBold);
        agregarFilaKpi(table, "Puestos Disponibles", String.valueOf(db.getPuestosDisponibles()), "Habilitados para Adjudicación", fontTd, fontTdBold);
        agregarFilaKpi(table, "Tasa de Ocupación Efectiva", db.getPorcentajeOcupacionTexto(), "Cálculo según Fórmula", fontTd, fontTdBold);
        agregarFilaKpi(table, "Padrón Oficial de Comerciantes", String.valueOf(db.getTotalComerciantes()), "Titulares Registrados", fontTd, fontTdBold);
        agregarFilaKpi(table, "Contratos Vigentes", String.valueOf(db.getContratosVigentes()), "Instrumentos Jurídicos", fontTd, fontTdBold);
        agregarFilaKpi(table, "Autorizaciones Vigentes", String.valueOf(db.getAutorizacionesVigentes()), "Licencias de Puesto", fontTd, fontTdBold);
        agregarFilaKpi(table, "Obligaciones Pendientes", String.valueOf(db.getObligacionesPendientes()), "Liquidaciones Exigibles", fontTd, fontTdBold);
        agregarFilaKpi(table, "Recaudación Neta Confirmada", "S/ " + db.getRecaudacionPeriodo().toPlainString(), "Amortizaciones Auditadas", fontTd, fontTdBold);
        agregarFilaKpi(table, "Inspecciones de Control", String.valueOf(db.getInspeccionesRealizadas()), "Actas en el Período", fontTd, fontTdBold);
        agregarFilaKpi(table, "Incidencias Operativas Abiertas", String.valueOf(db.getIncidenciasAbiertas()), "En Proceso de Atención", fontTd, fontTdBold);

        document.add(table);
    }

    private void renderizarOcupacion(Document document, ReporteMercadoFilter filtro, Font fontTh, Font fontTd, Font fontTdBold) throws DocumentException {
        IndicadorOcupacionResponse resp = reporteOcupacionService.obtenerIndicadorOcupacion(filtro);
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30f, 14f, 14f, 14f, 14f, 14f});
        table.setHeaderRows(1);

        agregarCabeceras(table, new String[]{"MERCADO / DETALLE", "TOTAL PUESTOS", "OCUPADOS", "DISPONIBLES", "MANTENIMIENTO", "TASA OCUPACIÓN"}, fontTh);

        for (Map<String, Object> item : resp.getOcupacionPorMercado()) {
            table.addCell(crearCelda(String.valueOf(item.getOrDefault("mercadoNombre", "-")), fontTd, Element.ALIGN_LEFT));
            table.addCell(crearCelda(String.valueOf(item.getOrDefault("total", 0)), fontTd, Element.ALIGN_CENTER));
            table.addCell(crearCelda(String.valueOf(item.getOrDefault("ocupados", 0)), fontTd, Element.ALIGN_CENTER));
            table.addCell(crearCelda(String.valueOf(item.getOrDefault("disponibles", 0)), fontTd, Element.ALIGN_CENTER));
            table.addCell(crearCelda(String.valueOf(item.getOrDefault("mantenimiento", 0)), fontTd, Element.ALIGN_CENTER));
            table.addCell(crearCelda(String.valueOf(item.getOrDefault("porcentaje", "0")) + "%", fontTdBold, Element.ALIGN_CENTER));
        }

        // Fila Resumen
        table.addCell(crearCeldaResaltada("TOTAL CONSOLIDADO", fontTdBold, Element.ALIGN_LEFT));
        table.addCell(crearCeldaResaltada(String.valueOf(resp.getTotalPuestos()), fontTdBold, Element.ALIGN_CENTER));
        table.addCell(crearCeldaResaltada(String.valueOf(resp.getPuestosOcupados()), fontTdBold, Element.ALIGN_CENTER));
        table.addCell(crearCeldaResaltada(String.valueOf(resp.getPuestosDisponibles()), fontTdBold, Element.ALIGN_CENTER));
        table.addCell(crearCeldaResaltada(String.valueOf(resp.getPuestosMantenimiento() + resp.getPuestosClausurados()), fontTdBold, Element.ALIGN_CENTER));
        table.addCell(crearCeldaResaltada(resp.getPorcentajeOcupacionTexto(), fontTdBold, Element.ALIGN_CENTER));

        document.add(table);
    }

    private void renderizarPuestos(Document document, ReporteMercadoFilter filtro, Font fontTh, Font fontTd) throws DocumentException {
        List<PuestoReporteItem> lista = reporteOcupacionService.listarTodosPuestos(filtro);
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{10f, 16f, 12f, 12f, 10f, 10f, 12f, 18f});
        table.setHeaderRows(1);

        agregarCabeceras(table, new String[]{"CÓDIGO", "MERCADO", "SECTOR", "PABELLÓN", "NIVEL", "ÁREA (M²)", "ESTADO", "OCUPANTE"}, fontTh);

        if (lista.isEmpty()) {
            agregarFilaVacia(table, 8, fontTd);
        } else {
            for (PuestoReporteItem p : lista) {
                table.addCell(crearCelda(p.getCodigo(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(p.getMercadoNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(p.getSectorNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(p.getPabellonNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(p.getNivel() != null ? p.getNivel().toString() : "1", fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(p.getArea() != null ? p.getArea().toPlainString() : "0.00", fontTd, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(p.getEstado(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(p.getOcupanteActual(), fontTd, Element.ALIGN_LEFT));
            }
        }

        document.add(table);
    }

    private void renderizarComerciantes(Document document, ReporteMercadoFilter filtro, boolean puedeVerDoc, Font fontTh, Font fontTd) throws DocumentException {
        List<ComercianteReporteItem> lista = reporteMercadosService.listarTodosComerciantes(filtro, puedeVerDoc);
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{12f, 14f, 26f, 14f, 16f, 9f, 9f});
        table.setHeaderRows(1);

        agregarCabeceras(table, new String[]{"CÓDIGO", "DOC. IDENTIDAD", "NOMBRES / RAZÓN SOCIAL", "TELÉFONO", "MERCADO", "ASIG.", "ESTADO"}, fontTh);

        if (lista.isEmpty()) {
            agregarFilaVacia(table, 7, fontTd);
        } else {
            for (ComercianteReporteItem c : lista) {
                table.addCell(crearCelda(c.getCodigo(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(c.getNumeroDocumento(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(c.getRazonSocialONombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(c.getTelefono(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(c.getMercadoNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(String.valueOf(c.getAsignacionesVigentes()), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(c.getEstado(), fontTd, Element.ALIGN_CENTER));
            }
        }

        document.add(table);
    }

    private void renderizarContratos(Document document, ReporteMercadoFilter filtro, Font fontTh, Font fontTd, Font fontTdBold) throws DocumentException {
        List<ContratoReporteItem> lista = reporteMercadosService.listarTodosContratos(filtro);
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{12f, 14f, 10f, 22f, 11f, 11f, 10f, 10f});
        table.setHeaderRows(1);

        agregarCabeceras(table, new String[]{"CÓDIGO", "MERCADO", "PUESTO", "CONDUCTOR", "INICIO", "FIN", "MERCED", "ESTADO"}, fontTh);

        BigDecimal total = BigDecimal.ZERO;
        if (lista.isEmpty()) {
            agregarFilaVacia(table, 8, fontTd);
        } else {
            for (ContratoReporteItem c : lista) {
                table.addCell(crearCelda(c.getCodigo(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(c.getMercadoNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(c.getPuestoCodigo(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(c.getComercianteNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(formatearFecha(c.getFechaInicio()), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(formatearFecha(c.getFechaFin()), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(c.getImporteMerced() != null ? "S/ " + c.getImporteMerced().toPlainString() : "S/ 0.00", fontTd, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(c.getEstado(), fontTd, Element.ALIGN_CENTER));
                if (c.getImporteMerced() != null) {
                    total = total.add(c.getImporteMerced());
                }
            }
            // Totales
            table.addCell(crearCeldaResaltada("TOTAL GENERAL", fontTdBold, Element.ALIGN_LEFT));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_LEFT));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_LEFT));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_LEFT));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_LEFT));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_LEFT));
            table.addCell(crearCeldaResaltada("S/ " + total.toPlainString(), fontTdBold, Element.ALIGN_RIGHT));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_CENTER));
        }

        document.add(table);
    }

    private void renderizarAutorizaciones(Document document, ReporteMercadoFilter filtro, Font fontTh, Font fontTd) throws DocumentException {
        List<ContratoReporteItem> lista = reporteMercadosService.listarTodasAutorizaciones(filtro);
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{14f, 16f, 10f, 26f, 12f, 12f, 10f});
        table.setHeaderRows(1);

        agregarCabeceras(table, new String[]{"CÓDIGO", "MERCADO", "PUESTO", "TITULAR", "EMISIÓN", "VENCE", "ESTADO"}, fontTh);

        if (lista.isEmpty()) {
            agregarFilaVacia(table, 7, fontTd);
        } else {
            for (ContratoReporteItem a : lista) {
                table.addCell(crearCelda(a.getCodigo(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(a.getMercadoNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(a.getPuestoCodigo(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(a.getComercianteNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(formatearFecha(a.getFechaInicio()), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(formatearFecha(a.getFechaFin()), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(a.getEstado(), fontTd, Element.ALIGN_CENTER));
            }
        }

        document.add(table);
    }

    private void renderizarVencimientos(Document document, ReporteMercadoFilter filtro, Font fontTh, Font fontTd) throws DocumentException {
        List<ContratoReporteItem> lista = reporteMercadosService.listarTodosVencimientos(filtro);
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{14f, 14f, 10f, 26f, 12f, 12f, 12f});
        table.setHeaderRows(1);

        agregarCabeceras(table, new String[]{"TIPO", "CÓDIGO", "PUESTO", "CONDUCTOR", "VENCIMIENTO", "DÍAS REST.", "ESTADO"}, fontTh);

        if (lista.isEmpty()) {
            agregarFilaVacia(table, 7, fontTd);
        } else {
            for (ContratoReporteItem v : lista) {
                table.addCell(crearCelda(v.getModalidad(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(v.getCodigo(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(v.getPuestoCodigo(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(v.getComercianteNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(formatearFecha(v.getFechaFin()), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(String.valueOf(v.getDiasRestantes()), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(v.getEstado(), fontTd, Element.ALIGN_CENTER));
            }
        }

        document.add(table);
    }

    private void renderizarDeudas(Document document, ReporteMercadoFilter filtro, Font fontTh, Font fontTd, Font fontTdBold) throws DocumentException {
        List<DeudaReporteItem> lista = reporteRecaudacionService.listarTodasDeudas(filtro);
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{22f, 10f, 14f, 10f, 12f, 11f, 11f, 10f});
        table.setHeaderRows(1);

        agregarCabeceras(table, new String[]{"CONDUCTOR", "PUESTO", "CONCEPTO", "PERÍODO", "SALDO PEND.", "VENCE", "ANTIGÜEDAD", "ESTADO"}, fontTh);

        BigDecimal total = BigDecimal.ZERO;
        if (lista.isEmpty()) {
            agregarFilaVacia(table, 8, fontTd);
        } else {
            for (DeudaReporteItem d : lista) {
                table.addCell(crearCelda(d.getComercianteNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(d.getPuestoCodigo(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(d.getConceptoNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(d.getFechaEmision() != null ? d.getFechaEmision().toString() : "-", fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(d.getSaldoPendiente() != null ? "S/ " + d.getSaldoPendiente().toPlainString() : "S/ 0.00", fontTd, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(formatearFecha(d.getFechaVencimiento()), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(d.getRangoAntiguedad(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(d.getEstado(), fontTd, Element.ALIGN_CENTER));
                if (d.getSaldoPendiente() != null) {
                    total = total.add(d.getSaldoPendiente());
                }
            }
            // Totales
            table.addCell(crearCeldaResaltada("TOTAL DEUDA PENDIENTE", fontTdBold, Element.ALIGN_LEFT));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_CENTER));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_LEFT));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_CENTER));
            table.addCell(crearCeldaResaltada("S/ " + total.toPlainString(), fontTdBold, Element.ALIGN_RIGHT));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_CENTER));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_CENTER));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_CENTER));
        }

        document.add(table);
    }

    private void renderizarRecaudacion(Document document, ReporteMercadoFilter filtro, Font fontTh, Font fontTd, Font fontTdBold) throws DocumentException {
        List<RecaudacionReporteItem> lista = reporteRecaudacionService.listarTodaRecaudacion(filtro);
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{11f, 13f, 10f, 22f, 14f, 12f, 10f, 8f});
        table.setHeaderRows(1);

        agregarCabeceras(table, new String[]{"FECHA", "OPERACIÓN", "PUESTO", "CONDUCTOR", "CONCEPTO", "MEDIO PAGO", "MONTO APLIC.", "CAJA"}, fontTh);

        BigDecimal total = BigDecimal.ZERO;
        if (lista.isEmpty()) {
            agregarFilaVacia(table, 8, fontTd);
        } else {
            for (RecaudacionReporteItem r : lista) {
                table.addCell(crearCelda(formatearOffsetDate(r.getFechaPago()), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(r.getReferenciaOperacion() != null ? r.getReferenciaOperacion() : r.getCodigoPago(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(r.getPuestoCodigo(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(r.getComercianteNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(r.getConceptoNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(r.getMedioPago(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(r.getMontoAplicado() != null ? "S/ " + r.getMontoAplicado().toPlainString() : "S/ 0.00", fontTd, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(r.getSesionCajaCodigo() != null ? r.getSesionCajaCodigo() : "-", fontTd, Element.ALIGN_CENTER));
                if (r.getMontoAplicado() != null) {
                    total = total.add(r.getMontoAplicado());
                }
            }
            // Totales
            table.addCell(crearCeldaResaltada("TOTAL CONFIRMADO", fontTdBold, Element.ALIGN_LEFT));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_CENTER));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_CENTER));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_LEFT));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_LEFT));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_CENTER));
            table.addCell(crearCeldaResaltada("S/ " + total.toPlainString(), fontTdBold, Element.ALIGN_RIGHT));
            table.addCell(crearCeldaResaltada("", fontTdBold, Element.ALIGN_CENTER));
        }

        document.add(table);
    }

    private void renderizarInspecciones(Document document, ReporteMercadoFilter filtro, Font fontTh, Font fontTd) throws DocumentException {
        List<InspeccionReporteItem> lista = reporteOperativoService.listarTodasInspecciones(filtro);
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{14f, 16f, 14f, 20f, 12f, 12f, 12f});
        table.setHeaderRows(1);

        agregarCabeceras(table, new String[]{"CÓDIGO ACTA", "MERCADO", "TIPO", "INSPECTOR", "FECHA", "RESULTADO", "ESTADO"}, fontTh);

        if (lista.isEmpty()) {
            agregarFilaVacia(table, 7, fontTd);
        } else {
            for (InspeccionReporteItem ins : lista) {
                table.addCell(crearCelda(ins.getCodigoInspeccion(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(ins.getMercadoNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(ins.getTipoInspeccion(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(ins.getInspectorNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(formatearFecha(ins.getFechaProgramada()), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(ins.getCalificacionFinal() != null ? ins.getCalificacionFinal() : ins.getEstado(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(ins.getEstado(), fontTd, Element.ALIGN_CENTER));
            }
        }

        document.add(table);
    }

    private void renderizarIncidencias(Document document, ReporteMercadoFilter filtro, Font fontTh, Font fontTd) throws DocumentException {
        List<IncidenciaReporteItem> lista = reporteOperativoService.listarTodasIncidencias(filtro);
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{14f, 16f, 16f, 12f, 20f, 11f, 11f});
        table.setHeaderRows(1);

        agregarCabeceras(table, new String[]{"CÓDIGO", "MERCADO", "TIPO INCIDENCIA", "PRIORIDAD", "REPORTANTE", "REGISTRO", "ESTADO"}, fontTh);

        if (lista.isEmpty()) {
            agregarFilaVacia(table, 7, fontTd);
        } else {
            for (IncidenciaReporteItem inc : lista) {
                table.addCell(crearCelda(inc.getCodigoIncidencia(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(inc.getMercadoNombre(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(inc.getTipoIncidencia(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(inc.getPrioridad(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(inc.getOrigen() != null ? inc.getOrigen() : "-", fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(formatearOffsetDate(inc.getFechaReporte()), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(inc.getEstado(), fontTd, Element.ALIGN_CENTER));
            }
        }

        document.add(table);
    }

    private void renderizarRenovaciones(Document document, ReporteMercadoFilter filtro, Font fontTh, Font fontTd) throws DocumentException {
        List<RenovacionReporteItem> lista = reporteOperativoService.listarTodasRenovaciones(filtro);
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{14f, 14f, 10f, 22f, 20f, 10f, 10f});
        table.setHeaderRows(1);

        agregarCabeceras(table, new String[]{"CÓDIGO", "TRÁMITE", "PUESTO", "CONDUCTOR ACTUAL", "NUEVO TITULAR", "FECHA", "ESTADO"}, fontTh);

        if (lista.isEmpty()) {
            agregarFilaVacia(table, 7, fontTd);
        } else {
            for (RenovacionReporteItem ren : lista) {
                table.addCell(crearCelda(ren.getCodigoSolicitud(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(ren.getTipoSolicitud(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(ren.getPuestoCodigo(), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(ren.getComercianteActual(), fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(ren.getComerciantePropuesto() != null ? ren.getComerciantePropuesto() : "-", fontTd, Element.ALIGN_LEFT));
                table.addCell(crearCelda(formatearFecha(ren.getFechaPresentacion()), fontTd, Element.ALIGN_CENTER));
                table.addCell(crearCelda(ren.getEstado(), fontTd, Element.ALIGN_CENTER));
            }
        }

        document.add(table);
    }

    private void agregarCabeceras(PdfPTable table, String[] cabeceras, Font fontTh) {
        for (String c : cabeceras) {
            PdfPCell cell = new PdfPCell(new Phrase(c, fontTh));
            cell.setBackgroundColor(new Color(30, 41, 59));
            cell.setPadding(4f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }
    }

    private void agregarFilaKpi(PdfPTable table, String indicador, String valor, String alcance, Font fontTd, Font fontTdBold) {
        table.addCell(crearCelda(indicador, fontTd, Element.ALIGN_LEFT));
        table.addCell(crearCelda(valor, fontTdBold, Element.ALIGN_CENTER));
        table.addCell(crearCelda(alcance, fontTd, Element.ALIGN_LEFT));
    }

    private void agregarFilaVacia(PdfPTable table, int columnas, Font fontTd) {
        PdfPCell cell = new PdfPCell(new Phrase("No se encontraron registros para los criterios seleccionados", fontTd));
        cell.setColspan(columnas);
        cell.setPadding(8f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private PdfPCell crearCelda(String texto, Font font, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "-", font));
        c.setPadding(3.5f);
        c.setHorizontalAlignment(alineacion);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setBorderColor(new Color(226, 232, 240));
        return c;
    }

    private PdfPCell crearCeldaResaltada(String texto, Font font, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "-", font));
        c.setPadding(4f);
        c.setHorizontalAlignment(alineacion);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setBackgroundColor(new Color(241, 245, 249));
        c.setBorderColor(new Color(148, 163, 184));
        return c;
    }

    private String formatearFecha(LocalDate fecha) {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "-";
    }

    private String formatearOffsetDate(OffsetDateTime fecha) {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "-";
    }

    /**
     * Evento para numeración de páginas y pie de página institucional.
     */
    private static class HeaderFooterPageEvent extends PdfPageEventHelper {
        private final String titulo;

        HeaderFooterPageEvent(String titulo) {
            this.titulo = titulo;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(2);
            try {
                footer.setWidths(new int[]{70, 30});
                footer.setTotalWidth(document.right() - document.left());
                footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                Font fontFooter = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.GRAY);

                PdfPCell cellIzq = new PdfPCell(new Phrase("SIGM-QUINUA — " + titulo + " | Documento informativo institucional emitido con fines administrativos.", fontFooter));
                cellIzq.setBorder(Rectangle.NO_BORDER);
                cellIzq.setHorizontalAlignment(Element.ALIGN_LEFT);
                footer.addCell(cellIzq);

                PdfPCell cellDer = new PdfPCell(new Phrase("Página " + writer.getPageNumber(), fontFooter));
                cellDer.setBorder(Rectangle.NO_BORDER);
                cellDer.setHorizontalAlignment(Element.ALIGN_RIGHT);
                footer.addCell(cellDer);

                footer.writeSelectedRows(0, -1, document.left(), document.bottom() - 10, writer.getDirectContent());
            } catch (Exception ignored) {
            }
        }
    }
}
