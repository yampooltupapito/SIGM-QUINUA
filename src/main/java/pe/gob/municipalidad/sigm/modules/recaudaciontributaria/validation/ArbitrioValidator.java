package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.CalculoArbitrioCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ConfiguracionArbitrioCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.SimulacionArbitrioRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArbitrioValidator {

    private final ContribuyenteTributarioRepository contribuyenteRepository;
    private final PredioTributarioRepository predioRepository;
    private final PeriodoTributarioRepository periodoRepository;
    private final RelacionContribuyentePredioRepository relacionRepository;
    private final ConfiguracionArbitrioMunicipalRepository configuracionRepository;

    public void validarConfiguracion(ConfiguracionArbitrioCreateRequest request) {
        if (request.getServicioId() == null) {
            throw new BusinessException("El servicio municipal de arbitrios es obligatorio.");
        }
        if (request.getPeriodoId() == null) {
            throw new BusinessException("El período fiscal para la configuración es obligatorio.");
        }
        if (request.getFechaInicioVigencia() == null) {
            throw new BusinessException("La fecha de inicio de vigencia es obligatoria.");
        }
        if (request.getFechaFinVigencia() != null && request.getFechaFinVigencia().isBefore(request.getFechaInicioVigencia())) {
            throw new BusinessException("La fecha de fin de vigencia no puede ser anterior a la fecha de inicio.");
        }
        if (request.getCostos() == null || request.getCostos().isEmpty()) {
            throw new BusinessException("La configuración debe contar con al menos un componente de costo debidamente sustentado.");
        }
        for (var c : request.getCostos()) {
            if (c.getImporte() == null || c.getImporte().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("El importe del componente de costo '" + c.getDescripcion() + "' debe ser mayor a cero.");
            }
        }
    }

    public void validarParaAprobacionConfiguracion(ConfiguracionArbitrioMunicipal config) {
        if (config.getNorma() == null) {
            throw new BusinessException("No se puede aprobar la configuración del arbitrio sin asociar la Ordenanza Municipal ratificada.");
        }
        if (config.getCostos() == null || config.getCostos().isEmpty()) {
            throw new BusinessException("No se puede aprobar una configuración de arbitrios sin estructura de costos registrada.");
        }
        if (config.getCostoTotalAnual() == null || config.getCostoTotalAnual().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El costo total anual debe ser mayor a cero para ser aprobado.");
        }
        if (config.getEstado() == EstadoConfiguracionArbitrio.APROBADO || config.getEstado() == EstadoConfiguracionArbitrio.ACTIVO) {
            throw new BusinessException("La configuración ya se encuentra en estado " + config.getEstado());
        }
    }

    public void validarSimulacion(SimulacionArbitrioRequest request, List<String> advertencias) {
        if (request.getContribuyenteId() == null) {
            throw new BusinessException("El contribuyente es obligatorio para la simulación.");
        }
        if (request.getPredioId() == null) {
            throw new BusinessException("El predio es obligatorio para la simulación.");
        }
        if (request.getPeriodoId() == null) {
            throw new BusinessException("El período fiscal es obligatorio.");
        }

        ContribuyenteTributario cont = contribuyenteRepository.findById(request.getContribuyenteId())
                .orElseThrow(() -> new BusinessException("El contribuyente especificado no existe."));

        if (cont.getEstado() != EstadoContribuyente.ACTIVO) {
            advertencias.add("El contribuyente figura con estado " + cont.getEstado() + " en el padrón tributario.");
        }

        PredioTributario predio = predioRepository.findById(request.getPredioId())
                .orElseThrow(() -> new BusinessException("El predio especificado no existe."));

        if (predio.getEstado() != EstadoPredio.ACTIVO) {
            advertencias.add("El predio registra estado " + predio.getEstado() + ".");
        }

        PeriodoTributario periodo = periodoRepository.findById(request.getPeriodoId())
                .orElseThrow(() -> new BusinessException("El período fiscal no existe."));

        if (periodo.getEstado() == EstadoPeriodoTributario.CERRADO) {
            advertencias.add("El período fiscal " + periodo.getAnio() + " está CERRADO. La determinación es histórica.");
        }

        // Validar titularidad
        boolean tieneRelacion = relacionRepository.existsByContribuyenteIdAndPredioIdAndEstado(
                cont.getId(), predio.getId(), EstadoRelacionContribuyentePredio.ACTIVO
        );
        if (!tieneRelacion) {
            advertencias.add("No se encontró vínculo activo de titularidad registrado entre el contribuyente y el predio. Verifique copropiedad.");
        }
    }

    public void validarCalculoPreliminar(CalculoArbitrioCreateRequest request) {
        if (request.getContribuyenteId() == null) {
            throw new BusinessException("El contribuyente es obligatorio.");
        }
        if (request.getPredioId() == null) {
            throw new BusinessException("El predio es obligatorio.");
        }
        if (request.getPeriodoId() == null) {
            throw new BusinessException("El período fiscal es obligatorio.");
        }

        PeriodoTributario periodo = periodoRepository.findById(request.getPeriodoId())
                .orElseThrow(() -> new BusinessException("El período fiscal no existe."));

        if (periodo.getEstado() == EstadoPeriodoTributario.CERRADO) {
            throw new BusinessException("No se pueden registrar nuevos cálculos en un ejercicio fiscal cerrado (" + periodo.getAnio() + ").");
        }

        ContribuyenteTributario cont = contribuyenteRepository.findById(request.getContribuyenteId())
                .orElseThrow(() -> new BusinessException("El contribuyente no existe."));

        if (cont.getEstado() != EstadoContribuyente.ACTIVO) {
            throw new BusinessException("El contribuyente está en estado inactivo o bloqueado.");
        }

        PredioTributario predio = predioRepository.findById(request.getPredioId())
                .orElseThrow(() -> new BusinessException("El predio no existe."));

        if (predio.getEstado() != EstadoPredio.ACTIVO) {
            throw new BusinessException("El predio está en estado no activo (" + predio.getEstado() + ").");
        }
    }

    public void validarPredioParaCalculo(PredioTributario predio) {
        if (predio == null) {
            throw new BusinessException("El predio es obligatorio para el cálculo.");
        }
        if (predio.getEstado() != EstadoPredio.ACTIVO) {
            throw new BusinessException("El predio no se encuentra en estado ACTIVO (" + predio.getEstado() + ").");
        }
    }

    public void validarPorcentajeCondomino(BigDecimal porcentaje) {
        if (porcentaje == null || porcentaje.compareTo(BigDecimal.ZERO) <= 0 || porcentaje.compareTo(new BigDecimal("100.00")) > 0) {
            throw new BusinessException("El porcentaje de copropiedad debe ser mayor a 0% y menor o igual a 100%.");
        }
    }

    public void validarPeriodoParaCalculo(PeriodoTributario periodo) {
        if (periodo == null) {
            throw new BusinessException("El período fiscal es obligatorio.");
        }
        if (periodo.getEstado() == EstadoPeriodoTributario.CERRADO) {
            throw new BusinessException("No se pueden registrar cálculos en un ejercicio fiscal cerrado (" + periodo.getAnio() + ").");
        }
    }

    public void validarTransicionEstado(EstadoCalculoArbitrio actual, EstadoCalculoArbitrio nuevoEstado) {
        if (actual == EstadoCalculoArbitrio.APROBADO && nuevoEstado != EstadoCalculoArbitrio.ANULADO) {
            throw new BusinessException("Un cálculo de arbitrios APROBADO no puede retroceder de estado. Genere una nueva versión o anúlelo.");
        }
        if (actual == EstadoCalculoArbitrio.ANULADO) {
            throw new BusinessException("Un cálculo ANULADO es definitivo e inmutable.");
        }
        if (actual == EstadoCalculoArbitrio.SIMULACION && nuevoEstado == EstadoCalculoArbitrio.APROBADO) {
            throw new BusinessException("Una simulación no puede aprobarse directamente sin pasar por determinación preliminar y revisión.");
        }
    }

    public void validarTransicionEstado(CalculoArbitrioMunicipal calculo, EstadoCalculoArbitrio nuevoEstado) {
        validarTransicionEstado(calculo.getEstado(), nuevoEstado);
    }

    public void validarMotivo(String motivo, String accion) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new BusinessException("Es obligatorio especificar el motivo para " + accion + ".");
        }
    }
}
