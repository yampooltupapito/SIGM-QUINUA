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
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoCuota;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoObligacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPagoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSesionCaja;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoMovimientoCuentaCorriente;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PagoMercadoService {

    private final PagoMercadoRepository pagoRepository;
    private final AplicacionPagoMercadoRepository aplicacionRepository;
    private final ReversoPagoMercadoRepository reversoRepository;
    private final ObligacionMercadoRepository obligacionRepository;
    private final CuotaObligacionMercadoRepository cuotaRepository;
    private final ComercianteRepository comercianteRepository;
    private final SesionCajaMercadoRepository sesionCajaRepository;
    private final MovimientoCuentaCorrienteMercadoRepository movimientoCcRepository;
    private final UsuarioRepository usuarioRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final AuditoriaAdministrativaService auditoriaService;

    public PagoMercadoService(PagoMercadoRepository pagoRepository,
                              AplicacionPagoMercadoRepository aplicacionRepository,
                              ReversoPagoMercadoRepository reversoRepository,
                              ObligacionMercadoRepository obligacionRepository,
                              CuotaObligacionMercadoRepository cuotaRepository,
                              ComercianteRepository comercianteRepository,
                              SesionCajaMercadoRepository sesionCajaRepository,
                              MovimientoCuentaCorrienteMercadoRepository movimientoCcRepository,
                              UsuarioRepository usuarioRepository,
                              QrCodeGenerator qrCodeGenerator,
                              AuditoriaAdministrativaService auditoriaService) {
        this.pagoRepository = pagoRepository;
        this.aplicacionRepository = aplicacionRepository;
        this.reversoRepository = reversoRepository;
        this.obligacionRepository = obligacionRepository;
        this.cuotaRepository = cuotaRepository;
        this.comercianteRepository = comercianteRepository;
        this.sesionCajaRepository = sesionCajaRepository;
        this.movimientoCcRepository = movimientoCcRepository;
        this.usuarioRepository = usuarioRepository;
        this.qrCodeGenerator = qrCodeGenerator;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public PagoResponse registrarPago(PagoCreateRequest request, String username, Long usuarioId) {
        if (request.getMontoRecibido() == null || request.getMontoRecibido().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El monto recibido debe ser mayor a 0.00.");
        }

        if (request.getAplicaciones() == null || request.getAplicaciones().isEmpty()) {
            throw new BusinessException("Debe especificar al menos una obligación a cancelar.");
        }

        Comerciante comerciante = comercianteRepository.findById(request.getComercianteId())
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado: " + request.getComercianteId()));

        SesionCajaMercado sesionCaja = null;
        if (request.getCajaSesionId() != null) {
            sesionCaja = sesionCajaRepository.findById(request.getCajaSesionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sesión de caja no encontrada: " + request.getCajaSesionId()));
            if (sesionCaja.getEstado() != EstadoSesionCaja.ABIERTA) {
                throw new BusinessException("No se pueden registrar pagos en una sesión de caja que no esté ABIERTA.");
            }
        }

        Usuario usuarioRegistro = null;
        if (usuarioId != null) {
            usuarioRegistro = usuarioRepository.findById(usuarioId).orElse(null);
        }

        // Validar total a aplicar vs monto recibido
        BigDecimal sumaAplicaciones = BigDecimal.ZERO;
        for (AplicacionItemRequest item : request.getAplicaciones()) {
            if (item.getMonto() == null || item.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("El monto a aplicar para cada obligación debe ser mayor a 0.00.");
            }
            sumaAplicaciones = sumaAplicaciones.add(item.getMonto());
        }

        if (sumaAplicaciones.compareTo(request.getMontoRecibido()) > 0) {
            throw new BusinessException("El total aplicado (S/ " + sumaAplicaciones + ") no puede ser superior al monto recibido (S/ " + request.getMontoRecibido() + ").");
        }

        // Generar correlativo oficial de pago
        Long seq = pagoRepository.obtenerSiguienteCorrelativo();
        int anio = Year.now().getValue();
        String codigoPago = String.format("REC-%d-%06d", anio, seq);
        String codigoSeguro = UUID.randomUUID().toString().replace("-", "");

        PagoMercado pago = PagoMercado.builder()
                .codigoPago(codigoPago)
                .comerciante(comerciante)
                .fechaPago(OffsetDateTime.now())
                .montoRecibido(request.getMontoRecibido())
                .montoAplicado(sumaAplicaciones)
                .moneda("PEN")
                .medioPago(request.getMedioPago())
                .referenciaOperacion(request.getReferenciaOperacion())
                .estado(EstadoPagoMercado.CONFIRMADO)
                .sesionCaja(sesionCaja)
                .usuarioRegistroEntity(usuarioRegistro)
                .usuarioRegistro(username != null ? username : "caja")
                .codigoSeguro(codigoSeguro)
                .observaciones(request.getObservaciones())
                .build();

        // Calcular hash SHA-256 preliminar
        String hashData = codigoPago + "|" + codigoSeguro + "|" + sumaAplicaciones.toPlainString();
        pago.setHashComprobante(calcularHashSha256(hashData));

        PagoMercado guardado = pagoRepository.save(pago);

        // Procesar aplicaciones y actualizar saldos de obligaciones / cuotas
        List<AplicacionPagoMercado> apps = new ArrayList<>();
        for (AplicacionItemRequest item : request.getAplicaciones()) {
            ObligacionMercado obligacion = obligacionRepository.findByIdConRelaciones(item.getObligacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Obligación no encontrada: " + item.getObligacionId()));

            if (!obligacion.getEstado().isExigible()) {
                throw new BusinessException("La obligación " + obligacion.getCodigoObligacion() + " no está en estado exigible (Estado: " + obligacion.getEstado() + ").");
            }

            if (item.getMonto().compareTo(obligacion.getSaldoPendiente()) > 0) {
                throw new BusinessException("El monto a aplicar S/ " + item.getMonto() + " supera el saldo pendiente S/ " + obligacion.getSaldoPendiente() + " de la obligación " + obligacion.getCodigoObligacion());
            }

            CuotaObligacionMercado cuota = null;
            if (item.getCuotaId() != null) {
                cuota = cuotaRepository.findById(item.getCuotaId())
                        .orElseThrow(() -> new ResourceNotFoundException("Cuota no encontrada: " + item.getCuotaId()));
                if (item.getMonto().compareTo(cuota.getSaldo()) > 0) {
                    throw new BusinessException("El monto a aplicar S/ " + item.getMonto() + " supera el saldo S/ " + cuota.getSaldo() + " de la cuota " + cuota.getNumeroCuota());
                }
                cuota.setMontoPagado(cuota.getMontoPagado().add(item.getMonto()));
                cuota.setSaldo(cuota.getSaldo().subtract(item.getMonto()));
                if (cuota.getSaldo().compareTo(BigDecimal.ZERO) == 0) {
                    cuota.setEstado(EstadoCuota.PAGADA);
                } else {
                    cuota.setEstado(EstadoCuota.PARCIALMENTE_PAGADA);
                }
                cuotaRepository.save(cuota);
            }

            // Actualizar obligación
            obligacion.setMontoPagado(obligacion.getMontoPagado().add(item.getMonto()));
            obligacion.setSaldoPendiente(obligacion.getSaldoPendiente().subtract(item.getMonto()));
            if (obligacion.getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0) {
                obligacion.setEstado(EstadoObligacion.PAGADA);
            } else {
                obligacion.setEstado(EstadoObligacion.PARCIALMENTE_PAGADA);
            }
            obligacionRepository.save(obligacion);

            AplicacionPagoMercado app = AplicacionPagoMercado.builder()
                    .pago(guardado)
                    .obligacion(obligacion)
                    .cuota(cuota)
                    .montoAplicado(item.getMonto())
                    .fechaAplicacion(OffsetDateTime.now())
                    .estado("APLICADO")
                    .build();
            apps.add(aplicacionRepository.save(app));
        }
        guardado.setAplicaciones(apps);

        // Actualizar sesión de caja si aplica
        if (sesionCaja != null) {
            sesionCaja.setTotalIngresos(sesionCaja.getTotalIngresos().add(guardado.getMontoRecibido()));
            sesionCaja.setMontoFinalEsperado(sesionCaja.getMontoFinalEsperado().add(guardado.getMontoRecibido()));
            sesionCajaRepository.save(sesionCaja);
        }

        // Registrar ABONO en cuenta corriente
        BigDecimal saldoTotalRestante = obligacionRepository.sumarDeudaTotalComerciante(comerciante.getId());
        movimientoCcRepository.save(MovimientoCuentaCorrienteMercado.builder()
                .comerciante(comerciante)
                .tipoMovimiento(TipoMovimientoCuentaCorriente.ABONO)
                .concepto("Pago " + guardado.getCodigoPago() + " (" + guardado.getMedioPago().getDescripcion() + ")")
                .monto(guardado.getMontoAplicado())
                .saldoResultante(saldoTotalRestante)
                .referenciaId(guardado.getId())
                .tipoReferencia("PAGO")
                .fechaMovimiento(OffsetDateTime.now())
                .usuario(username != null ? username : "caja")
                .build());

        auditoriaService.registrar("REGISTRAR_PAGO_MERCADO", "mercados_pagos", guardado.getId(),
                "EXITO", "Pago registrado: " + guardado.getCodigoPago() + " por S/ " + guardado.getMontoRecibido());

        return toResponse(guardado);
    }

    @Transactional
    public PagoResponse reversarPago(UUID pagoId, ReversoPagoRequest request, String username, Long usuarioId) {
        PagoMercado pago = pagoRepository.findByIdConRelaciones(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + pagoId));

        if (pago.getEstado() == EstadoPagoMercado.REVERSADO || pago.getEstado() == EstadoPagoMercado.ANULADO) {
            throw new BusinessException("El pago ya se encuentra " + pago.getEstado() + ".");
        }

        Usuario usuarioReverso = null;
        if (usuarioId != null) {
            usuarioReverso = usuarioRepository.findById(usuarioId).orElse(null);
        }

        // Reversar cada aplicación de fondos
        List<AplicacionPagoMercado> aplicaciones = aplicacionRepository.findByPagoId(pago.getId());
        for (AplicacionPagoMercado app : aplicaciones) {
            if ("APLICADO".equalsIgnoreCase(app.getEstado())) {
                ObligacionMercado obligacion = app.getObligacion();
                obligacion.setSaldoPendiente(obligacion.getSaldoPendiente().add(app.getMontoAplicado()));
                obligacion.setMontoPagado(obligacion.getMontoPagado().subtract(app.getMontoAplicado()));
                if (obligacion.getMontoPagado().compareTo(BigDecimal.ZERO) == 0) {
                    obligacion.setEstado(EstadoObligacion.PENDIENTE);
                } else {
                    obligacion.setEstado(EstadoObligacion.PARCIALMENTE_PAGADA);
                }
                obligacionRepository.save(obligacion);

                if (app.getCuota() != null) {
                    CuotaObligacionMercado cuota = app.getCuota();
                    cuota.setSaldo(cuota.getSaldo().add(app.getMontoAplicado()));
                    cuota.setMontoPagado(cuota.getMontoPagado().subtract(app.getMontoAplicado()));
                    if (cuota.getMontoPagado().compareTo(BigDecimal.ZERO) == 0) {
                        cuota.setEstado(EstadoCuota.PENDIENTE);
                    } else {
                        cuota.setEstado(EstadoCuota.PARCIALMENTE_PAGADA);
                    }
                    cuotaRepository.save(cuota);
                }

                app.setEstado("REVERSADO");
                aplicacionRepository.save(app);
            }
        }

        pago.setEstado(EstadoPagoMercado.REVERSADO);
        pago.setObservaciones((pago.getObservaciones() != null ? pago.getObservaciones() + " | " : "") + "REVERSADO: " + request.getMotivo());
        pagoRepository.save(pago);

        // Actualizar sesión de caja si pertenecía a una
        if (pago.getSesionCaja() != null) {
            SesionCajaMercado sesion = pago.getSesionCaja();
            sesion.setTotalReversos(sesion.getTotalReversos().add(pago.getMontoRecibido()));
            sesion.setMontoFinalEsperado(sesion.getMontoFinalEsperado().subtract(pago.getMontoRecibido()));
            sesionCajaRepository.save(sesion);
        }

        // Registrar Reverso en tabla de auditoría de reversos
        reversoRepository.save(ReversoPagoMercado.builder()
                .pago(pago)
                .motivo(request.getMotivo())
                .documentoSustento(request.getDocumentoSustento())
                .usuarioReversoEntity(usuarioReverso)
                .usuarioReverso(username != null ? username : "supervisor")
                .fechaReverso(OffsetDateTime.now())
                .montoReversado(pago.getMontoAplicado())
                .build());

        // Movimiento en cuenta corriente
        BigDecimal saldoTotalResultante = obligacionRepository.sumarDeudaTotalComerciante(pago.getComerciante().getId());
        movimientoCcRepository.save(MovimientoCuentaCorrienteMercado.builder()
                .comerciante(pago.getComerciante())
                .tipoMovimiento(TipoMovimientoCuentaCorriente.REVERSO)
                .concepto("Reverso de pago " + pago.getCodigoPago() + ": " + request.getMotivo())
                .monto(pago.getMontoAplicado())
                .saldoResultante(saldoTotalResultante)
                .referenciaId(pago.getId())
                .tipoReferencia("REVERSO_PAGO")
                .fechaMovimiento(OffsetDateTime.now())
                .usuario(username != null ? username : "supervisor")
                .build());

        auditoriaService.registrar("REVERSAR_PAGO_MERCADO", "mercados_pagos", pago.getId(),
                "EXITO", "Pago reversado: " + pago.getCodigoPago() + ". Motivo: " + request.getMotivo());

        return toResponse(pago);
    }

    @Transactional(readOnly = true)
    public PagoResponse obtenerPorId(UUID id) {
        return pagoRepository.findByIdConRelaciones(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Page<PagoResponse> buscarPaginado(UUID comercianteId, UUID cajaSesionId, String medioPago, String estado,
                                            OffsetDateTime fechaDesde, OffsetDateTime fechaHasta, String q, Pageable pageable) {
        Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return pagoRepository.buscarPaginado(comercianteId, cajaSesionId, medioPago, estado, fechaDesde, fechaHasta, q, unsortedPageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public byte[] generarConstanciaPdf(UUID pagoId) {
        PagoMercado pago = pagoRepository.findByIdConRelaciones(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + pagoId));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font fontCode = FontFactory.getFont(FontFactory.COURIER, 8, Color.DARK_GRAY);

            // Membrete
            Paragraph pHeader = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA\nHUAMANGA - AYACUCHO", fontTitulo);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            doc.add(pHeader);

            Paragraph pSub = new Paragraph("SISTEMA INTEGRAL DE GESTIÓN MUNICIPAL (SIGM-QUINUA)\nADMINISTRACIÓN DE MERCADOS MUNICIPALES", fontSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(15);
            doc.add(pSub);

            // Título
            Paragraph pDoc = new Paragraph("RECIBO DE CAJA / CONSTANCIA DE PAGO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(29, 78, 216)));
            pDoc.setAlignment(Element.ALIGN_CENTER);
            doc.add(pDoc);

            Paragraph pNum = new Paragraph(pago.getCodigoPago() + " (" + pago.getEstado().name() + ")", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK));
            pNum.setAlignment(Element.ALIGN_CENTER);
            pNum.setSpacingAfter(15);
            doc.add(pNum);

            // Tabla de datos del pago
            PdfPTable t = new PdfPTable(4);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{25, 25, 25, 25});

            addCell(t, "Fecha de Pago:", fontBold);
            addCell(t, pago.getFechaPago().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fontNormal);
            addCell(t, "Medio de Pago:", fontBold);
            addCell(t, pago.getMedioPago().getDescripcion(), fontNormal);

            addCell(t, "Comerciante Titular:", fontBold);
            addCell(t, pago.getComerciante().getPersona().getNombreCompleto(), fontNormal);
            addCell(t, "N° Documento:", fontBold);
            addCell(t, pago.getComerciante().getPersona().getNumeroDocumento(), fontNormal);

            addCell(t, "Operador de Caja:", fontBold);
            addCell(t, pago.getUsuarioRegistro(), fontNormal);
            addCell(t, "Caja Recaudadora:", fontBold);
            addCell(t, pago.getSesionCaja() != null ? pago.getSesionCaja().getCaja().getCodigo() : "OFICINA DE RECAUDACIÓN", fontNormal);

            addCell(t, "Monto Recibido:", fontBold);
            addCell(t, "S/ " + pago.getMontoRecibido().toPlainString(), fontBold);
            addCell(t, "Total Aplicado:", fontBold);
            addCell(t, "S/ " + pago.getMontoAplicado().toPlainString(), fontBold);

            t.setSpacingAfter(15);
            doc.add(t);

            // Tabla de Aplicación a Obligaciones
            Paragraph pDetalle = new Paragraph("DETALLE DE OBLIGACIONES CANCELADAS", fontBold);
            pDetalle.setSpacingAfter(5);
            doc.add(pDetalle);

            PdfPTable tApps = new PdfPTable(4);
            tApps.setWidthPercentage(100);
            tApps.setWidths(new float[]{30, 35, 15, 20});

            addHeaderCell(tApps, "Código Obligación", fontBold);
            addHeaderCell(tApps, "Concepto", fontBold);
            addHeaderCell(tApps, "Cuota", fontBold);
            addHeaderCell(tApps, "Monto Aplicado", fontBold);

            List<AplicacionPagoMercado> apps = aplicacionRepository.findByPagoId(pago.getId());
            for (AplicacionPagoMercado a : apps) {
                addCell(tApps, a.getObligacion().getCodigoObligacion(), fontNormal);
                addCell(tApps, a.getObligacion().getConcepto().getNombre(), fontNormal);
                addCell(tApps, a.getCuota() != null ? "N° " + a.getCuota().getNumeroCuota() : "ÚNICA", fontNormal);
                addCell(tApps, "S/ " + a.getMontoAplicado().toPlainString(), fontNormal);
            }
            tApps.setSpacingAfter(20);
            doc.add(tApps);

            // Pie con QR y Hash SHA-256
            PdfPTable tPie = new PdfPTable(2);
            tPie.setWidthPercentage(100);
            tPie.setWidths(new float[]{25, 75});

            if (pago.getCodigoSeguro() != null) {
                String qrUrl = "https://sigm.muniquinua.gob.pe/verificar/mercados/documento/" + pago.getCodigoSeguro();
                byte[] qrBytes = qrCodeGenerator.generateQrCodePng(qrUrl, 120, 120);
                Image qrImg = Image.getInstance(qrBytes);
                PdfPCell cQr = new PdfPCell(qrImg);
                cQr.setBorder(Rectangle.NO_BORDER);
                cQr.setHorizontalAlignment(Element.ALIGN_CENTER);
                tPie.addCell(cQr);
            } else {
                tPie.addCell(new PdfPCell(new Phrase("")));
            }

            Paragraph pLegal = new Paragraph();
            pLegal.add(new Phrase("VALIDACIÓN DE CONSTANCIA ELECTRÓNICA DE RECAUDACIÓN\n", fontBold));
            pLegal.add(new Phrase("Este documento certifica el ingreso dinerario registrado en las arcas municipales de Quinua.\n" +
                    "Para verificar la autenticidad del recibo escanee el código QR o consulte en el portal oficial.\n", fontSub));
            pLegal.add(new Phrase("Hash Criptográfico SHA-256: " + pago.getHashComprobante() + "\n", fontCode));
            pLegal.add(new Phrase("Código Seguro: " + pago.getCodigoSeguro(), fontCode));

            PdfPCell cLegal = new PdfPCell(pLegal);
            cLegal.setBorder(Rectangle.NO_BORDER);
            tPie.addCell(cLegal);

            doc.add(tPie);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar constancia PDF de pago", e);
            throw new BusinessException("No se pudo generar la constancia PDF del pago: " + e.getMessage());
        }
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setPadding(4);
        c.setBorderColor(new Color(203, 213, 225));
        table.addCell(c);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(new Color(241, 245, 249));
        c.setPadding(5);
        c.setBorderColor(new Color(203, 213, 225));
        table.addCell(c);
    }

    private String calcularHashSha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    public PagoResponse toResponse(PagoMercado p) {
        List<AplicacionResponse> apps = p.getAplicaciones() != null ? p.getAplicaciones().stream().map(a -> AplicacionResponse.builder()
                .id(a.getId())
                .pagoId(p.getId())
                .codigoPago(p.getCodigoPago())
                .obligacionId(a.getObligacion().getId())
                .codigoObligacion(a.getObligacion().getCodigoObligacion())
                .conceptoNombre(a.getObligacion().getConcepto().getNombre())
                .cuotaId(a.getCuota() != null ? a.getCuota().getId() : null)
                .numeroCuota(a.getCuota() != null ? a.getCuota().getNumeroCuota() : null)
                .montoAplicado(a.getMontoAplicado())
                .fechaAplicacion(a.getFechaAplicacion())
                .estado(a.getEstado())
                .build()).collect(Collectors.toList()) : new ArrayList<>();

        Comerciante com = p.getComerciante();
        String comNombre = "-";
        String comDoc = "-";
        if (com != null) {
            if (com.getPersona() != null) {
                comNombre = com.getPersona().getNombreCompleto();
                comDoc = com.getPersona().getNumeroDocumento();
            } else if (com.getPersonaJuridica() != null) {
                comNombre = com.getPersonaJuridica().getRazonSocial();
                comDoc = com.getPersonaJuridica().getRuc();
            }
        }

        return PagoResponse.builder()
                .id(p.getId())
                .codigoPago(p.getCodigoPago())
                .comercianteId(com != null ? com.getId() : null)
                .comercianteNombre(comNombre)
                .comercianteDocumento(comDoc)
                .fechaPago(p.getFechaPago())
                .montoRecibido(p.getMontoRecibido())
                .montoAplicado(p.getMontoAplicado())
                .moneda(p.getMoneda())
                .medioPago(p.getMedioPago())
                .medioPagoDescripcion(p.getMedioPago() != null ? p.getMedioPago().getDescripcion() : null)
                .referenciaOperacion(p.getReferenciaOperacion())
                .estado(p.getEstado())
                .estadoDescripcion(p.getEstado() != null ? p.getEstado().getDescripcion() : null)
                .cajaSesionId(p.getSesionCaja() != null ? p.getSesionCaja().getId() : null)
                .cajaCodigo(p.getSesionCaja() != null && p.getSesionCaja().getCaja() != null ? p.getSesionCaja().getCaja().getCodigo() : "GENERAL")
                .cajeroUsername(p.getUsuarioRegistro())
                .codigoSeguro(p.getCodigoSeguro())
                .hashComprobante(p.getHashComprobante())
                .observaciones(p.getObservaciones())
                .aplicaciones(apps)
                .build();
    }
}
