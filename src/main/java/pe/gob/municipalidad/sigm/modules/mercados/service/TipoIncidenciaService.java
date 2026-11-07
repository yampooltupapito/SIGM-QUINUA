package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.TipoIncidenciaResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.TipoIncidenciaMercado;
import pe.gob.municipalidad.sigm.modules.mercados.repository.TipoIncidenciaMercadoRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TipoIncidenciaService {

    private final TipoIncidenciaMercadoRepository tipoRepository;

    public TipoIncidenciaService(TipoIncidenciaMercadoRepository tipoRepository) {
        this.tipoRepository = tipoRepository;
    }

    @Transactional(readOnly = true)
    public List<TipoIncidenciaResponse> listarActivos() {
        return tipoRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TipoIncidenciaResponse obtenerPorId(UUID id) {
        TipoIncidenciaMercado t = tipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de incidencia no encontrado: " + id));
        return toResponse(t);
    }

    public TipoIncidenciaResponse toResponse(TipoIncidenciaMercado t) {
        return TipoIncidenciaResponse.builder()
                .id(t.getId())
                .codigo(t.getCodigo())
                .nombre(t.getNombre())
                .descripcion(t.getDescripcion())
                .prioridadSugerida(t.getPrioridadSugerida())
                .requierePuesto(t.getRequierePuesto())
                .activo(t.getActivo())
                .build();
    }
}
