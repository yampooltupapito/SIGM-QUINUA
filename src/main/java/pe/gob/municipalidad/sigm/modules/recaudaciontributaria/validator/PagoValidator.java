package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago.ItemAplicacionPagoRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago.RegistrarPagoRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PagoValidator {

    private final PagoTributarioRepository pagoRepository;
    private final ObligacionTributariaRepository obligacionRepository;
    private final CuotaObligacionTributariaRepository cuotaRepository;
    private final SesionCajaMunicipalRepository sesionCajaRepository;
    private final CajaMunicipalRepository cajaRepository;

    public void validarRegistroPago(RegistrarPagoRequest request, ContribuyenteTributario contribuyente) {
        if (contribuyente == null) {
            throw new BusinessException("El contribuyente indicado no existe");
        }

        if (request.getImporteTotal() == null || request.getImporteTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El importe total a pagar debe ser mayor a 0.00 PEN");
        }

        if (request.getAplicaciones() == null || request.getAplicaciones().isEmpty()) {
            throw new BusinessException("Debe especificar al menos una aplicación de pago hacia una obligación tributaria");
        }

        // Sum of applications must equal importeTotal
        BigDecimal sumaAplicaciones = BigDecimal.ZERO;
        for (ItemAplicacionPagoRequest item : request.getAplicaciones()) {
            if (item.getImporteAplicado() == null || item.getImporteAplicado().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Cada aplicación de pago debe tener un importe mayor a cero");
            }
            sumaAplicaciones = sumaAplicaciones.add(item.getImporteAplicado());
        }

        if (sumaAplicaciones.compareTo(request.getImporteTotal()) != 0) {
            throw new BusinessException(String.format(
                    "Inconsistencia financiera: La suma de aplicaciones (S/ %s) no coincide con el importe total declarado (S/ %s)",
                    sumaAplicaciones, request.getImporteTotal()
            ));
        }

        // Validar Idempotencia
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().trim().isEmpty()) {
            Optional<PagoTributario> existente = pagoRepository.findByIdempotencyKey(request.getIdempotencyKey().trim());
            if (existente.isPresent()) {
                throw new BusinessException("Operación duplicada: Ya existe un pago registrado con la misma clave de idempotencia: " + request.getIdempotencyKey());
            }
        }

        // Validar medio de pago efectivo requiere sesión de caja abierta
        if (request.getMedioPago() == MedioPagoTributario.EFECTIVO_CAJA) {
            if (request.getSesionCajaId() == null) {
                throw new BusinessException("Los pagos en efectivo en ventanilla requieren obligatoriamente una sesión de caja municipal activa");
            }
            SesionCajaMunicipal sesion = sesionCajaRepository.findById(request.getSesionCajaId())
                    .orElseThrow(() -> new BusinessException("La sesión de caja especificada no existe"));
            if (sesion.getEstado() != EstadoSesionCaja.ABIERTA) {
                throw new BusinessException("La sesión de caja " + sesion.getCodigoSesion() + " no se encuentra en estado ABIERTA para recibir recaudación");
            }
        }

        // Validar referencias bancarias
        if (request.getMedioPago() == MedioPagoTributario.TRANSFERENCIA_BANCARIA || request.getMedioPago() == MedioPagoTributario.DEPOSITO_CUENTA) {
            String ref = request.getNumeroOperacionBancaria() != null ? request.getNumeroOperacionBancaria() : request.getReferenciaExterna();
            if (ref == null || ref.trim().isEmpty()) {
                throw new BusinessException("Las operaciones bancarias (transferencias/depósitos) requieren el número de operación o referencia bancaria");
            }
        }

        // Validar cada obligación y cuota
        for (ItemAplicacionPagoRequest item : request.getAplicaciones()) {
            ObligacionTributaria obl = obligacionRepository.findById(item.getObligacionId())
                    .orElseThrow(() -> new BusinessException("La obligación con ID " + item.getObligacionId() + " no existe"));

            if (obl.getEstado() == EstadoObligacionTributaria.ANULADA || obl.getEstado() == EstadoObligacionTributaria.RECTIFICADA) {
                throw new BusinessException("No se puede aplicar pagos a la obligación " + obl.getCodigoObligacion() + " porque está " + obl.getEstado());
            }

            if (obl.getEstadoFinanciero() == EstadoFinancieroObligacion.CANCELADA || obl.getSaldo().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("La obligación " + obl.getCodigoObligacion() + " ya se encuentra totalmente cancelada");
            }

            if (item.getImporteAplicado().compareTo(obl.getSaldo()) > 0) {
                throw new BusinessException(String.format(
                        "Sobrepago prohibido: El importe a aplicar (S/ %s) supera el saldo pendiente de la obligación %s (S/ %s)",
                        item.getImporteAplicado(), obl.getCodigoObligacion(), obl.getSaldo()
                ));
            }

            if (item.getCuotaId() != null) {
                CuotaObligacionTributaria cuota = cuotaRepository.findById(item.getCuotaId())
                        .orElseThrow(() -> new BusinessException("La cuota especificada no existe"));
                if (!cuota.getObligacion().getId().equals(obl.getId())) {
                    throw new BusinessException("La cuota " + cuota.getNumeroCuota() + " no pertenece a la obligación " + obl.getCodigoObligacion());
                }
                if (item.getImporteAplicado().compareTo(cuota.getSaldo()) > 0) {
                    throw new BusinessException(String.format(
                            "Sobrepago prohibido: El importe a aplicar (S/ %s) supera el saldo pendiente de la cuota %d (S/ %s)",
                            item.getImporteAplicado(), cuota.getNumeroCuota(), cuota.getSaldo()
                    ));
                }
            }
        }
    }

    public void validarConfirmacionPago(PagoTributario pago) {
        if (pago.getEstado() == EstadoPagoTributario.CONFIRMADO || pago.getEstado() == EstadoPagoTributario.PROCESADO) {
            throw new BusinessException("El pago " + pago.getCodigoPago() + " ya se encuentra confirmado");
        }
        if (pago.getEstado() == EstadoPagoTributario.ANULADO || pago.getEstado() == EstadoPagoTributario.REVERTIDO) {
            throw new BusinessException("No se puede confirmar un pago que ha sido anulado o revertido");
        }
    }

    public void validarReversionPago(PagoTributario pago, String motivo) {
        if (pago.getEstado() != EstadoPagoTributario.CONFIRMADO && pago.getEstado() != EstadoPagoTributario.PROCESADO) {
            throw new BusinessException("Sólo pueden revertirse financieramente pagos que se encuentren en estado CONFIRMADO o PROCESADO");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new BusinessException("Debe proporcionar un motivo de reversión justificado y auditable");
        }
    }

    public void validarAperturaSesion(UUID cajaId, Long cajeroId) {
        CajaMunicipal caja = cajaRepository.findById(cajaId)
                .orElseThrow(() -> new BusinessException("La caja municipal especificada no existe"));

        if (caja.getEstado() != EstadoCajaMunicipal.ACTIVA) {
            throw new BusinessException("La caja municipal " + caja.getCodigo() + " no se encuentra ACTIVA para operar");
        }

        Optional<SesionCajaMunicipal> sesionCajaActiva = sesionCajaRepository.findSesionActivaPorCaja(cajaId);
        if (sesionCajaActiva.isPresent()) {
            throw new BusinessException("La caja " + caja.getCodigo() + " ya tiene una sesión abierta (" +
                    sesionCajaActiva.get().getCodigoSesion() + "). Debe cerrarse antes de aperturar un nuevo turno.");
        }

        if (cajeroId != null) {
            Optional<SesionCajaMunicipal> sesionCajeroActiva = sesionCajaRepository.findSesionActivaPorCajero(cajeroId);
            if (sesionCajeroActiva.isPresent()) {
                throw new BusinessException("El cajero ya cuenta con una sesión activa (" +
                        sesionCajeroActiva.get().getCodigoSesion() + ") en otra ventanilla.");
            }
        }
    }

    public void validarCierreSesion(SesionCajaMunicipal sesion) {
        if (sesion.getEstado() == EstadoSesionCaja.CERRADA) {
            throw new BusinessException("La sesión de caja " + sesion.getCodigoSesion() + " ya se encuentra cerrada");
        }
    }
}
