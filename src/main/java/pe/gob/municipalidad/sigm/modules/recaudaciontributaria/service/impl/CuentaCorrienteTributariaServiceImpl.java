package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.CuentaCorrienteDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.CuentaCorrienteSummaryDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.MovimientoResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.ObligacionSummaryDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuentaCorriente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoMovimientoCuenta;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.NaturalezaMovimiento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoMovimientoCuentaCorriente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ContribuyenteTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.CuentaCorrienteTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.MovimientoCuentaCorrienteTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ObligacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CuentaCorrienteTributariaService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification.CuentaCorrienteSpecification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CuentaCorrienteTributariaServiceImpl implements CuentaCorrienteTributariaService {

    private final CuentaCorrienteTributariaRepository cuentaRepository;
    private final MovimientoCuentaCorrienteTributariaRepository movimientoRepository;
    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final ObligacionTributariaRepository obligacionRepository;

    @Override
    @Transactional
    public CuentaCorrienteTributaria asegurarCuentaContribuyente(UUID contribuyenteId, String usuario) {
        return cuentaRepository.findByContribuyenteId(contribuyenteId)
                .orElseGet(() -> {
                    ContribuyenteTributario contribuyente = contribuyenteRepository.findById(contribuyenteId)
                            .orElseThrow(() -> new ResourceNotFoundException("Contribuyente no encontrado: " + contribuyenteId));

                    String codigoCuenta = "CTA-CT-" + contribuyente.getCodigoContribuyente() + "-PEN";
                    CuentaCorrienteTributaria nuevaCuenta = CuentaCorrienteTributaria.builder()
                            .contribuyente(contribuyente)
                            .codigoCuenta(codigoCuenta)
                            .moneda("PEN")
                            .saldoActual(BigDecimal.ZERO)
                            .totalCargos(BigDecimal.ZERO)
                            .totalAbonos(BigDecimal.ZERO)
                            .estado(EstadoCuentaCorriente.ACTIVA)
                            .fechaApertura(LocalDate.now())
                            .observaciones("Cuenta Corriente Tributaria generada automáticamente por el sistema.")
                            .build();

                    nuevaCuenta.setCreatedBy(usuario != null ? usuario : "SISTEMA");
                    log.info("Aperturando nueva Cuenta Corriente Tributaria: {} para contribuyente: {}", codigoCuenta, contribuyente.getCodigoContribuyente());
                    return cuentaRepository.save(nuevaCuenta);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaCorrienteDetailResponse obtenerPorId(UUID cuentaId) {
        CuentaCorrienteTributaria cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta Corriente no encontrada: " + cuentaId));
        return mapearADetalle(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaCorrienteDetailResponse obtenerPorContribuyenteId(UUID contribuyenteId) {
        CuentaCorrienteTributaria cuenta = cuentaRepository.findByContribuyenteId(contribuyenteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró cuenta corriente para el contribuyente: " + contribuyenteId));
        return mapearADetalle(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaCorrienteSummaryDto> listarCuentas(String query, EstadoCuentaCorriente estado, Boolean conDeuda, Pageable pageable) {
        Page<CuentaCorrienteTributaria> page = cuentaRepository.findAll(
                CuentaCorrienteSpecification.conFiltros(query, estado, conDeuda), pageable
        );

        List<CuentaCorrienteSummaryDto> dtoList = page.getContent().stream().map(c -> {
            long deudasCount = obligacionRepository.findDeudasPendientesByContribuyente(c.getContribuyente().getId()).size();
            return CuentaCorrienteSummaryDto.builder()
                    .id(c.getId())
                    .contribuyenteId(c.getContribuyente().getId())
                    .contribuyenteCodigo(c.getContribuyente().getCodigoContribuyente())
                    .contribuyenteNombre(c.getContribuyente().getNombreRazonSocial())
                    .contribuyenteDocumento(c.getContribuyente().getTipoDocumento() + ": " + c.getContribuyente().getNumeroDocumento())
                    .codigoCuenta(c.getCodigoCuenta())
                    .moneda(c.getMoneda())
                    .saldoActual(c.getSaldoActual())
                    .totalCargos(c.getTotalCargos())
                    .totalAbonos(c.getTotalAbonos())
                    .estado(c.getEstado())
                    .fechaApertura(c.getFechaApertura())
                    .totalObligacionesPendientes(deudasCount)
                    .build();
        }).toList();

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    @Override
    @Transactional
    public synchronized MovimientoCuentaCorrienteTributaria registrarMovimiento(
            UUID cuentaCorrienteId,
            ObligacionTributaria obligacion,
            CuotaObligacionTributaria cuota,
            TipoMovimientoCuentaCorriente tipoMovimiento,
            BigDecimal importe,
            String referenciaOrigen,
            String documentoSustento,
            String descripcion,
            String usuarioResponsable
    ) {
        if (importe == null || importe.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El importe del movimiento debe ser estrictamente mayor a cero.");
        }

        CuentaCorrienteTributaria cuenta = cuentaRepository.findById(cuentaCorrienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta Corriente no encontrada: " + cuentaCorrienteId));

        if (cuenta.getEstado() == EstadoCuentaCorriente.BLOQUEADA || cuenta.getEstado() == EstadoCuentaCorriente.CERRADA) {
            throw new BusinessException("La cuenta corriente " + cuenta.getCodigoCuenta() + " no está activa para registrar movimientos. Estado: " + cuenta.getEstado());
        }

        BigDecimal importeNormalizado = importe.setScale(2, RoundingMode.HALF_UP);
        BigDecimal saldoAnterior = cuenta.getSaldoActual();
        NaturalezaMovimiento naturaleza = tipoMovimiento.getNaturalezaPorDefecto();
        BigDecimal saldoPosterior;

        if (naturaleza == NaturalezaMovimiento.DEBITO) {
            saldoPosterior = saldoAnterior.add(importeNormalizado);
            cuenta.setTotalCargos(cuenta.getTotalCargos().add(importeNormalizado));
        } else {
            saldoPosterior = saldoAnterior.subtract(importeNormalizado);
            cuenta.setTotalAbonos(cuenta.getTotalAbonos().add(importeNormalizado));
        }

        cuenta.setSaldoActual(saldoPosterior);

        Long ultimoSecuencia = movimientoRepository.findUltimoNumeroSecuenciaByCuentaId(cuenta.getId());
        long siguienteSecuencia = (ultimoSecuencia != null ? ultimoSecuencia : 0L) + 1L;

        MovimientoCuentaCorrienteTributaria movimiento = MovimientoCuentaCorrienteTributaria.builder()
                .cuentaCorriente(cuenta)
                .obligacion(obligacion)
                .cuota(cuota)
                .numeroSecuencia(siguienteSecuencia)
                .tipoMovimiento(tipoMovimiento)
                .naturaleza(naturaleza)
                .importe(importeNormalizado)
                .saldoAnterior(saldoAnterior)
                .saldoPosterior(saldoPosterior)
                .fechaOperacion(LocalDate.now())
                .fechaRegistro(OffsetDateTime.now())
                .referenciaOrigen(referenciaOrigen)
                .documentoSustento(documentoSustento)
                .descripcion(descripcion)
                .estado(EstadoMovimientoCuenta.REGISTRADO)
                .usuarioResponsable(usuarioResponsable != null ? usuarioResponsable : "SISTEMA")
                .build();

        movimiento.setCreatedBy(usuarioResponsable != null ? usuarioResponsable : "SISTEMA");
        cuentaRepository.save(cuenta);
        MovimientoCuentaCorrienteTributaria guardado = movimientoRepository.save(movimiento);

        log.info("Movimiento registrado en cuenta {}: Sec={}, Tipo={}, Nat={}, Importe={}, SaldoAnterior={}, SaldoPosterior={}",
                cuenta.getCodigoCuenta(), siguienteSecuencia, tipoMovimiento, naturaleza, importeNormalizado, saldoAnterior, saldoPosterior);

        return guardado;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoResponse> listarMovimientos(UUID cuentaId, Pageable pageable) {
        return movimientoRepository.findByCuentaCorrienteIdOrderByNumeroSecuenciaDesc(cuentaId, pageable)
                .map(this::mapearAMovimientoResponse);
    }

    private CuentaCorrienteDetailResponse mapearADetalle(CuentaCorrienteTributaria c) {
        List<MovimientoResponse> movimientos = movimientoRepository.findByCuentaCorrienteIdOrderByNumeroSecuenciaAsc(c.getId())
                .stream().map(this::mapearAMovimientoResponse).collect(Collectors.toList());

        List<ObligacionSummaryDto> deudas = obligacionRepository.findDeudasPendientesByContribuyente(c.getContribuyente().getId())
                .stream().map(o -> ObligacionSummaryDto.builder()
                        .id(o.getId())
                        .codigoObligacion(o.getCodigoObligacion())
                        .contribuyenteId(c.getContribuyente().getId())
                        .contribuyenteCodigo(c.getContribuyente().getCodigoContribuyente())
                        .contribuyenteNombre(c.getContribuyente().getNombreRazonSocial())
                        .contribuyenteDocumento(c.getContribuyente().getTipoDocumento() + ": " + c.getContribuyente().getNumeroDocumento())
                        .conceptoCodigo(o.getConcepto().getCodigo())
                        .conceptoNombre(o.getConcepto().getNombre())
                        .predioCodigo(o.getPredio() != null ? o.getPredio().getCodigoPredio() : "N/A")
                        .predioDireccion(o.getPredio() != null ? o.getPredio().getDireccionCompleta() : "N/A")
                        .periodoAnio(o.getPeriodo().getAnio())
                        .tipoDeterminacion(o.getTipoDeterminacion())
                        .cuotaNumero(o.getCuotaNumero())
                        .totalCuotas(o.getTotalCuotas())
                        .importeDeterminado(o.getImporteDeterminado())
                        .importePagado(o.getImportePagado())
                        .saldo(o.getSaldo())
                        .moneda(o.getMoneda())
                        .fechaVencimiento(o.getFechaVencimiento())
                        .fechaRegistro(o.getFechaRegistro())
                        .estado(o.getEstado())
                        .estadoFinanciero(o.getEstadoFinanciero())
                        .estadoExigibilidad(o.getEstadoExigibilidad())
                        .build()
                ).toList();

        return CuentaCorrienteDetailResponse.builder()
                .id(c.getId())
                .codigoCuenta(c.getCodigoCuenta())
                .contribuyenteId(c.getContribuyente().getId())
                .contribuyenteCodigo(c.getContribuyente().getCodigoContribuyente())
                .contribuyenteNombre(c.getContribuyente().getNombreRazonSocial())
                .contribuyenteDocumento(c.getContribuyente().getTipoDocumento() + ": " + c.getContribuyente().getNumeroDocumento())
                .domicilioFiscal(c.getContribuyente().getDomicilioFiscal() != null ? c.getContribuyente().getDomicilioFiscal() : "Distrito de Quinua, Huamanga, Ayacucho")
                .email(c.getContribuyente().getCorreo())
                .telefono(c.getContribuyente().getTelefono())
                .moneda(c.getMoneda())
                .saldoActual(c.getSaldoActual())
                .totalCargos(c.getTotalCargos())
                .totalAbonos(c.getTotalAbonos())
                .estado(c.getEstado())
                .fechaApertura(c.getFechaApertura())
                .observaciones(c.getObservaciones())
                .movimientos(movimientos)
                .obligacionesPendientes(deudas)
                .build();
    }

    private MovimientoResponse mapearAMovimientoResponse(MovimientoCuentaCorrienteTributaria m) {
        return MovimientoResponse.builder()
                .id(m.getId())
                .numeroSecuencia(m.getNumeroSecuencia())
                .tipoMovimiento(m.getTipoMovimiento())
                .naturaleza(m.getNaturaleza())
                .importe(m.getImporte())
                .saldoAnterior(m.getSaldoAnterior())
                .saldoPosterior(m.getSaldoPosterior())
                .fechaOperacion(m.getFechaOperacion())
                .fechaRegistro(m.getFechaRegistro())
                .referenciaOrigen(m.getReferenciaOrigen())
                .documentoSustento(m.getDocumentoSustento())
                .descripcion(m.getDescripcion())
                .estado(m.getEstado())
                .usuarioResponsable(m.getUsuarioResponsable())
                .obligacionId(m.getObligacion() != null ? m.getObligacion().getId() : null)
                .obligacionCodigo(m.getObligacion() != null ? m.getObligacion().getCodigoObligacion() : null)
                .build();
    }
}
