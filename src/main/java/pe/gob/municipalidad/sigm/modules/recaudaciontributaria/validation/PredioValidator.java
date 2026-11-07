package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.PredioCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.PredioUpdateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.RelacionContribuyentePredioRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.PredioTributarioRepository;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Validador de reglas de negocio físicas, cartográficas y de integridad para predios municipales.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PredioValidator {

    private final PredioTributarioRepository predioRepository;

    public void validarCreacion(PredioCreateRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de registro de predio no puede ser nula.");
        }

        // 1. Validación de Áreas
        validarArea(request.getAreaTerreno(), request.getUnidadArea() != null ? request.getUnidadArea() : request.getUnidadMedidaArea());

        if (request.getAreaConstruida() != null && request.getAreaConstruida().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El área construida no puede ser negativa.");
        }

        // 2. Unicidad de Código Catastral si se proporciona
        validarCodigoCatastral(request.getCodigoCatastral(), null);

        // 3. Validación de Dirección o Ubicación Rural
        boolean tieneDireccion = (request.getDireccion() != null && !request.getDireccion().trim().isEmpty())
                || (request.getNombreVia() != null && !request.getNombreVia().trim().isEmpty());
        boolean tieneComunidad = request.getComunidadCentroPoblado() != null && !request.getComunidadCentroPoblado().trim().isEmpty();
        boolean tieneReferencia = request.getReferenciaUbicacion() != null && !request.getReferenciaUbicacion().trim().isEmpty();

        if (!tieneDireccion && !tieneComunidad && !tieneReferencia) {
            throw new BusinessException("Debe especificar al menos una dirección urbana o una comunidad/referencia rural para la ubicación del predio.");
        }

        // 4. Validación de Coordenadas Geográficas si se ingresan
        validarCoordenadas(request.getLatitud(), request.getLongitud());
    }

    public void validarActualizacion(UUID id, PredioUpdateRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de actualización no puede ser nula.");
        }

        validarArea(request.getAreaTerreno(), request.getUnidadArea());
        validarCodigoCatastral(request.getCodigoCatastral(), id);
        validarCoordenadas(request.getLatitud(), request.getLongitud());
    }

    public void validarArea(BigDecimal area, String unidad) {
        if (area == null || area.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El área del terreno debe ser un valor numérico positivo mayor a cero.");
        }
    }

    public BigDecimal calcularAreaEnMetrosCuadrados(BigDecimal area, String unidad) {
        if (area == null) {
            return BigDecimal.ZERO;
        }
        if (unidad != null && "HA".equalsIgnoreCase(unidad.trim())) {
            return area.multiply(new BigDecimal("10000"));
        }
        return area;
    }

    public void validarCodigoCatastral(String codigoCatastral, UUID excludeId) {
        if (codigoCatastral != null && !codigoCatastral.trim().isEmpty()) {
            String codCat = codigoCatastral.trim();
            boolean duplicado = (excludeId == null)
                    ? predioRepository.existsByCodigoCatastralIgnoreCase(codCat)
                    : predioRepository.existsByCodigoCatastralIgnoreCaseAndIdNot(codCat, excludeId);

            if (duplicado) {
                throw new DuplicateResourceException("El código catastral oficial '" + codCat + "' ya se encuentra registrado en el sistema.");
            }
        }
    }

    public void validarPorcentajeParticipacion(BigDecimal porcentaje) {
        if (porcentaje == null) {
            return;
        }
        if (porcentaje.compareTo(BigDecimal.ZERO) <= 0 || porcentaje.compareTo(new BigDecimal("100.00")) > 0) {
            throw new BusinessException("El porcentaje de participación debe ser mayor a 0% y menor o igual al 100%.");
        }
    }

    public void validarTransicionEstado(EstadoPredio actual, EstadoPredio nuevo, String motivo) {
        if (actual == nuevo) {
            throw new BusinessException("El nuevo estado no puede ser idéntico al estado administrativo actual.");
        }
        if (motivo == null || motivo.trim().isBlank()) {
            throw new BusinessException("Debe proporcionar un motivo o sustento formal para la transición de estado.");
        }
        if (actual == EstadoPredio.INACTIVO && nuevo == EstadoPredio.ACTIVO) {
            throw new BusinessException("Un predio inactivo no puede pasar a activo sin pasar previamente por revisión técnica.");
        }
    }

    public void validarRelacion(RelacionContribuyentePredioRequest request) {
        if (request == null) {
            throw new BusinessException("La solicitud de relación contribuyente-predio no puede ser nula.");
        }
        if (request.getContribuyenteId() == null) {
            throw new BusinessException("Debe seleccionar un contribuyente válido.");
        }
        if (request.getTipoRelacion() == null) {
            throw new BusinessException("Debe indicar el tipo de relación jurídica con el predio.");
        }
        validarPorcentajeParticipacion(request.getPorcentajeParticipacion() != null ? request.getPorcentajeParticipacion() : request.getPorcentajePropiedad());
    }

    public void validarCoordenadas(BigDecimal latitud, BigDecimal longitud) {
        if (latitud != null || longitud != null) {
            if (latitud == null || longitud == null) {
                throw new BusinessException("Debe proporcionar tanto latitud como longitud conjuntamente.");
            }

            // Latitud WGS84: -90 a 90
            if (latitud.compareTo(new BigDecimal("-90")) < 0 || latitud.compareTo(new BigDecimal("90")) > 0) {
                throw new BusinessException("La latitud debe estar comprendida entre -90 y 90 grados (WGS84).");
            }

            // Longitud WGS84: -180 a 180
            if (longitud.compareTo(new BigDecimal("-180")) < 0 || longitud.compareTo(new BigDecimal("180")) > 0) {
                throw new BusinessException("La longitud debe estar comprendida entre -180 y 180 grados (WGS84).");
            }
        }
    }
}
