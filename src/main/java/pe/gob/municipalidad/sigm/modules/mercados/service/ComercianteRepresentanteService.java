package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaJuridica;
import pe.gob.municipalidad.sigm.common.model.PersonaJuridicaRepresentante;
import pe.gob.municipalidad.sigm.common.repository.PersonaJuridicaRepresentanteRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.RepresentanteRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.RepresentanteResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ComercianteRepository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio encargado de gestionar los representantes legales de los comerciantes personas jurídicas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComercianteRepresentanteService {

    private final ComercianteRepository comercianteRepository;
    private final PersonaRepository personaRepository;
    private final PersonaJuridicaRepresentanteRepository representanteRepository;
    private final DocumentoRepository documentoRepository;
    private final ComercianteValidator validator;
    private final AuditoriaAdministrativaService auditoriaService;

    @Transactional(readOnly = true)
    public List<RepresentanteResponse> listarRepresentantesPorComerciante(UUID comercianteId) {
        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + comercianteId));

        if (!comerciante.isPersonaJuridica() || comerciante.getPersonaJuridica() == null) {
            return Collections.emptyList();
        }

        UUID pjId = comerciante.getPersonaJuridica().getId();
        return representanteRepository.findByPersonaJuridicaId(pjId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RepresentanteResponse agregarRepresentante(UUID comercianteId, RepresentanteRequest request) {
        validator.validarRepresentante(request);

        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + comercianteId));

        if (!comerciante.isPersonaJuridica() || comerciante.getPersonaJuridica() == null) {
            throw new BusinessException("Solo se pueden asociar representantes legales a comerciantes constituidos como Persona Jurídica.");
        }

        PersonaJuridica pj = comerciante.getPersonaJuridica();
        Persona persona;

        if (request.getPersonaId() != null) {
            persona = personaRepository.findById(request.getPersonaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con ID: " + request.getPersonaId()));
        } else if (request.getDni() != null && !request.getDni().trim().isEmpty()) {
            String dni = request.getDni().trim();
            persona = personaRepository.findByTipoDocumentoAndNumeroDocumento("DNI", dni)
                    .orElseGet(() -> {
                        Persona nueva = Persona.builder()
                                .tipoDocumento("DNI")
                                .numeroDocumento(dni)
                                .nombres(request.getNombres() != null ? request.getNombres().trim() : "Por Especificar")
                                .apellidoPaterno(request.getApellidoPaterno() != null ? request.getApellidoPaterno().trim() : "Por Especificar")
                                .apellidoMaterno(request.getApellidoMaterno() != null ? request.getApellidoMaterno().trim() : null)
                                .estado("ACTIVO")
                                .build();
                        return personaRepository.save(nueva);
                    });
        } else {
            throw new BusinessException("Debe especificar una Persona existente o el DNI del nuevo representante.");
        }

        Documento documento = null;
        if (request.getDocumentoSustentoId() != null) {
            documento = documentoRepository.findById(request.getDocumentoSustentoId()).orElse(null);
        }

        PersonaJuridicaRepresentante rep = PersonaJuridicaRepresentante.builder()
                .personaJuridica(pj)
                .persona(persona)
                .tipoRepresentacion(request.getTipoRepresentacion())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .documentoSustento(documento)
                .estado(request.getEstado() != null ? request.getEstado() : "ACTIVO")
                .build();

        PersonaJuridicaRepresentante guardado = representanteRepository.save(rep);

        auditoriaService.registrar("AGREGAR_REPRESENTANTE_PJ", "personas_juridicas_representantes", guardado.getId().toString(), "EXITO",
                "Agregado representante " + persona.getNombreCompleto() + " (" + request.getTipoRepresentacion() + ") a empresa RUC " + pj.getRuc());

        return toResponse(guardado);
    }

    public RepresentanteResponse toResponse(PersonaJuridicaRepresentante r) {
        Persona p = r.getPersona();
        Documento doc = r.getDocumentoSustento();
        return RepresentanteResponse.builder()
                .id(r.getId())
                .personaId(p != null ? p.getId() : null)
                .dni(p != null ? p.getNumeroDocumento() : null)
                .nombreCompleto(p != null ? p.getNombreCompleto() : null)
                .tipoRepresentacion(r.getTipoRepresentacion())
                .tipoRepresentacionDescripcion(r.getTipoRepresentacion() != null ? r.getTipoRepresentacion().getDescripcion() : null)
                .fechaInicio(r.getFechaInicio())
                .fechaFin(r.getFechaFin())
                .vigente(r.isVigente())
                .documentoSustentoId(doc != null ? doc.getId() : null)
                .documentoSustentoCodigo(doc != null ? doc.getNombreOriginal() : null)
                .estado(r.getEstado())
                .build();
    }
}
