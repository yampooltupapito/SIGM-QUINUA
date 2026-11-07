package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ActividadComercianteRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ActividadComercianteResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ActividadComercial;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ActividadComercialRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.ComercianteRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio encargado de gestionar las actividades comerciales autorizadas de los comerciantes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComercianteActividadService {

    private final ComercianteRepository comercianteRepository;
    private final ActividadComercialRepository actividadRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    @Transactional(readOnly = true)
    public List<ActividadComercianteResponse> listarActividadesPorComerciante(UUID comercianteId) {
        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + comercianteId));

        return comerciante.getActividades().stream()
                .map(a -> ActividadComercianteResponse.builder()
                        .id(a.getId())
                        .codigo(a.getCodigo())
                        .nombre(a.getNombre())
                        .categoria(a.getCategoria())
                        .descripcion(a.getDescripcion())
                        .principal(true)
                        .estado(a.getEstado())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public ActividadComercianteResponse asociarActividad(UUID comercianteId, ActividadComercianteRequest request) {
        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + comercianteId));

        ActividadComercial actividad = actividadRepository.findById(request.getActividadId())
                .orElseThrow(() -> new ResourceNotFoundException("Actividad comercial no encontrada con ID: " + request.getActividadId()));

        comerciante.getActividades().add(actividad);
        comercianteRepository.save(comerciante);

        auditoriaService.registrar("ASOCIAR_ACTIVIDAD_COMERCIANTE", "mercados_comerciantes", comercianteId.toString(), "EXITO",
                "Asociada actividad " + actividad.getCodigo() + " - " + actividad.getNombre() + " al comerciante " + comerciante.getCodigoComerciante());

        return ActividadComercianteResponse.builder()
                .id(actividad.getId())
                .codigo(actividad.getCodigo())
                .nombre(actividad.getNombre())
                .categoria(actividad.getCategoria())
                .descripcion(actividad.getDescripcion())
                .principal(request.isPrincipal())
                .estado(actividad.getEstado())
                .build();
    }

    @Transactional
    public void desasociarActividad(UUID comercianteId, Long actividadId) {
        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + comercianteId));

        comerciante.getActividades().removeIf(a -> a.getId().equals(actividadId));
        comercianteRepository.save(comerciante);

        auditoriaService.registrar("DESASOCIAR_ACTIVIDAD_COMERCIANTE", "mercados_comerciantes", comercianteId.toString(), "EXITO",
                "Desasociada actividad ID " + actividadId + " del comerciante " + comerciante.getCodigoComerciante());
    }
}
