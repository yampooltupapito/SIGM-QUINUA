package pe.gob.municipalidad.sigm.modules.mercados.service;

import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.common.exception.InvalidRequestException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PuestoComercialCreateRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PuestoComercialUpdateRequest;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.repository.PuestoComercialRepository;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Validador de negocio para la administración de puestos comerciales.
 */
@Component
public class PuestoComercialValidator {

    private final PuestoComercialRepository puestoRepository;

    public PuestoComercialValidator(PuestoComercialRepository puestoRepository) {
        this.puestoRepository = puestoRepository;
    }

    /**
     * Valida la creación de un nuevo puesto comercial.
     */
    public void validarRegistro(PuestoComercialCreateRequest request, MercadoMunicipal mercado,
                                SectorMercado sector, PabellonMercado pabellon, ZonaComercial zona) {
        if (request.getMercadoId() == null) {
            throw new InvalidRequestException("El ID del mercado es obligatorio.");
        }
        if (request.getCodigo() == null || request.getCodigo().isBlank()) {
            throw new InvalidRequestException("El código del puesto es obligatorio.");
        }
        if (request.getNumero() == null || request.getNumero().isBlank()) {
            throw new InvalidRequestException("El número de puesto es obligatorio.");
        }
        if (request.getTipoPuestoId() == null) {
            throw new InvalidRequestException("El tipo de puesto comercial es obligatorio.");
        }

        String codigoNormalizado = request.getCodigo().trim().toUpperCase();
        if (puestoRepository.existsByMercadoIdAndCodigoIgnoreCase(mercado.getId(), codigoNormalizado)) {
            throw new DuplicateResourceException("El código de puesto '" + codigoNormalizado + "' ya existe en el mercado " + mercado.getNombre());
        }

        validarJerarquiaFisica(mercado, sector, pabellon, zona);
        validarDimensiones(request.getAreaMetrosCuadrados(), request.getAnchoMetros(), request.getLargoMetros());
    }

    /**
     * Valida la actualización técnica de un puesto comercial existente.
     */
    public void validarActualizacion(UUID puestoId, UUID mercadoId, PuestoComercialUpdateRequest request) {
        validarDimensiones(request.getAreaMetrosCuadrados(), request.getAnchoMetros(), request.getLargoMetros());
    }

    /**
     * Valida dimensiones técnicas (ancho, largo, área).
     */
    public void validarDimensiones(BigDecimal area, BigDecimal ancho, BigDecimal largo) {
        if (area != null && area.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("El área en metros cuadrados no puede ser negativa (no puede ser un valor negativo).");
        }
        if (ancho != null && ancho.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("El ancho en metros no puede ser negativo.");
        }
        if (largo != null && largo.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("El largo en metros no puede ser negativo.");
        }
    }

    /**
     * Valida la coherencia de relaciones físicas entre mercado, sector, pabellón y zona.
     */
    public void validarJerarquiaFisica(MercadoMunicipal mercado, SectorMercado sector,
                                       PabellonMercado pabellon, ZonaComercial zona) {
        if (sector != null && !sector.getMercado().getId().equals(mercado.getId())) {
            throw new BusinessException("El sector especificado no pertenece al mercado indicado.");
        }

        if (pabellon != null) {
            if (!pabellon.getMercado().getId().equals(mercado.getId())) {
                throw new BusinessException("El pabellón especificado no pertenece al mercado indicado.");
            }
            if (sector != null && pabellon.getSector() != null && !pabellon.getSector().getId().equals(sector.getId())) {
                throw new BusinessException("El pabellón seleccionado pertenece a un sector distinto al indicado.");
            }
        }

        if (zona != null && !zona.getMercado().getId().equals(mercado.getId())) {
            throw new BusinessException("La zona especificada no pertenece al mercado indicado.");
        }
    }
}
