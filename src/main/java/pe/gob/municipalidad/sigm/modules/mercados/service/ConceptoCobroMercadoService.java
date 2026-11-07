package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ConceptoCobroRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ConceptoCobroResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ConceptoCobroMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ConceptoCobroMercadoRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConceptoCobroMercadoService {

    private final ConceptoCobroMercadoRepository conceptoRepository;
    private final OficinaRepository oficinaRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public ConceptoCobroMercadoService(ConceptoCobroMercadoRepository conceptoRepository,
                                       OficinaRepository oficinaRepository,
                                       AuditoriaAdministrativaService auditoriaService) {
        this.conceptoRepository = conceptoRepository;
        this.oficinaRepository = oficinaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public ConceptoCobroResponse crear(ConceptoCobroRequest request, String username) {
        if (conceptoRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Ya existe un concepto de cobro con el código: " + request.getCodigo());
        }

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId()).orElse(null);
        }

        ConceptoCobroMercado entity = ConceptoCobroMercado.builder()
                .codigo(request.getCodigo().trim().toUpperCase())
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .tipoConcepto(request.getTipoConcepto())
                .periodicidad(request.getPeriodicidad())
                .estado(request.getEstado() != null ? request.getEstado() : "ACTIVO")
                .requiereAsignacion(request.isRequiereAsignacion())
                .oficinaResponsable(oficina)
                .build();

        ConceptoCobroMercado guardado = conceptoRepository.save(entity);

        auditoriaService.registrar("CREAR_CONCEPTO_COBRO_MERCADO", "mercados_conceptos_cobro", guardado.getId(),
                "EXITO", "Concepto de cobro creado: " + guardado.getCodigo());

        return toResponse(guardado);
    }

    @Transactional
    public ConceptoCobroResponse actualizar(UUID id, ConceptoCobroRequest request, String username) {
        ConceptoCobroMercado entity = conceptoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concepto de cobro no encontrado: " + id));

        if (!entity.getCodigo().equalsIgnoreCase(request.getCodigo()) && conceptoRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Ya existe otro concepto de cobro con el código: " + request.getCodigo());
        }

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId()).orElse(null);
        }

        entity.setCodigo(request.getCodigo().trim().toUpperCase());
        entity.setNombre(request.getNombre().trim());
        entity.setDescripcion(request.getDescripcion());
        entity.setTipoConcepto(request.getTipoConcepto());
        entity.setPeriodicidad(request.getPeriodicidad());
        entity.setEstado(request.getEstado());
        entity.setRequiereAsignacion(request.isRequiereAsignacion());
        entity.setOficinaResponsable(oficina);

        ConceptoCobroMercado guardado = conceptoRepository.save(entity);

        auditoriaService.registrar("ACTUALIZAR_CONCEPTO_COBRO_MERCADO", "mercados_conceptos_cobro", guardado.getId(),
                "EXITO", "Concepto de cobro actualizado: " + guardado.getCodigo());

        return toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public ConceptoCobroResponse obtenerPorId(UUID id) {
        return conceptoRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Concepto de cobro no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Page<ConceptoCobroResponse> buscarPaginado(String estado, String q, Pageable pageable) {
        return conceptoRepository.buscarPaginado(estado, q, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ConceptoCobroResponse> listarActivos() {
        return conceptoRepository.findByEstado("ACTIVO").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ConceptoCobroResponse toResponse(ConceptoCobroMercado c) {
        return ConceptoCobroResponse.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .nombre(c.getNombre())
                .descripcion(c.getDescripcion())
                .tipoConcepto(c.getTipoConcepto())
                .tipoConceptoDescripcion(c.getTipoConcepto() != null ? c.getTipoConcepto().getDescripcion() : null)
                .periodicidad(c.getPeriodicidad())
                .periodicidadDescripcion(c.getPeriodicidad() != null ? c.getPeriodicidad().getDescripcion() : null)
                .estado(c.getEstado())
                .requiereAsignacion(c.isRequiereAsignacion())
                .oficinaResponsableId(c.getOficinaResponsable() != null ? c.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(c.getOficinaResponsable() != null ? c.getOficinaResponsable().getNombre() : null)
                .build();
    }
}
