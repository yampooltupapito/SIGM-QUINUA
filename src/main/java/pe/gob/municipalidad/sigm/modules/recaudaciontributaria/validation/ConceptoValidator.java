package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ConceptoCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ConceptoUpdateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ConfiguracionCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ParametroCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConfiguracionConceptoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ParametroTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConfiguracionConcepto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ConceptoTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ConfiguracionConceptoTributarioRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Validador de reglas normativas, temporales y de integridad de conceptos y configuraciones tributarias.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConceptoValidator {

    private final ConceptoTributarioRepository conceptoRepository;
    private final ConfiguracionConceptoTributarioRepository configuracionRepository;

    public void validarCreacionConcepto(ConceptoCreateRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de creación de concepto no puede ser nula.");
        }

        // 1. Unicidad y formato de código
        if (request.getCodigo() == null || !request.getCodigo().matches("^[A-Z0-9_]{3,50}$")) {
            throw new BusinessException("El código del concepto debe contener solo mayúsculas, números o guiones bajos (entre 3 y 50 caracteres).");
        }

        if (conceptoRepository.existsByCodigoIgnoreCase(request.getCodigo().trim())) {
            throw new BusinessException("El código '" + request.getCodigo().trim() + "' ya se encuentra registrado en el catálogo institucional.");
        }

        // 2. Coherencia de fechas
        validarRangoFechas(request.getFechaInicioVigencia(), request.getFechaFinVigencia());
    }

    public void validarActualizacionConcepto(UUID id, ConceptoUpdateRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de actualización no puede ser nula.");
        }

        validarRangoFechas(request.getFechaInicioVigencia(), request.getFechaFinVigencia());
    }

    public void validarCreacionConfiguracion(ConfiguracionCreateRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de configuración no puede ser nula.");
        }

        validarRangoFechas(request.getFechaInicioVigencia(), request.getFechaFinVigencia());
    }

    public void validarParametro(ParametroCreateRequest request) {
        if (request == null) {
            throw new BusinessException("El parámetro a registrar no puede ser nulo.");
        }

        if (request.getCodigo() == null || request.getCodigo().trim().isEmpty()) {
            throw new BusinessException("El código del parámetro es obligatorio.");
        }

        if (request.getTipoDato() == null) {
            throw new BusinessException("El tipo de dato del parámetro es obligatorio.");
        }

        if (request.getTipoDato() == pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDatoParametro.DECIMAL) {
            if (request.getValorDecimal() != null && request.getValorDecimal().compareTo(BigDecimal.ZERO) < 0) {
                // Alícuotas o importes no pueden ser negativos
                throw new BusinessException("El valor decimal o alícuota del parámetro no puede ser negativo.");
            }
        }

        if (request.getUnidad() == pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.UnidadMedidaParametro.PORCENTAJE) {
            if (request.getValorDecimal() != null && (request.getValorDecimal().compareTo(BigDecimal.ZERO) < 0 || request.getValorDecimal().compareTo(new BigDecimal("100")) > 0)) {
                throw new BusinessException("El valor porcentual debe estar comprendido entre 0 y 100.");
            }
        }
    }

    public void validarParaRevision(ConfiguracionConceptoTributario conf) {
        if (conf.getEstado() != EstadoConfiguracionConcepto.BORRADOR) {
            throw new BusinessException("Solo las configuraciones en estado BORRADOR pueden ser enviadas a revisión.");
        }

        if (conf.getParametros().isEmpty()) {
            throw new BusinessException("La configuración debe contener al menos un parámetro antes de enviarse a revisión.");
        }
    }

    public void validarParaAprobacion(ConfiguracionConceptoTributario conf, String resolucionAprobacion) {
        if (conf.getEstado() != EstadoConfiguracionConcepto.EN_REVISION && conf.getEstado() != EstadoConfiguracionConcepto.BORRADOR) {
            throw new BusinessException("La configuración debe estar en revisión o borrador para ser aprobada.");
        }

        if (resolucionAprobacion == null || resolucionAprobacion.trim().isEmpty()) {
            throw new BusinessException("Debe especificar la resolución de alcaldía, acuerdo o sustento formal de aprobación.");
        }

        // Verificar parámetros obligatorios
        for (ParametroTributario p : conf.getParametros()) {
            if (Boolean.TRUE.equals(p.getEsObligatorio()) && p.getValorDecimal() == null && p.getValorTexto() == null && p.getValorBooleano() == null && p.getValorFecha() == null) {
                throw new BusinessException("El parámetro obligatorio '" + p.getNombre() + "' (" + p.getCodigo() + ") no tiene valor asignado.");
            }
        }
    }

    public void validarParaActivacion(ConfiguracionConceptoTributario conf) {
        if (conf.getEstado() != EstadoConfiguracionConcepto.APROBADO) {
            throw new BusinessException("Solo una configuración previamente APROBADA formalmente puede ser activada.");
        }

        // Detección de solapamiento de vigencia con otra configuración activa del mismo concepto y período
        UUID sectorId = conf.getSector() != null ? conf.getSector().getId() : null;
        boolean solapa = configuracionRepository.existeSolapamientoActivo(
                conf.getConcepto().getId(),
                conf.getPeriodo().getId(),
                conf.getFechaInicioVigencia(),
                conf.getFechaFinVigencia(),
                sectorId,
                conf.getId()
        );

        if (solapa) {
            throw new BusinessException("Existe un conflicto de superposición: ya existe una versión ACTIVA para este concepto y período en el mismo ámbito territorial y temporal.");
        }
    }

    public void validarParaNuevaVersion(ConfiguracionConceptoTributario conf) {
        if (!conf.esAprobadoOActivo()) {
            throw new BusinessException("Solo se pueden generar nuevas versiones a partir de configuraciones APROBADAS o ACTIVAS.");
        }
    }

    public void validarCreacion(ConceptoCreateRequest request) {
        validarCreacionConcepto(request);
    }

    public void validarModificable(ConfiguracionConceptoTributario conf) {
        if (conf == null) {
            throw new BusinessException("La configuración tributaria no puede ser nula.");
        }
        if (!conf.isEditable()) {
            throw new BusinessException("La versión " + conf.getNumeroVersion() + " está en estado " + conf.getEstado() + " y no puede ser modificada directamente.");
        }
    }

    public void validarAprobacion(ConfiguracionConceptoTributario conf, String resolucionAprobacion) {
        validarParaAprobacion(conf, resolucionAprobacion);
    }

    public void validarActivacion(ConfiguracionConceptoTributario conf) {
        validarParaActivacion(conf);
    }

    public void validarDesactivacion(ConfiguracionConceptoTributario conf, String motivo) {
        if (conf == null) {
            throw new BusinessException("La configuración no puede ser nula.");
        }
        if (conf.getEstado() != EstadoConfiguracionConcepto.ACTIVO) {
            throw new BusinessException("Solo se pueden desactivar configuraciones que estén actualmente ACTIVAS.");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new BusinessException("Debe especificar un motivo fundamentado para la desactivación de la versión.");
        }
    }

    public void validarRangoFechas(LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            throw new BusinessException("La fecha de fin de vigencia (" + fin + ") no puede ser anterior a la fecha de inicio (" + inicio + ").");
        }
    }
}
