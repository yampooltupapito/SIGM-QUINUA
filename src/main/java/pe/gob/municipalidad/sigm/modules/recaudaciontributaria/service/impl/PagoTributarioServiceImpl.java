package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CuentaCorrienteTributariaService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.PagoTributarioService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.PagoSpecification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validator.PagoValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoTributarioServiceImpl implements PagoTributarioService {

    private final PagoTributarioRepository pagoRepository;
    private final AplicacionPagoTributarioRepository aplicacionRepository;
    private final ObligacionTributariaRepository obligacionRepository;
    private final CuotaObligacionTributariaRepository cuotaRepository;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final SesionCajaMunicipalRepository sesionCajaRepository;
    private final CuentaCorrienteTributariaService cuentaCorrienteService;
    private final UsuarioRepository usuarioRepository;
    private final PagoValidator pagoValidator;

    @Override
    @Transactional(readOnly = true)
    public List<ObligacionPagoItemResponse> buscarObligacionesParaCobro(UUID contribuyenteId) {
        List<ObligacionTributaria> obligaciones = obligacionRepository.findByContribuyenteId(contribuyenteId).stream()
                .filter(o -> o.getEstado() != EstadoObligacionTributaria.ANULADA &&
                             o.getEstado() != EstadoObligacionTributaria.RECTIFICADA &&
                             o.getEstadoFinanciero() != EstadoFinancieroObligacion.CANCELADA &&
                             o.getSaldo().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing((ObligacionTributaria o) -> o.getPeriodo().getAnio())
                        .thenComparing(o -> o.getConcepto().getNombre())
                        .thenComparing(ObligacionTributaria::getCuotaNumero))
                .collect(Collectors.toList());

        List<ObligacionPagoItemResponse> resultado = new ArrayList<>();
        for (ObligacionTributaria o : obligaciones) {
            List<ObligacionPagoItemResponse.CuotaItemResponse> cuotasItems = o.getCuotas().stream()
                    .filter(c -> c.getSaldo().compareTo(BigDecimal.ZERO) > 0)
                    .map(c -> ObligacionPagoItemResponse.CuotaItemResponse.builder()
                            .cuotaId(c.getId())
                            .numeroCuota(c.getNumeroCuota())
                            .importe(c.getImporte())
                            .importePagado(c.getImportePagado())
                            .saldo(c.getSaldo())
                            .fechaVencimiento(c.getFechaVencimiento())
                            .estado(c.getEstado().name())
                            .build())
                    .collect(Collectors.toList());

            resultado.add(ObligacionPagoItemResponse.builder()
                    .obligacionId(o.getId())
                    .codigoObligacion(o.getCodigoObligacion())
                    .conceptoNombre(o.getConcepto().getNombre())
                    .anioPeriodo(o.getPeriodo().getAnio())
                    .predioCodigo(o.getPredio() != null ? o.getPredio().getCodigoPredio() : "N/A")
                    .predioDireccion(o.getPredio() != null ? o.getPredio().getDireccion() : "No aplica")
                    .cuotaNumero(o.getCuotaNumero())
                    .totalCuotas(o.getTotalCuotas())
                    .importeVigente(o.getImporteVigente())
                    .saldoPendiente(o.getSaldo())
                    .fechaVencimiento(o.getFechaVencimiento())
                    .estadoFinanciero(o.getEstadoFinanciero().getDescripcion())
                    .estadoExigibilidad(o.getEstadoExigibilidad().getDescripcion())
                    .cuotas(cuotasItems)
                    .build());
        }

        return resultado;
    }

    @Override
    @Transactional
    public PagoResponse registrarPago(RegistrarPagoRequest request, String usuario) {
        ContribuyenteTributario contribuyente = contribuyenteRepository.findById(request.getContribuyenteId())
                .orElseThrow(() -> new ResourceNotFoundException("Contribuyente no encontrado: " + request.getContribuyenteId()));

        pagoValidator.validarRegistroPago(request, contribuyente);

        Usuario usuarioReg = usuarioRepository.findByUsername(usuario).orElse(null);

        SesionCajaMunicipal sesion = null;
        if (request.getSesionCajaId() != null) {
            sesion = sesionCajaRepository.findById(request.getSesionCajaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sesión de caja no encontrada"));
        }

        Long seq = pagoRepository.obtenerSiguienteValorSecuencia();
        int anio = LocalDate.now().getYear();
        String codigoPago = String.format("PAG-%d-%05d", anio, seq);

        BigDecimal importeTotal = request.getImporteTotal().setScale(2, RoundingMode.HALF_UP);

        PagoTributario pago = PagoTributario.builder()
                .codigoPago(codigoPago)
                .contribuyente(contribuyente)
                .sesionCaja(sesion)
                .fechaHoraPago(OffsetDateTime.now())
                .importe(importeTotal)
                .moneda("PEN")
                .medioPago(request.getMedioPago())
                .referenciaExterna(request.getReferenciaExterna())
                .numeroOperacionBancaria(request.getNumeroOperacionBancaria())
                .bancoOrigen(request.getBancoOrigen())
                .idempotencyKey(request.getIdempotencyKey() != null ? request.getIdempotencyKey().trim() : null)
                .estado(EstadoPagoTributario.BORRADOR)
                .usuarioRegistro(usuarioReg)
                .observaciones(request.getObservaciones())
                .build();

        pago.setCreatedBy(usuario);

        // Crear las aplicaciones
        for (ItemAplicacionPagoRequest itemReq : request.getAplicaciones()) {
            ObligacionTributaria obligacion = obligacionRepository.findById(itemReq.getObligacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Obligación no encontrada"));

            CuotaObligacionTributaria cuota = null;
            if (itemReq.getCuotaId() != null) {
                cuota = cuotaRepository.findById(itemReq.getCuotaId()).orElse(null);
            }

            AplicacionPagoTributario app = AplicacionPagoTributario.builder()
                    .pago(pago)
                    .obligacion(obligacion)
                    .cuota(cuota)
                    .importeAplicado(itemReq.getImporteAplicado().setScale(2, RoundingMode.HALF_UP))
                    .fechaHoraAplicacion(OffsetDateTime.now())
                    .usuarioAplicacion(usuarioReg)
                    .estado(EstadoAplicacionPago.ACTIVA)
                    .observaciones(itemReq.getObservaciones())
                    .build();
            app.setCreatedBy(usuario);

            pago.getAplicaciones().add(app);
        }

        PagoTributario guardado = pagoRepository.save(pago);
        log.info("Pago registrado en borrador: {} por S/ {}", codigoPago, importeTotal);

        // Si es pago en efectivo en ventanilla o en sesión activa, confirmamos de inmediato
        if (request.getMedioPago() == MedioPagoTributario.EFECTIVO_CAJA || sesion != null) {
            return ejecutarConfirmacionPago(guardado, usuario, usuarioReg);
        }

        return mapearPagoAResponse(guardado);
    }

    @Override
    @Transactional
    public PagoResponse confirmarPago(UUID pagoId, ConfirmarPagoRequest request, String usuario) {
        PagoTributario pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + pagoId));

        pagoValidator.validarConfirmacionPago(pago);
        Usuario usuarioConf = usuarioRepository.findByUsername(usuario).orElse(null);

        if (request != null && request.getObservaciones() != null) {
            pago.setObservaciones(pago.getObservaciones() != null ?
                    pago.getObservaciones() + " | Confirmación: " + request.getObservaciones() : request.getObservaciones());
        }

        return ejecutarConfirmacionPago(pago, usuario, usuarioConf);
    }

    private PagoResponse ejecutarConfirmacionPago(PagoTributario pago, String usuario, Usuario usuarioConf) {
        pago.setEstado(EstadoPagoTributario.CONFIRMADO);
        pago.setFechaConfirmacion(OffsetDateTime.now());
        pago.setUsuarioConfirmacion(usuarioConf);
        pago.setUpdatedBy(usuario);

        // Asegurar cuenta corriente del contribuyente
        CuentaCorrienteTributaria cuenta = cuentaCorrienteService.asegurarCuentaContribuyente(
                pago.getContribuyente().getId(), usuario
        );

        // Aplicar cada partida contablemente y actualizar saldo de obligaciones
        for (AplicacionPagoTributario app : pago.getAplicaciones()) {
            ObligacionTributaria obligacion = app.getObligacion();
            CuotaObligacionTributaria cuota = app.getCuota();
            BigDecimal montoAplicado = app.getImporteAplicado();

            // 1. Registrar movimiento en la cuenta corriente tributaria (ABONO / CREDITO)
            String docSustento = String.format("Pago %s (%s)", pago.getCodigoPago(), pago.getMedioPago().getDescripcion());
            String descMov = String.format("Abono por recaudación tributaria aplicada a %s", obligacion.getCodigoObligacion());

            MovimientoCuentaCorrienteTributaria mov = cuentaCorrienteService.registrarMovimiento(
                    cuenta.getId(),
                    obligacion,
                    cuota,
                    TipoMovimientoCuentaCorriente.APLICACION_PAGO,
                    montoAplicado,
                    pago.getCodigoPago(),
                    docSustento,
                    descMov,
                    usuario
            );
            app.setMovimientoCuenta(mov);

            // 2. Actualizar saldo de la obligación
            obligacion.setImportePagado(obligacion.getImportePagado().add(montoAplicado));
            BigDecimal nuevoSaldoObl = obligacion.getSaldo().subtract(montoAplicado);
            obligacion.setSaldo(nuevoSaldoObl);

            if (nuevoSaldoObl.compareTo(BigDecimal.ZERO) == 0) {
                obligacion.setEstadoFinanciero(EstadoFinancieroObligacion.CANCELADA);
            } else {
                obligacion.setEstadoFinanciero(EstadoFinancieroObligacion.PARCIALMENTE_CUBIERTA);
            }
            obligacion.setUpdatedBy(usuario);
            obligacionRepository.save(obligacion);

            // 3. Si hay cuota, actualizarla
            if (cuota != null) {
                cuota.setImportePagado(cuota.getImportePagado().add(montoAplicado));
                BigDecimal nuevoSaldoCuota = cuota.getSaldo().subtract(montoAplicado);
                cuota.setSaldo(nuevoSaldoCuota);

                if (nuevoSaldoCuota.compareTo(BigDecimal.ZERO) == 0) {
                    cuota.setEstado(EstadoCuotaTributaria.CANCELADA);
                } else {
                    cuota.setEstado(EstadoCuotaTributaria.PARCIALMENTE_PAGADA);
                }
                cuota.setUpdatedBy(usuario);
                cuotaRepository.save(cuota);
            }
        }

        // 4. Si está vinculado a sesión de caja, acumular en el turno
        if (pago.getSesionCaja() != null) {
            SesionCajaMunicipal sesion = pago.getSesionCaja();
            if (pago.getMedioPago() == MedioPagoTributario.EFECTIVO_CAJA) {
                sesion.setTotalRecaudadoEfectivo(sesion.getTotalRecaudadoEfectivo().add(pago.getImporte()));
            } else {
                sesion.setTotalRecaudadoOtrosMedios(sesion.getTotalRecaudadoOtrosMedios().add(pago.getImporte()));
            }
            sesion.setTotalRecaudadoGeneral(sesion.getTotalRecaudadoGeneral().add(pago.getImporte()));
            sesion.setTotalOperaciones(sesion.getTotalOperaciones() + 1);
            sesion.setUpdatedBy(usuario);
            sesionCajaRepository.save(sesion);
        }

        PagoTributario guardado = pagoRepository.save(pago);
        log.info("Pago confirmado exitosamente: {} por S/ {}", guardado.getCodigoPago(), guardado.getImporte());
        return mapearPagoAResponse(guardado);
    }

    @Override
    @Transactional
    public PagoResponse revertirPago(UUID pagoId, RevertirPagoRequest request, String usuario) {
        PagoTributario pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + pagoId));

        pagoValidator.validarReversionPago(pago, request.getMotivoReversion());

        Usuario usuarioRev = usuarioRepository.findByUsername(usuario).orElse(null);
        pago.setEstado(EstadoPagoTributario.REVERTIDO);
        pago.setFechaReversion(OffsetDateTime.now());
        pago.setUsuarioReversion(usuarioRev);
        pago.setMotivoReversion(request.getMotivoReversion());
        pago.setUpdatedBy(usuario);

        CuentaCorrienteTributaria cuenta = cuentaCorrienteService.asegurarCuentaContribuyente(
                pago.getContribuyente().getId(), usuario
        );

        // Revertir cada aplicación contable y restaurar saldos
        for (AplicacionPagoTributario app : pago.getAplicaciones()) {
            if (app.getEstado() == EstadoAplicacionPago.ACTIVA) {
                app.setEstado(EstadoAplicacionPago.REVERTIDA);
                app.setUpdatedBy(usuario);

                ObligacionTributaria obligacion = app.getObligacion();
                CuotaObligacionTributaria cuota = app.getCuota();
                BigDecimal montoRevertido = app.getImporteAplicado();

                // Contraasiento en cuenta corriente (CARGO / DEBITO)
                String docSustento = String.format("Reversión de Pago %s", pago.getCodigoPago());
                String descMov = String.format("Contraasiento por reversión de pago aplicado a %s. Motivo: %s",
                        obligacion.getCodigoObligacion(), request.getMotivoReversion());

                cuentaCorrienteService.registrarMovimiento(
                        cuenta.getId(),
                        obligacion,
                        cuota,
                        TipoMovimientoCuentaCorriente.REVERSO_APLICACION_PAGO,
                        montoRevertido,
                        pago.getCodigoPago(),
                        docSustento,
                        descMov,
                        usuario
                );

                // Restaurar saldo en la obligación
                obligacion.setImportePagado(obligacion.getImportePagado().subtract(montoRevertido));
                BigDecimal saldoRestaurado = obligacion.getSaldo().add(montoRevertido);
                obligacion.setSaldo(saldoRestaurado);

                if (saldoRestaurado.compareTo(obligacion.getImporteVigente()) >= 0) {
                    obligacion.setEstadoFinanciero(EstadoFinancieroObligacion.PENDIENTE);
                } else {
                    obligacion.setEstadoFinanciero(EstadoFinancieroObligacion.PARCIALMENTE_CUBIERTA);
                }
                obligacion.setUpdatedBy(usuario);
                obligacionRepository.save(obligacion);

                // Restaurar cuota si aplica
                if (cuota != null) {
                    cuota.setImportePagado(cuota.getImportePagado().subtract(montoRevertido));
                    BigDecimal saldoCuotaRestaurado = cuota.getSaldo().add(montoRevertido);
                    cuota.setSaldo(saldoCuotaRestaurado);
                    cuota.setEstado(EstadoCuotaTributaria.PENDIENTE);
                    cuota.setUpdatedBy(usuario);
                    cuotaRepository.save(cuota);
                }
            }
        }

        // Si estaba en una sesión activa, descontar
        if (pago.getSesionCaja() != null) {
            SesionCajaMunicipal sesion = pago.getSesionCaja();
            if (pago.getMedioPago() == MedioPagoTributario.EFECTIVO_CAJA) {
                sesion.setTotalRecaudadoEfectivo(sesion.getTotalRecaudadoEfectivo().subtract(pago.getImporte()));
            } else {
                sesion.setTotalRecaudadoOtrosMedios(sesion.getTotalRecaudadoOtrosMedios().subtract(pago.getImporte()));
            }
            sesion.setTotalRecaudadoGeneral(sesion.getTotalRecaudadoGeneral().subtract(pago.getImporte()));
            sesion.setTotalOperaciones(Math.max(0, sesion.getTotalOperaciones() - 1));
            sesion.setUpdatedBy(usuario);
            sesionCajaRepository.save(sesion);
        }

        PagoTributario guardado = pagoRepository.save(pago);
        log.warn("Pago revertido formalmente: {}. Motivo: {}", guardado.getCodigoPago(), request.getMotivoReversion());
        return mapearPagoAResponse(guardado);
    }

    @Override
    @Transactional
    public PagoResponse anularPago(UUID pagoId, String motivo, String usuario) {
        PagoTributario pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + pagoId));

        if (pago.getEstado() == EstadoPagoTributario.CONFIRMADO || pago.getEstado() == EstadoPagoTributario.PROCESADO) {
            throw new BusinessException("Un pago confirmado no puede anularse administrativamente, debe revertirse financieramente.");
        }

        pago.setEstado(EstadoPagoTributario.ANULADO);
        pago.setMotivoReversion(motivo);
        pago.setUpdatedBy(usuario);
        log.info("Pago en borrador {} anulado por {}", pago.getCodigoPago(), usuario);
        return mapearPagoAResponse(pagoRepository.save(pago));
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtenerPorId(UUID id) {
        PagoTributario pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago tributario no encontrado: " + id));
        return mapearPagoAResponse(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtenerPorCodigo(String codigoPago) {
        PagoTributario pago = pagoRepository.findByCodigoPago(codigoPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago tributario no encontrado: " + codigoPago));
        return mapearPagoAResponse(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoResponse> listarPagos(String busqueda,
                                          UUID contribuyenteId,
                                          UUID sesionCajaId,
                                          MedioPagoTributario medioPago,
                                          EstadoPagoTributario estado,
                                          OffsetDateTime fechaDesde,
                                          OffsetDateTime fechaHasta,
                                          Pageable pageable) {
        return pagoRepository.findAll(PagoSpecification.conFiltros(busqueda, contribuyenteId, sesionCajaId, medioPago, estado, fechaDesde, fechaHasta), pageable)
                .map(this::mapearPagoAResponse);
    }

    private PagoResponse mapearPagoAResponse(PagoTributario pago) {
        List<AplicacionPagoResponse> aplicaciones = pago.getAplicaciones().stream().map(a ->
                AplicacionPagoResponse.builder()
                        .id(a.getId())
                        .obligacionId(a.getObligacion().getId())
                        .obligacionCodigo(a.getObligacion().getCodigoObligacion())
                        .conceptoNombre(a.getObligacion().getConcepto().getNombre())
                        .periodoAnio(a.getObligacion().getPeriodo().getAnio())
                        .cuotaNumero(a.getObligacion().getCuotaNumero())
                        .cuotaId(a.getCuota() != null ? a.getCuota().getId() : null)
                        .importeAplicado(a.getImporteAplicado())
                        .fechaHoraAplicacion(a.getFechaHoraAplicacion())
                        .estado(a.getEstado())
                        .estadoDescripcion(a.getEstado().getDescripcion())
                        .estadoBadgeClass(a.getEstado().getBadgeClass())
                        .movimientoCuentaId(a.getMovimientoCuenta() != null ? a.getMovimientoCuenta().getId() : null)
                        .observaciones(a.getObservaciones())
                        .build()
        ).collect(Collectors.toList());

        String cajaNom = pago.getSesionCaja() != null ? pago.getSesionCaja().getCaja().getNombre() : "N/A (Bancario/Externo)";
        String cajeroNom = pago.getSesionCaja() != null ?
                pago.getSesionCaja().getCajero().getNombreCompleto() : "Directo";

        return PagoResponse.builder()
                .id(pago.getId())
                .codigoPago(pago.getCodigoPago())
                .contribuyenteId(pago.getContribuyente().getId())
                .contribuyenteDocumento(pago.getContribuyente().getNumeroDocumento())
                .contribuyenteNombre(pago.getContribuyente().getNombreRazonSocial())
                .sesionCajaId(pago.getSesionCaja() != null ? pago.getSesionCaja().getId() : null)
                .sesionCajaCodigo(pago.getSesionCaja() != null ? pago.getSesionCaja().getCodigoSesion() : null)
                .cajaNombre(cajaNom)
                .cajeroNombre(cajeroNom)
                .importe(pago.getImporte())
                .moneda(pago.getMoneda())
                .medioPago(pago.getMedioPago())
                .medioPagoDescripcion(pago.getMedioPago().getDescripcion())
                .referenciaExterna(pago.getReferenciaExterna())
                .numeroOperacionBancaria(pago.getNumeroOperacionBancaria())
                .bancoOrigen(pago.getBancoOrigen())
                .idempotencyKey(pago.getIdempotencyKey())
                .estado(pago.getEstado())
                .estadoDescripcion(pago.getEstado().getDescripcion())
                .estadoBadgeClass(pago.getEstado().getBadgeClass())
                .fechaHoraPago(pago.getFechaHoraPago())
                .fechaConfirmacion(pago.getFechaConfirmacion())
                .usuarioConfirmacionNombre(pago.getUsuarioConfirmacion() != null ?
                        pago.getUsuarioConfirmacion().getNombreCompleto() : null)
                .fechaReversion(pago.getFechaReversion())
                .usuarioReversionNombre(pago.getUsuarioReversion() != null ?
                        pago.getUsuarioReversion().getNombreCompleto() : null)
                .motivoReversion(pago.getMotivoReversion())
                .observaciones(pago.getObservaciones())
                .aplicaciones(aplicaciones)
                .build();
    }
}
