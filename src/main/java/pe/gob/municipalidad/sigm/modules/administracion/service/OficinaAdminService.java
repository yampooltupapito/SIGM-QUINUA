package pe.gob.municipalidad.sigm.modules.administracion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Cargo;
import pe.gob.municipalidad.sigm.common.model.Municipalidad;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaOficina;
import pe.gob.municipalidad.sigm.common.model.TipoActor;
import pe.gob.municipalidad.sigm.common.repository.CargoRepository;
import pe.gob.municipalidad.sigm.common.repository.MunicipalidadRepository;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaOficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.TipoActorRepository;
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.AsignacionOficinaRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.AsignacionOficinaResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.OficinaCreateRequest;
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.OficinaResponse;
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.OficinaTreeDto;
import pe.gob.municipalidad.sigm.modules.administracion.dto.OficinaDtos.OficinaUpdateRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de administración de oficinas y unidades orgánicas municipales con control
 * estricto de prevención de ciclos y dependencias de personal.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OficinaAdminService {

    private final OficinaRepository oficinaRepository;
    private final MunicipalidadRepository municipalidadRepository;
    private final PersonaRepository personaRepository;
    private final CargoRepository cargoRepository;
    private final TipoActorRepository tipoActorRepository;
    private final PersonaOficinaRepository personaOficinaRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<OficinaResponse> buscarOficinas(String term, String estado, Pageable pageable) {
        return oficinaRepository.searchOficinas(
                (term != null && !term.isBlank()) ? term : null,
                (estado != null && !estado.isBlank()) ? estado : null,
                pageable
        ).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OficinaResponse> listarTodasActivas() {
        return oficinaRepository.findByEstado("ACTIVO").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OficinaResponse obtenerPorId(Long id) {
        Oficina o = oficinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oficina no encontrada con ID: " + id));
        return toResponse(o);
    }

    @Transactional(readOnly = true)
    public List<OficinaTreeDto> obtenerArbolJerarquico() {
        List<Oficina> todas = oficinaRepository.findAll();
        List<Oficina> raices = todas.stream()
                .filter(o -> o.getOficinaPadre() == null)
                .toList();

        return raices.stream()
                .map(r -> construirNodo(r, todas))
                .collect(Collectors.toList());
    }

    @Transactional
    public OficinaResponse crearOficina(OficinaCreateRequest request) {
        String codigo = request.getCodigo().trim().toUpperCase();
        if (oficinaRepository.existsByCodigo(codigo)) {
            throw new ConflictException("Ya existe una oficina registrada con el código: " + codigo);
        }

        Municipalidad muni;
        if (request.getMunicipalidadId() != null) {
            muni = municipalidadRepository.findById(request.getMunicipalidadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Municipalidad no encontrada"));
        } else {
            muni = municipalidadRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new BusinessException("ERR_MUNI_NOT_FOUND", "No existe entidad municipal registrada."));
        }

        Oficina oficinaPadre = null;
        int nivelJerarquico = 1;
        if (request.getOficinaPadreId() != null) {
            oficinaPadre = oficinaRepository.findById(request.getOficinaPadreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Oficina padre no encontrada"));
            nivelJerarquico = oficinaPadre.getNivelJerarquico() + 1;
        }

        Oficina nueva = Oficina.builder()
                .municipalidad(muni)
                .oficinaPadre(oficinaPadre)
                .codigo(codigo)
                .nombre(request.getNombre().trim())
                .sigla(request.getSigla() != null ? request.getSigla().trim().toUpperCase() : null)
                .descripcion(request.getDescripcion())
                .nivelJerarquico(nivelJerarquico)
                .estado("ACTIVO")
                .build();

        Oficina guardada = oficinaRepository.save(nueva);

        auditoriaService.registrar("CREAR_OFICINA", "OFICINA", guardada.getId(), "EXITO",
                "Oficina creada: " + guardada.getCodigo() + " - " + guardada.getNombre());

        return toResponse(guardada);
    }

    @Transactional
    public OficinaResponse actualizarOficina(Long id, OficinaUpdateRequest request) {
        Oficina oficina = oficinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oficina no encontrada con ID: " + id));

        Oficina nuevaPadre = null;
        int nivelJerarquico = 1;
        if (request.getOficinaPadreId() != null) {
            validarNoCiclosJerarquicos(id, request.getOficinaPadreId());
            nuevaPadre = oficinaRepository.findById(request.getOficinaPadreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Oficina padre no encontrada con ID: " + request.getOficinaPadreId()));
            nivelJerarquico = nuevaPadre.getNivelJerarquico() + 1;
        }

        oficina.setNombre(request.getNombre().trim());
        oficina.setSigla(request.getSigla() != null ? request.getSigla().trim().toUpperCase() : null);
        oficina.setDescripcion(request.getDescripcion());
        oficina.setOficinaPadre(nuevaPadre);
        oficina.setNivelJerarquico(nivelJerarquico);

        Oficina guardada = oficinaRepository.save(oficina);

        auditoriaService.registrar("ACTUALIZAR_OFICINA", "OFICINA", id, "EXITO",
                "Oficina actualizada: " + guardada.getCodigo() + " - " + guardada.getNombre());

        return toResponse(guardada);
    }

    @Transactional
    public OficinaResponse cambiarEstado(Long id, String nuevoEstado) {
        Oficina oficina = oficinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oficina no encontrada con ID: " + id));

        if (!"ACTIVO".equalsIgnoreCase(nuevoEstado)) {
            // Validar dependencias: personal activo
            List<PersonaOficina> asignacionesActivas = personaOficinaRepository.findByOficinaIdAndEstado(id, "ACTIVO");
            if (!asignacionesActivas.isEmpty()) {
                throw new BusinessException("ERR_OFICINA_PERSONAL_ACTIVO",
                        "No se puede desactivar la oficina '" + oficina.getNombre() +
                                "' porque cuenta con " + asignacionesActivas.size() + " trabajador(es) con asignación activa.");
            }

            // Validar dependencias: suboficinas activas
            List<Oficina> suboficinas = oficinaRepository.findByOficinaPadreId(id);
            boolean tieneHijasActivas = suboficinas.stream().anyMatch(s -> "ACTIVO".equalsIgnoreCase(s.getEstado()));
            if (tieneHijasActivas) {
                throw new BusinessException("ERR_OFICINA_SUBOFICINAS_ACTIVAS",
                        "No se puede desactivar la oficina porque contiene suboficinas activas subordinadas.");
            }
        }

        oficina.setEstado(nuevoEstado.toUpperCase());
        Oficina guardada = oficinaRepository.save(oficina);

        auditoriaService.registrar("CAMBIO_ESTADO_OFICINA", "OFICINA", id, "EXITO",
                "Estado de oficina " + oficina.getCodigo() + " cambiado a " + nuevoEstado);

        return toResponse(guardada);
    }

    @Transactional
    public AsignacionOficinaResponse asignarTrabajador(AsignacionOficinaRequest request) {
        Persona persona = personaRepository.findById(request.getPersonaId())
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con ID: " + request.getPersonaId()));

        Oficina oficina = oficinaRepository.findById(request.getOficinaId())
                .orElseThrow(() -> new ResourceNotFoundException("Oficina no encontrada con ID: " + request.getOficinaId()));

        if (!"ACTIVO".equalsIgnoreCase(oficina.getEstado())) {
            throw new BusinessException("ERR_OFICINA_INACTIVA", "No se puede asignar personal a una oficina inactiva.");
        }

        Cargo cargo = null;
        if (request.getCargoId() != null) {
            cargo = cargoRepository.findById(request.getCargoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cargo no encontrado con ID: " + request.getCargoId()));
        }

        TipoActor tipoActor = tipoActorRepository.findById(request.getTipoActorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de actor no encontrado con ID: " + request.getTipoActorId()));

        // Si es principal, desmarcar principales anteriores
        if (Boolean.TRUE.equals(request.getPrincipal())) {
            List<PersonaOficina> asignacionesPrevias = personaOficinaRepository.findByPersonaId(persona.getId());
            for (PersonaOficina prev : asignacionesPrevias) {
                if (Boolean.TRUE.equals(prev.getPrincipal())) {
                    prev.setPrincipal(false);
                    personaOficinaRepository.save(prev);
                }
            }
        }

        PersonaOficina asignacion = PersonaOficina.builder()
                .persona(persona)
                .oficina(oficina)
                .cargo(cargo)
                .tipoActor(tipoActor)
                .fechaInicio(request.getFechaInicio() != null ? request.getFechaInicio() : LocalDate.now())
                .principal(Boolean.TRUE.equals(request.getPrincipal()))
                .estado("ACTIVO")
                .build();

        PersonaOficina guardada = personaOficinaRepository.save(asignacion);

        auditoriaService.registrar("ASIGNAR_PERSONAL_OFICINA", "PERSONA_OFICINA", guardada.getId(), "EXITO",
                "Persona " + persona.getNumeroDocumento() + " asignada a oficina " + oficina.getCodigo());

        return AsignacionOficinaResponse.builder()
                .id(guardada.getId())
                .personaId(persona.getId())
                .personaNombre(persona.getNombreCompleto())
                .oficinaId(oficina.getId())
                .oficinaNombre(oficina.getNombre())
                .cargoId(cargo != null ? cargo.getId() : null)
                .cargoNombre(cargo != null ? cargo.getNombre() : null)
                .tipoActorId(tipoActor.getId())
                .tipoActorNombre(tipoActor.getNombre())
                .fechaInicio(guardada.getFechaInicio())
                .fechaFin(guardada.getFechaFin())
                .principal(guardada.getPrincipal())
                .estado(guardada.getEstado())
                .build();
    }

    /**
     * Valida recursivamente que la nueva oficina padre no cause ciclos en la jerarquía.
     */
    public void validarNoCiclosJerarquicos(Long oficinaId, Long nuevaPadreId) {
        if (oficinaId.equals(nuevaPadreId)) {
            throw new BusinessException("ERR_CICLO_JERARQUICO",
                    "Una oficina no puede seleccionarse como su propia oficina superior.");
        }

        Set<Long> ancestrosVisitados = new HashSet<>();
        ancestrosVisitados.add(oficinaId);

        Long actualPadreId = nuevaPadreId;
        while (actualPadreId != null) {
            if (ancestrosVisitados.contains(actualPadreId)) {
                throw new BusinessException("ERR_CICLO_JERARQUICO",
                        "La asignación de dependencia generaría un ciclo jerárquico cerrado en la estructura institucional.");
            }
            ancestrosVisitados.add(actualPadreId);

            Oficina actual = oficinaRepository.findById(actualPadreId).orElse(null);
            if (actual == null || actual.getOficinaPadre() == null) {
                break;
            }
            actualPadreId = actual.getOficinaPadre().getId();
        }
    }

    private OficinaTreeDto construirNodo(Oficina actual, List<Oficina> todas) {
        List<OficinaTreeDto> hijos = todas.stream()
                .filter(o -> o.getOficinaPadre() != null && o.getOficinaPadre().getId().equals(actual.getId()))
                .map(h -> construirNodo(h, todas))
                .collect(Collectors.toList());

        return OficinaTreeDto.builder()
                .id(actual.getId())
                .codigo(actual.getCodigo())
                .nombre(actual.getNombre())
                .sigla(actual.getSigla())
                .nivelJerarquico(actual.getNivelJerarquico())
                .estado(actual.getEstado())
                .suboficinas(hijos)
                .build();
    }

    private OficinaResponse toResponse(Oficina o) {
        Long padreId = o.getOficinaPadre() != null ? o.getOficinaPadre().getId() : null;
        String padreNombre = o.getOficinaPadre() != null ? o.getOficinaPadre().getNombre() : null;

        return OficinaResponse.builder()
                .id(o.getId())
                .codigo(o.getCodigo())
                .nombre(o.getNombre())
                .sigla(o.getSigla())
                .descripcion(o.getDescripcion())
                .oficinaPadreId(padreId)
                .oficinaPadreNombre(padreNombre)
                .nivelJerarquico(o.getNivelJerarquico())
                .estado(o.getEstado())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
