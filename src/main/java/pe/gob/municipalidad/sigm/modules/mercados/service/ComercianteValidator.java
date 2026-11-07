package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaJuridica;
import pe.gob.municipalidad.sigm.common.repository.PersonaJuridicaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteJuridicoCreateRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteNaturalCreateRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.RepresentanteRequest;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ComercianteRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.verification.IdentidadVerificationService;

import java.util.Optional;
import java.util.UUID;

/**
 * Validador de reglas de negocio para el registro y administración de comerciantes municipales.
 */
@Component
@RequiredArgsConstructor
public class ComercianteValidator {

    private final ComercianteRepository comercianteRepository;
    private final PersonaRepository personaRepository;
    private final PersonaJuridicaRepository personaJuridicaRepository;
    private final IdentidadVerificationService identidadVerificationService;

    public void validarRegistroNatural(ComercianteNaturalCreateRequest request) {
        String dni = request.getNumeroDocumento() != null ? request.getNumeroDocumento().trim() : "";
        if (!identidadVerificationService.validarFormatoDni(dni)) {
            throw new BusinessException("El DNI '" + dni + "' no es válido. Debe contener exactamente 8 dígitos numéricos.");
        }

        // Si ya existe la persona en el sistema, verificar que no tenga ya un registro de comerciante
        Optional<Persona> personaOpt = personaRepository.findByTipoDocumentoAndNumeroDocumento("DNI", dni);
        if (personaOpt.isPresent()) {
            if (comercianteRepository.existsByPersonaId(personaOpt.get().getId())) {
                throw new DuplicateResourceException("La persona con DNI " + dni + " ya se encuentra registrada como comerciante municipal.");
            }
        }

        // Validar código si se especificó manualmente
        if (request.getCodigoComerciante() != null && !request.getCodigoComerciante().trim().isEmpty()) {
            String codigo = request.getCodigoComerciante().trim().toUpperCase();
            if (comercianteRepository.existsByCodigoComerciante(codigo)) {
                throw new DuplicateResourceException("El código de comerciante '" + codigo + "' ya se encuentra registrado en el padrón.");
            }
        }
    }

    public void validarRegistroJuridico(ComercianteJuridicoCreateRequest request) {
        String ruc = request.getRuc() != null ? request.getRuc().trim() : "";
        if (!identidadVerificationService.validarFormatoRuc(ruc)) {
            throw new BusinessException("El RUC '" + ruc + "' no es válido según las reglas matemáticas oficiales de SUNAT.");
        }

        // Verificar si la persona jurídica ya tiene un registro de comerciante
        Optional<PersonaJuridica> pjOpt = personaJuridicaRepository.findByRuc(ruc);
        if (pjOpt.isPresent()) {
            if (comercianteRepository.existsByPersonaJuridicaId(pjOpt.get().getId())) {
                throw new DuplicateResourceException("La empresa con RUC " + ruc + " ya se encuentra registrada como comerciante municipal.");
            }
        }

        // Validar código si se especificó manualmente
        if (request.getCodigoComerciante() != null && !request.getCodigoComerciante().trim().isEmpty()) {
            String codigo = request.getCodigoComerciante().trim().toUpperCase();
            if (comercianteRepository.existsByCodigoComerciante(codigo)) {
                throw new DuplicateResourceException("El código de comerciante '" + codigo + "' ya se encuentra registrado en el padrón.");
            }
        }

        // Validar representante si se especificó
        if (request.getRepresentanteDni() != null && !request.getRepresentanteDni().trim().isEmpty()) {
            String dniRep = request.getRepresentanteDni().trim();
            if (!identidadVerificationService.validarFormatoDni(dniRep)) {
                throw new BusinessException("El DNI del representante legal '" + dniRep + "' no es válido.");
            }
        }
    }

    public void validarActualizacionCodigo(UUID comercianteId, String nuevoCodigo) {
        if (nuevoCodigo == null || nuevoCodigo.trim().isEmpty()) {
            return;
        }
        String codigo = nuevoCodigo.trim().toUpperCase();
        if (comercianteRepository.existsByCodigoComercianteAndIdNot(codigo, comercianteId)) {
            throw new DuplicateResourceException("El código de comerciante '" + codigo + "' ya está siendo utilizado por otro comerciante.");
        }
    }

    public void validarRepresentante(RepresentanteRequest request) {
        if (request.getDni() != null && !request.getDni().trim().isEmpty()) {
            String dni = request.getDni().trim();
            if (!identidadVerificationService.validarFormatoDni(dni)) {
                throw new BusinessException("El DNI del representante '" + dni + "' no es válido. Debe contener 8 dígitos numéricos.");
            }
        }

        if (request.getFechaFin() != null && request.getFechaInicio() != null) {
            if (request.getFechaFin().isBefore(request.getFechaInicio())) {
                throw new BusinessException("La fecha de fin de la representación no puede ser anterior a la fecha de inicio.");
            }
        }
    }
}
