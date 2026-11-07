package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.AccionObligacionRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.GeneracionObligacionRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CalculoArbitrioMunicipal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CalculoImpuestoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDeterminacionTributaria;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
public class ObligacionValidator {

    public void validarGeneracionIndividual(GeneracionObligacionRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de generación no puede ser nula.");
        }
        if (request.getTipoDeterminacion() == null) {
            throw new BusinessException("Debe especificar el tipo de determinación tributaria.");
        }
        if (request.getDeterminacionId() == null) {
            throw new BusinessException("Debe indicar el identificador de la determinación autorizada.");
        }
        if (request.getCuotas() == null || request.getCuotas() < 1 || request.getCuotas() > 12) {
            throw new BusinessException("El número de cuotas debe ser un valor entero entre 1 y 12.");
        }
    }

    public void validarElegibilidadPredial(CalculoImpuestoPredial calculo, boolean yaExisteObligacionActiva) {
        if (calculo == null) {
            throw new BusinessException("La determinación del Impuesto Predial no fue encontrada.");
        }
        if (calculo.getEstado() != EstadoCalculoPredial.APROBADO) {
            throw new BusinessException("Solo determinaciones en estado APROBADO pueden generar obligaciones tributarias. Estado actual: " + calculo.getEstado());
        }
        if (calculo.getImpuestoAnualDeterminado() == null || calculo.getImpuestoAnualDeterminado().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El impuesto anual determinado debe ser un monto mayor o igual a cero.");
        }
        if (yaExisteObligacionActiva) {
            throw new BusinessException("Ya existe una obligación tributaria activa registrada para la determinación predial " + calculo.getCodigoCalculo() + ".");
        }
    }

    public void validarElegibilidadArbitrio(CalculoArbitrioMunicipal calculo, boolean yaExisteObligacionActiva) {
        if (calculo == null) {
            throw new BusinessException("La determinación de Arbitrios Municipales no fue encontrada.");
        }
        if (calculo.getEstado() != EstadoCalculoArbitrio.APROBADO) {
            throw new BusinessException("Solo determinaciones de arbitrios en estado APROBADO pueden generar obligaciones. Estado actual: " + calculo.getEstado());
        }
        BigDecimal importe = calculo.getImportePeriodoTotal() != null && calculo.getImportePeriodoTotal().compareTo(BigDecimal.ZERO) > 0
                ? calculo.getImportePeriodoTotal() : calculo.getImporteAnualTotal();
        if (importe == null || importe.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El importe de arbitrios determinado no es válido.");
        }
        if (yaExisteObligacionActiva) {
            throw new BusinessException("Ya existe una obligación tributaria activa registrada para la liquidación de arbitrios " + calculo.getCodigoCalculo() + ".");
        }
    }

    public void validarAnulacion(ObligacionTributaria obligacion, AccionObligacionRequest request) {
        if (obligacion == null) {
            throw new BusinessException("Obligación tributaria no encontrada.");
        }
        if (obligacion.getEstado() == EstadoObligacionTributaria.ANULADA) {
            throw new BusinessException("La obligación tributaria ya se encuentra anulada.");
        }
        if (request == null || request.getMotivo() == null || request.getMotivo().trim().length() < 10) {
            throw new BusinessException("Debe ingresar un motivo formal de anulación de al menos 10 caracteres.");
        }
        if (request.getResolucionSustento() == null || request.getResolucionSustento().trim().isEmpty()) {
            throw new BusinessException("Debe consignar el documento o resolución de sustento de la anulación.");
        }
    }

    public void validarSuspension(ObligacionTributaria obligacion, AccionObligacionRequest request) {
        if (obligacion == null) {
            throw new BusinessException("Obligación tributaria no encontrada.");
        }
        if (obligacion.getEstado() == EstadoObligacionTributaria.SUSPENDIDA) {
            throw new BusinessException("La obligación tributaria ya se encuentra suspendida.");
        }
        if (obligacion.getEstado() == EstadoObligacionTributaria.ANULADA) {
            throw new BusinessException("No se puede suspender una obligación que ha sido anulada.");
        }
        if (request == null || request.getMotivo() == null || request.getMotivo().trim().length() < 10) {
            throw new BusinessException("Debe registrar un sustento detallado de la suspensión legal (mínimo 10 caracteres).");
        }
    }

    public void validarRectificacion(ObligacionTributaria obligacion, AccionObligacionRequest request) {
        if (obligacion == null) {
            throw new BusinessException("Obligación tributaria a rectificar no encontrada.");
        }
        if (obligacion.getEstado() == EstadoObligacionTributaria.ANULADA) {
            throw new BusinessException("No se puede rectificar una obligación anulada.");
        }
        if (request == null || request.getNuevaDeterminacionId() == null) {
            throw new BusinessException("Debe proporcionar el identificador de la nueva determinación aprobada para la rectificación.");
        }
        if (request.getMotivo() == null || request.getMotivo().trim().length() < 10) {
            throw new BusinessException("Debe detallar el motivo de la rectificación técnica (mínimo 10 caracteres).");
        }
    }
}
