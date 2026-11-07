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
import pe.gob.municipalidad.sigm.modules.mercados.dto.ItemVerificacionDto;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PlantillaInspeccionRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PlantillaInspeccionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ItemVerificacionPlantilla;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PlantillaInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoInspeccionMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ItemVerificacionPlantillaRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.PlantillaInspeccionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PlantillaInspeccionService {

    private final PlantillaInspeccionRepository plantillaRepository;
    private final ItemVerificacionPlantillaRepository itemRepository;
    private final OficinaRepository oficinaRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public PlantillaInspeccionService(PlantillaInspeccionRepository plantillaRepository,
                                      ItemVerificacionPlantillaRepository itemRepository,
                                      OficinaRepository oficinaRepository,
                                      AuditoriaAdministrativaService auditoriaService) {
        this.plantillaRepository = plantillaRepository;
        this.itemRepository = itemRepository;
        this.oficinaRepository = oficinaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public PlantillaInspeccionResponse crear(PlantillaInspeccionRequest request, String username) {
        if (plantillaRepository.existsByCodigo(request.getCodigo().trim().toUpperCase())) {
            throw new BusinessException("Ya existe una plantilla de inspección con el código: " + request.getCodigo());
        }

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId()).orElse(null);
        }

        PlantillaInspeccion entity = PlantillaInspeccion.builder()
                .codigo(request.getCodigo().trim().toUpperCase())
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .tipoInspeccion(request.getTipoInspeccion())
                .version(1)
                .estado("ACTIVA")
                .oficinaResponsable(oficina)
                .fechaAprobacion(request.getFechaAprobacion())
                .sustentoNormativo(request.getSustentoNormativo())
                .items(new ArrayList<>())
                .build();

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (PlantillaInspeccionRequest.ItemDto itemDto : request.getItems()) {
                ItemVerificacionPlantilla item = ItemVerificacionPlantilla.builder()
                        .plantilla(entity)
                        .codigo(itemDto.getCodigo().trim().toUpperCase())
                        .categoria(itemDto.getCategoria().trim().toUpperCase())
                        .descripcion(itemDto.getDescripcion().trim())
                        .orden(itemDto.getOrden())
                        .obligatorio(itemDto.isObligatorio())
                        .referenciaNormativa(itemDto.getReferenciaNormativa())
                        .estado("ACTIVO")
                        .build();
                entity.getItems().add(item);
            }
        }

        PlantillaInspeccion guardada = plantillaRepository.save(entity);

        auditoriaService.registrar("CREAR_PLANTILLA_INSPECCION", "mercados_plantillas_inspeccion", guardada.getId(),
                "EXITO", "Plantilla de inspección creada: " + guardada.getCodigo());

        return toResponse(guardada);
    }

    @Transactional
    public PlantillaInspeccionResponse versionarPlantilla(UUID id, PlantillaInspeccionRequest request, String username) {
        PlantillaInspeccion actual = plantillaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada: " + id));

        // Marcar la versión actual como HISTORICA
        actual.setEstado("HISTORICA");
        plantillaRepository.save(actual);

        // Crear nueva versión con incremento de versión
        String nuevoCodigo = actual.getCodigo() + "-V" + (actual.getVersion() + 1);

        PlantillaInspeccion nueva = PlantillaInspeccion.builder()
                .codigo(nuevoCodigo)
                .nombre(request.getNombre() != null ? request.getNombre().trim() : actual.getNombre())
                .descripcion(request.getDescripcion() != null ? request.getDescripcion() : actual.getDescripcion())
                .tipoInspeccion(request.getTipoInspeccion() != null ? request.getTipoInspeccion() : actual.getTipoInspeccion())
                .version(actual.getVersion() + 1)
                .estado("ACTIVA")
                .oficinaResponsable(actual.getOficinaResponsable())
                .fechaAprobacion(request.getFechaAprobacion())
                .sustentoNormativo(request.getSustentoNormativo() != null ? request.getSustentoNormativo() : actual.getSustentoNormativo())
                .items(new ArrayList<>())
                .build();

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (PlantillaInspeccionRequest.ItemDto itemDto : request.getItems()) {
                ItemVerificacionPlantilla item = ItemVerificacionPlantilla.builder()
                        .plantilla(nueva)
                        .codigo(itemDto.getCodigo().trim().toUpperCase())
                        .categoria(itemDto.getCategoria().trim().toUpperCase())
                        .descripcion(itemDto.getDescripcion().trim())
                        .orden(itemDto.getOrden())
                        .obligatorio(itemDto.isObligatorio())
                        .referenciaNormativa(itemDto.getReferenciaNormativa())
                        .estado("ACTIVO")
                        .build();
                nueva.getItems().add(item);
            }
        }

        PlantillaInspeccion guardada = plantillaRepository.save(nueva);

        auditoriaService.registrar("VERSIONAR_PLANTILLA_INSPECCION", "mercados_plantillas_inspeccion", guardada.getId(),
                "EXITO", "Nueva versión generada: " + guardada.getCodigo() + " (v" + guardada.getVersion() + ")");

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public PlantillaInspeccionResponse obtenerPorId(UUID id) {
        return plantillaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<PlantillaInspeccionResponse> listarActivas() {
        return plantillaRepository.findByEstado("ACTIVA").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlantillaInspeccionResponse> listarPorTipo(TipoInspeccionMercado tipo) {
        return plantillaRepository.findByTipoInspeccionAndEstado(tipo, "ACTIVA").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PlantillaInspeccionResponse> buscarPaginado(String estado, TipoInspeccionMercado tipo, String q, Pageable pageable) {
        String qNorm = (q != null && !q.trim().isEmpty()) ? "%" + q.trim().toLowerCase() + "%" : null;
        return plantillaRepository.buscarPaginado(estado, tipo, qNorm, pageable)
                .map(this::toResponse);
    }

    public PlantillaInspeccionResponse toResponse(PlantillaInspeccion p) {
        List<ItemVerificacionDto> itemDtos = new ArrayList<>();
        if (p.getItems() != null) {
            itemDtos = p.getItems().stream()
                    .map(it -> ItemVerificacionDto.builder()
                            .id(it.getId())
                            .codigo(it.getCodigo())
                            .categoria(it.getCategoria())
                            .descripcion(it.getDescripcion())
                            .orden(it.getOrden())
                            .obligatorio(it.getObligatorio())
                            .referenciaNormativa(it.getReferenciaNormativa())
                            .estado(it.getEstado())
                            .build())
                    .collect(Collectors.toList());
        }

        return PlantillaInspeccionResponse.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .tipoInspeccion(p.getTipoInspeccion())
                .tipoInspeccionDescripcion(p.getTipoInspeccion() != null ? p.getTipoInspeccion().getDescripcion() : null)
                .version(p.getVersion())
                .estado(p.getEstado())
                .oficinaResponsableId(p.getOficinaResponsable() != null ? p.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(p.getOficinaResponsable() != null ? p.getOficinaResponsable().getNombre() : null)
                .fechaAprobacion(p.getFechaAprobacion())
                .sustentoNormativo(p.getSustentoNormativo())
                .items(itemDtos)
                .createdAt(p.getCreatedAt())
                .build();
    }
}
