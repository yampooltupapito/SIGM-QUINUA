package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoPredialCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.SimulacionPredialRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CalculoImpuestoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PeriodoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoBeneficioPredial;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CalculoPredialValidator {

    public void validarSimulacion(SimulacionPredialRequest request, ContribuyenteTributario contribuyente, PeriodoTributario periodo, List<?> prediosRelacionados) {
        if (request == null) {
            throw new BusinessException("La solicitud de simulación no puede ser nula.");
        }
        if (contribuyente == null) {
            throw new BusinessException("El contribuyente especificado no existe o no se encuentra registrado.");
        }
        if (periodo == null) {
            throw new BusinessException("El período fiscal especificado no existe.");
        }
        if (periodo.getUitVigente() == null || periodo.getUitVigente().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El período fiscal " + periodo.getAnio() + " no tiene configurada una UIT oficial válida (> 0).");
        }
        if (prediosRelacionados == null || prediosRelacionados.isEmpty()) {
            throw new BusinessException("El contribuyente " + contribuyente.getCodigoContribuyente() + " no registra predios afectos bajo su titularidad en el distrito de Quinua.");
        }
        if (request.getBeneficio() == TipoBeneficioPredial.PENSIONISTA_ADULTO_MAYOR_50_UIT) {
            if (prediosRelacionados.size() > 1 && (request.getPrediosSeleccionadosIds() == null || request.getPrediosSeleccionadosIds().size() != 1)) {
                throw new BusinessException("El beneficio del Art. 19 (Deducción 50 UIT) exige la titularidad de un único predio destinado a vivienda propia o la selección expresa del predio casa-habitación afecto.");
            }
        }
    }

    public void validarRegistroCalculo(CalculoPredialCreateRequest request, ContribuyenteTributario contribuyente, PeriodoTributario periodo, List<?> prediosRelacionados) {
        if (request == null) {
            throw new BusinessException("La solicitud de determinación predial no puede ser nula.");
        }
        if (contribuyente == null) {
            throw new BusinessException("El contribuyente especificado no existe.");
        }
        if (periodo == null) {
            throw new BusinessException("El ejercicio fiscal es obligatorio.");
        }
        if (periodo.getUitVigente() == null || periodo.getUitVigente().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El período fiscal " + periodo.getAnio() + " no cuenta con UIT vigente oficial aprobada por el MEF.");
        }
        if (prediosRelacionados == null || prediosRelacionados.isEmpty()) {
            throw new BusinessException("No existen predios registrados para el contribuyente en el distrito de Quinua.");
        }
    }

    public void validarParaRevision(CalculoImpuestoPredial calculo) {
        if (calculo == null) {
            throw new BusinessException("El cálculo a revisar no puede ser nulo.");
        }
        if (calculo.getEstado() != EstadoCalculoPredial.PRELIMINAR && calculo.getEstado() != EstadoCalculoPredial.OBSERVADO) {
            throw new BusinessException("Solo se pueden enviar a revisión cálculos que se encuentren en estado PRELIMINAR u OBSERVADO.");
        }
    }

    public void validarParaAprobacion(CalculoImpuestoPredial calculo, String resolucionAprobacion) {
        if (calculo == null) {
            throw new BusinessException("El cálculo no puede ser nulo.");
        }
        if (calculo.getEstado() != EstadoCalculoPredial.EN_REVISION && calculo.getEstado() != EstadoCalculoPredial.PRELIMINAR) {
            throw new BusinessException("Solo se pueden aprobar cálculos en estado PRELIMINAR o EN_REVISION.");
        }
        if (resolucionAprobacion == null || resolucionAprobacion.trim().isEmpty()) {
            throw new BusinessException("Debe especificar la resolución u orden institucional que sustenta la aprobación de la determinación.");
        }
    }

    public void validarParaAnulacion(CalculoImpuestoPredial calculo, String motivo) {
        if (calculo == null) {
            throw new BusinessException("El cálculo no puede ser nulo.");
        }
        if (calculo.getEstado() == EstadoCalculoPredial.ANULADO) {
            throw new BusinessException("El cálculo ya se encuentra anulado.");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new BusinessException("Debe especificar un motivo formal y fundamentado para la anulación del cálculo.");
        }
    }
}
