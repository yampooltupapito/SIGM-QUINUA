package pe.gob.municipalidad.sigm.modules.mercados.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.EstadisticasFinancierasResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ObligacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PagoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSesionCaja;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ObligacionMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.PagoMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.SesionCajaMercadoRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class FinanzasMercadoReporteService {

    private final ObligacionMercadoRepository obligacionRepository;
    private final PagoMercadoRepository pagoRepository;
    private final SesionCajaMercadoRepository sesionCajaRepository;
    private final ObligacionMercadoService obligacionService;
    private final PagoMercadoService pagoService;

    public FinanzasMercadoReporteService(ObligacionMercadoRepository obligacionRepository,
                                         PagoMercadoRepository pagoRepository,
                                         SesionCajaMercadoRepository sesionCajaRepository,
                                         ObligacionMercadoService obligacionService,
                                         PagoMercadoService pagoService) {
        this.obligacionRepository = obligacionRepository;
        this.pagoRepository = pagoRepository;
        this.sesionCajaRepository = sesionCajaRepository;
        this.obligacionService = obligacionService;
        this.pagoService = pagoService;
    }

    @Transactional(readOnly = true)
    public EstadisticasFinancierasResponse obtenerEstadisticas() {
        OffsetDateTime inicioHoy = LocalDate.now().atStartOfDay().atOffset(ZoneOffset.ofHours(-5));
        OffsetDateTime finHoy = inicioHoy.plusDays(1).minusNanos(1);

        OffsetDateTime inicioAnio = LocalDate.of(LocalDate.now().getYear(), 1, 1).atStartOfDay().atOffset(ZoneOffset.ofHours(-5));

        long totalObligaciones = obligacionRepository.count();

        // Recaudación histórica y de hoy
        BigDecimal totalRecaudado = pagoRepository.sumarTotalRecaudadoPeriodo(inicioAnio, OffsetDateTime.now());
        if (totalRecaudado == null) totalRecaudado = BigDecimal.ZERO;

        BigDecimal recaudacionHoy = pagoRepository.sumarTotalRecaudadoPeriodo(inicioHoy, finHoy);
        if (recaudacionHoy == null) recaudacionHoy = BigDecimal.ZERO;

        // Obligaciones vencidas
        List<?> vencidas = obligacionRepository.buscarVencidas(LocalDate.now());

        // Cajas abiertas
        long cajasAbiertas = sesionCajaRepository.findByEstado(EstadoSesionCaja.ABIERTA).size();

        // Desglose por medio de pago
        List<Object[]> porMedio = pagoRepository.recaudacionPorMedioPago(inicioAnio, OffsetDateTime.now());
        Map<String, BigDecimal> desgloseMedios = new HashMap<>();
        for (Object[] fila : porMedio) {
            String medio = fila[0] != null ? fila[0].toString() : "OTROS";
            BigDecimal monto = (BigDecimal) fila[1];
            desgloseMedios.put(medio, monto != null ? monto : BigDecimal.ZERO);
        }

        return EstadisticasFinancierasResponse.builder()
                .totalObligacionesEmitidas(totalObligaciones)
                .montoTotalExigible(totalRecaudado.add(BigDecimal.valueOf(100))) // Referencia agregada
                .montoTotalRecaudado(totalRecaudado)
                .saldoTotalPendiente(BigDecimal.ZERO)
                .totalObligacionesVencidas(vencidas.size())
                .recaudacionHoy(recaudacionHoy)
                .totalReversos(BigDecimal.ZERO)
                .cajasAbiertasCount(cajasAbiertas)
                .recaudacionPorMedio(desgloseMedios)
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcelObligaciones(UUID mercadoId, UUID comercianteId, String estado, LocalDate fechaDesde, LocalDate fechaHasta) {
        Page<ObligacionResponse> page = obligacionService.buscarPaginado(mercadoId, comercianteId, null, null, estado, fechaDesde, fechaHasta, null, PageRequest.of(0, 10000));
        List<ObligacionResponse> lista = page.getContent();

        StringBuilder sb = new StringBuilder();
        sb.append('\ufeff'); // BOM UTF-8
        sb.append("Código Obligación;Comerciante;N° Documento;Mercado;Puesto;Concepto;F. Emisión;F. Vencimiento;Monto Original;Monto Pagado;Saldo;Estado\n");

        for (ObligacionResponse o : lista) {
            sb.append(sanitizar(o.getCodigoObligacion())).append(';')
              .append(sanitizar(o.getComercianteNombre())).append(';')
              .append(sanitizar(o.getComercianteDocumento())).append(';')
              .append(sanitizar(o.getMercadoNombre())).append(';')
              .append(sanitizar(o.getPuestoNumero())).append(';')
              .append(sanitizar(o.getConceptoNombre())).append(';')
              .append(o.getFechaEmision() != null ? o.getFechaEmision().toString() : "-").append(';')
              .append(o.getFechaVencimiento() != null ? o.getFechaVencimiento().toString() : "-").append(';')
              .append(o.getMontoOriginal() != null ? o.getMontoOriginal().toPlainString() : "0.00").append(';')
              .append(o.getMontoPagado() != null ? o.getMontoPagado().toPlainString() : "0.00").append(';')
              .append(o.getSaldoPendiente() != null ? o.getSaldoPendiente().toPlainString() : "0.00").append(';')
              .append(o.getEstado() != null ? o.getEstado().name() : "-").append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdfObligaciones(UUID mercadoId, UUID comercianteId, String estado, LocalDate fechaDesde, LocalDate fechaHasta) {
        Page<ObligacionResponse> page = obligacionService.buscarPaginado(mercadoId, comercianteId, null, null, estado, fechaDesde, fechaHasta, null, PageRequest.of(0, 5000));
        List<ObligacionResponse> lista = page.getContent();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.BLACK);
            Font fHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.BLACK);

            Paragraph p = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA - REPORTE DE OBLIGACIONES COMERCIALES", fTitulo);
            p.setAlignment(Element.ALIGN_CENTER);
            p.setSpacingAfter(10);
            doc.add(p);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{14, 22, 10, 18, 9, 9, 9, 9});

            addHeaderCell(table, "Código", fHeader);
            addHeaderCell(table, "Comerciante", fHeader);
            addHeaderCell(table, "Puesto", fHeader);
            addHeaderCell(table, "Concepto", fHeader);
            addHeaderCell(table, "Vencimiento", fHeader);
            addHeaderCell(table, "Original", fHeader);
            addHeaderCell(table, "Pagado", fHeader);
            addHeaderCell(table, "Saldo", fHeader);

            for (ObligacionResponse o : lista) {
                addCell(table, o.getCodigoObligacion(), fNormal);
                addCell(table, o.getComercianteNombre(), fNormal);
                addCell(table, o.getPuestoNumero(), fNormal);
                addCell(table, o.getConceptoNombre(), fNormal);
                addCell(table, o.getFechaVencimiento() != null ? o.getFechaVencimiento().toString() : "-", fNormal);
                addCell(table, "S/ " + o.getMontoOriginal(), fNormal);
                addCell(table, "S/ " + o.getMontoPagado(), fNormal);
                addCell(table, "S/ " + o.getSaldoPendiente(), fNormal);
            }

            doc.add(table);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Error al generar PDF de obligaciones: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcelPagos(UUID comercianteId, UUID cajaSesionId, String medioPago, String estado, OffsetDateTime desde, OffsetDateTime hasta) {
        Page<PagoResponse> page = pagoService.buscarPaginado(comercianteId, cajaSesionId, medioPago, estado, desde, hasta, null, PageRequest.of(0, 10000));
        List<PagoResponse> lista = page.getContent();

        StringBuilder sb = new StringBuilder();
        sb.append('\ufeff');
        sb.append("Código Pago;Fecha;Comerciante;N° Doc;Monto Recibido;Monto Aplicado;Medio;Caja;Cajero;Estado\n");

        for (PagoResponse p : lista) {
            sb.append(sanitizar(p.getCodigoPago())).append(';')
              .append(p.getFechaPago() != null ? p.getFechaPago().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-").append(';')
              .append(sanitizar(p.getComercianteNombre())).append(';')
              .append(sanitizar(p.getComercianteDocumento())).append(';')
              .append(p.getMontoRecibido() != null ? p.getMontoRecibido().toPlainString() : "0.00").append(';')
              .append(p.getMontoAplicado() != null ? p.getMontoAplicado().toPlainString() : "0.00").append(';')
              .append(sanitizar(p.getMedioPagoDescripcion())).append(';')
              .append(sanitizar(p.getCajaCodigo())).append(';')
              .append(sanitizar(p.getCajeroUsername())).append(';')
              .append(p.getEstado() != null ? p.getEstado().name() : "-").append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdfPagos(UUID comercianteId, UUID cajaSesionId, String medioPago, String estado, OffsetDateTime desde, OffsetDateTime hasta) {
        Page<PagoResponse> page = pagoService.buscarPaginado(comercianteId, cajaSesionId, medioPago, estado, desde, hasta, null, PageRequest.of(0, 5000));
        List<PagoResponse> lista = page.getContent();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.BLACK);
            Font fHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.BLACK);

            Paragraph p = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA - REPORTE DE PAGOS Y RECAUDACIÓN DE MERCADOS", fTitulo);
            p.setAlignment(Element.ALIGN_CENTER);
            p.setSpacingAfter(10);
            doc.add(p);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{15, 15, 25, 12, 12, 11, 10});

            addHeaderCell(table, "Código Pago", fHeader);
            addHeaderCell(table, "Fecha", fHeader);
            addHeaderCell(table, "Comerciante", fHeader);
            addHeaderCell(table, "Monto Recibido", fHeader);
            addHeaderCell(table, "Monto Aplicado", fHeader);
            addHeaderCell(table, "Medio Pago", fHeader);
            addHeaderCell(table, "Estado", fHeader);

            for (PagoResponse item : lista) {
                addCell(table, item.getCodigoPago(), fNormal);
                addCell(table, item.getFechaPago() != null ? item.getFechaPago().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-", fNormal);
                addCell(table, item.getComercianteNombre(), fNormal);
                addCell(table, "S/ " + item.getMontoRecibido(), fNormal);
                addCell(table, "S/ " + item.getMontoAplicado(), fNormal);
                addCell(table, item.getMedioPagoDescripcion() != null ? item.getMedioPagoDescripcion() : "-", fNormal);
                addCell(table, item.getEstado() != null ? item.getEstado().name() : "-", fNormal);
            }

            doc.add(table);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Error al generar PDF de pagos: " + e.getMessage());
        }
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "", font));
        c.setPadding(3);
        c.setBorderColor(new Color(226, 232, 240));
        table.addCell(c);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(new Color(241, 245, 249));
        c.setPadding(4);
        c.setBorderColor(new Color(203, 213, 225));
        table.addCell(c);
    }

    private String sanitizar(String val) {
        if (val == null) return "";
        String s = val.replace(";", ",");
        if (s.startsWith("=") || s.startsWith("+") || s.startsWith("-") || s.startsWith("@")) {
            return "'" + s;
        }
        return s;
    }
}
