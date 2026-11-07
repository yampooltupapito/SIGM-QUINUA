package pe.gob.municipalidad.sigm.modules.mercados.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteEstadoCuentaResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComercianteObligacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PortalComerciantePagoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ObligacionMercado;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PagoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoObligacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPagoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ObligacionMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.PagoMercadoRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PortalComercianteFinanzasService {

    private final ComerciantePortalContextService contextService;
    private final ObligacionMercadoRepository obligacionRepository;
    private final PagoMercadoRepository pagoRepository;
    private final PagoMercadoService pagoMercadoService;

    public PortalComercianteFinanzasService(
            ComerciantePortalContextService contextService,
            ObligacionMercadoRepository obligacionRepository,
            PagoMercadoRepository pagoRepository,
            PagoMercadoService pagoMercadoService) {
        this.contextService = contextService;
        this.obligacionRepository = obligacionRepository;
        this.pagoRepository = pagoRepository;
        this.pagoMercadoService = pagoMercadoService;
    }

    @Transactional(readOnly = true)
    public Page<PortalComercianteObligacionResponse> listarMisObligaciones(EstadoObligacion estado, Pageable pageable) {
        Comerciante comerciante = contextService.getComercianteActivo();
        Page<ObligacionMercado> page = obligacionRepository.findByComercianteId(comerciante.getId(), pageable);

        List<PortalComercianteObligacionResponse> items = page.getContent().stream()
                .filter(o -> estado == null || o.getEstado() == estado)
                .map(this::mapearObligacion)
                .collect(Collectors.toList());

        return new PageImpl<>(items, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PortalComercianteObligacionResponse obtenerDetalleObligacion(UUID obligacionId) {
        Comerciante comerciante = contextService.getComercianteActivo();
        ObligacionMercado o = contextService.validarPertenenciaObligacion(obligacionId, comerciante);
        return mapearObligacion(o);
    }

    @Transactional(readOnly = true)
    public Page<PortalComerciantePagoResponse> listarMisPagos(Pageable pageable) {
        Comerciante comerciante = contextService.getComercianteActivo();
        Page<PagoMercado> page = pagoRepository.findByComercianteId(comerciante.getId(), pageable);

        List<PortalComerciantePagoResponse> items = page.getContent().stream()
                .map(this::mapearPago)
                .collect(Collectors.toList());

        return new PageImpl<>(items, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PortalComerciantePagoResponse obtenerDetallePago(UUID pagoId) {
        Comerciante comerciante = contextService.getComercianteActivo();
        PagoMercado p = contextService.validarPertenenciaPago(pagoId, comerciante);
        return mapearPago(p);
    }

    @Transactional(readOnly = true)
    public byte[] descargarConstanciaPagoPdf(UUID pagoId) {
        Comerciante comerciante = contextService.getComercianteActivo();
        contextService.validarPertenenciaPago(pagoId, comerciante);
        return pagoMercadoService.generarConstanciaPdf(pagoId);
    }

    @Transactional(readOnly = true)
    public PortalComercianteEstadoCuentaResponse obtenerEstadoCuenta() {
        Comerciante c = contextService.getComercianteActivo();

        List<ObligacionMercado> allObs = obligacionRepository.findByComercianteIdOrderByFechaVencimientoAsc(c.getId());
        List<PagoMercado> allPagos = pagoRepository.findByComercianteId(c.getId());

        List<PortalComercianteObligacionResponse> pendientes = allObs.stream()
                .filter(o -> o.getEstado() == EstadoObligacion.PENDIENTE || o.getEstado() == EstadoObligacion.VENCIDA || o.getEstado() == EstadoObligacion.PARCIALMENTE_PAGADA)
                .map(this::mapearObligacion)
                .collect(Collectors.toList());

        BigDecimal totalDeuda = pendientes.stream()
                .map(PortalComercianteObligacionResponse::getSaldoPendiente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalHistoricoPagado = allPagos.stream()
                .filter(p -> p.getEstado() == EstadoPagoMercado.CONFIRMADO)
                .map(PagoMercado::getMontoRecibido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PortalComerciantePagoResponse> ultimosPagos = allPagos.stream()
                .limit(10)
                .map(this::mapearPago)
                .collect(Collectors.toList());

        return PortalComercianteEstadoCuentaResponse.builder()
                .codigoComerciante(c.getCodigoComerciante())
                .titularNombre(c.getNombreOTitular())
                .fechaCorte(LocalDate.now())
                .deudaTotalExigible(totalDeuda)
                .totalHistoricoPagado(totalHistoricoPagado)
                .cantidadObligacionesPendientes(pendientes.size())
                .obligacionesPendientes(pendientes)
                .ultimosPagos(ultimosPagos)
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] generarEstadoCuentaPdf() {
        PortalComercianteEstadoCuentaResponse estadoCuenta = obtenerEstadoCuenta();

        Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(30, 41, 59));
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.DARK_GRAY);
            Font fontTh = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font fontTd = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            Font fontAviso = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(185, 28, 28));

            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA — HUAMANGA, AYACUCHO", fontTitulo);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            doc.add(pHeader);

            Paragraph pSub = new Paragraph("ADMINISTRACIÓN DE MERCADOS MUNICIPALES\nESTADO DE CUENTA CORRIENTE INFORMATIVO", fontSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(8);
            doc.add(pSub);

            Paragraph pAviso = new Paragraph("NOTA IMPORTANTE: ESTE DOCUMENTO ES ESTRICTAMENTE INFORMATIVO Y NO CONSTITUYE CONSTANCIA DE NO ADEUDO NI CERTIFICACIÓN TRIBUTARIA.", fontAviso);
            pAviso.setAlignment(Element.ALIGN_CENTER);
            pAviso.setSpacingAfter(15);
            doc.add(pAviso);

            PdfPTable tInfo = new PdfPTable(2);
            tInfo.setWidthPercentage(100);
            tInfo.setWidths(new float[]{30, 70});
            tInfo.addCell(crearCeldaHeader("Titular:", fontSub));
            tInfo.addCell(crearCelda(estadoCuenta.getTitularNombre(), fontTd));
            tInfo.addCell(crearCeldaHeader("Código Comerciante:", fontSub));
            tInfo.addCell(crearCelda(estadoCuenta.getCodigoComerciante(), fontTd));
            tInfo.addCell(crearCeldaHeader("Fecha de Emisión:", fontSub));
            tInfo.addCell(crearCelda(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), fontTd));
            tInfo.addCell(crearCeldaHeader("Total Deuda Exigible:", fontSub));
            tInfo.addCell(crearCelda("S/ " + estadoCuenta.getDeudaTotalExigible().toString(), fontTd));
            tInfo.setSpacingAfter(15);
            doc.add(tInfo);

            Paragraph pSecObs = new Paragraph("OBLIGACIONES PENDIENTES DE PAGO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(29, 78, 216)));
            pSecObs.setSpacingAfter(5);
            doc.add(pSecObs);

            PdfPTable tObs = new PdfPTable(5);
            tObs.setWidthPercentage(100);
            tObs.setWidths(new float[]{20, 30, 15, 15, 20});
            String[] hObs = {"CÓDIGO", "CONCEPTO", "VENCIMIENTO", "MONTO S/", "SALDO S/"};
            for (String h : hObs) {
                PdfPCell c = new PdfPCell(new Phrase(h, fontTh));
                c.setBackgroundColor(new Color(30, 41, 59));
                c.setPadding(4f);
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                tObs.addCell(c);
            }

            for (PortalComercianteObligacionResponse o : estadoCuenta.getObligacionesPendientes()) {
                tObs.addCell(crearCelda(o.getCodigoObligacion(), fontTd));
                tObs.addCell(crearCelda(o.getConceptoNombre(), fontTd));
                tObs.addCell(crearCelda(o.getFechaVencimiento() != null ? o.getFechaVencimiento().toString() : "-", fontTd));
                tObs.addCell(crearCelda(o.getMontoOriginal().toString(), fontTd));
                tObs.addCell(crearCelda(o.getSaldoPendiente().toString(), fontTd));
            }
            if (estadoCuenta.getObligacionesPendientes().isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("No registra obligaciones pendientes a la fecha de consulta.", fontTd));
                empty.setColspan(5);
                empty.setPadding(8f);
                empty.setHorizontalAlignment(Element.ALIGN_CENTER);
                tObs.addCell(empty);
            }
            tObs.setSpacingAfter(15);
            doc.add(tObs);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF de estado de cuenta", e);
            return new byte[0];
        }
    }

    private PdfPCell crearCelda(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "-", font));
        c.setPadding(4f);
        c.setBorderColor(new Color(226, 232, 240));
        return c;
    }

    private PdfPCell crearCeldaHeader(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(new Color(248, 250, 252));
        c.setPadding(4f);
        c.setBorderColor(new Color(226, 232, 240));
        return c;
    }

    private PortalComercianteObligacionResponse mapearObligacion(ObligacionMercado o) {
        LocalDate hoy = LocalDate.now();
        boolean vencida = o.getFechaVencimiento() != null && hoy.isAfter(o.getFechaVencimiento()) && o.getSaldoPendiente() != null && o.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0;

        return PortalComercianteObligacionResponse.builder()
                .obligacionId(o.getId())
                .codigoObligacion(o.getCodigoObligacion())
                .conceptoNombre(o.getConcepto() != null ? o.getConcepto().getNombre() : "TARIFA DE MERCADO")
                .puestoCodigo(o.getPuesto() != null ? o.getPuesto().getCodigo() : "-")
                .periodoInicio(o.getPeriodoInicio())
                .periodoFin(o.getPeriodoFin())
                .fechaEmision(o.getFechaEmision())
                .fechaVencimiento(o.getFechaVencimiento())
                .montoOriginal(o.getMontoOriginal())
                .saldoPendiente(o.getSaldoPendiente())
                .estado(o.getEstado())
                .estadoLabel(o.getEstado() != null ? o.getEstado().name() : "-")
                .vencida(vencida)
                .build();
    }

    private PortalComerciantePagoResponse mapearPago(PagoMercado p) {
        return PortalComerciantePagoResponse.builder()
                .pagoId(p.getId())
                .codigoPago(p.getCodigoPago())
                .codigoSeguro(p.getCodigoSeguro())
                .fechaPago(p.getFechaPago())
                .montoRecibido(p.getMontoRecibido())
                .montoAplicado(p.getMontoAplicado())
                .moneda(p.getMoneda() != null ? p.getMoneda() : "PEN")
                .medioPago(p.getMedioPago())
                .medioPagoDescripcion(p.getMedioPago() != null ? p.getMedioPago().getDescripcion() : "-")
                .referenciaOperacion(p.getReferenciaOperacion())
                .estado(p.getEstado())
                .estadoDescripcion(p.getEstado() != null ? p.getEstado().name() : "-")
                .hashComprobante(p.getHashComprobante())
                .urlConstanciaPdf("/api/portal/comerciante/pagos/" + p.getId() + "/constancia")
                .build();
    }
}
