package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.TipificacionInfraccionRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.TipificacionInfraccionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.TipificacionInfraccion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.TipificacionInfraccionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TipificacionInfraccionService {

    private final TipificacionInfraccionRepository tipificacionRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public TipificacionInfraccionService(
            TipificacionInfraccionRepository tipificacionRepository,
            AuditoriaAdministrativaService auditoriaService) {
        this.tipificacionRepository = tipificacionRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<TipificacionInfraccionResponse> listarVigentes() {
        LocalDate hoy = LocalDate.now();
        return tipificacionRepository.findVigentes(hoy).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TipificacionInfraccionResponse obtenerPorId(UUID id) {
        TipificacionInfraccion t = tipificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipificación no encontrada: " + id));
        return toResponse(t);
    }

    @Transactional(readOnly = true)
    public Page<TipificacionInfraccionResponse> buscarPaginado(String estado, String q, Pageable pageable) {
        String qNorm = (q != null && !q.trim().isEmpty()) ? "%" + q.trim().toLowerCase() + "%" : null;
        return tipificacionRepository.buscarPaginado(estado, qNorm, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public TipificacionInfraccionResponse crear(TipificacionInfraccionRequest request, String username) {
        if (tipificacionRepository.findByCodigo(request.getCodigo().trim()).isPresent()) {
            throw new BusinessException("Ya existe una tipificación con el código: " + request.getCodigo());
        }

        TipificacionInfraccion t = TipificacionInfraccion.builder()
                .codigo(request.getCodigo().trim().toUpperCase())
                .descripcion(request.getDescripcion().trim())
                .normaReferencia(request.getNormaReferencia().trim())
                .articulo(request.getArticulo() != null ? request.getArticulo().trim() : null)
                .gravedadBase(request.getGravedadBase())
                .autoridadCompetente(request.getAutoridadCompetente().trim())
                .fechaInicioVigencia(request.getFechaInicioVigencia())
                .fechaFinVigencia(request.getFechaFinVigencia())
                .versionNormativa(request.getVersionNormativa() != null ? request.getVersionNormativa() : 1)
                .estado(request.getEstado() != null ? request.getEstado() : "ACTIVO")
                .documentoFuenteId(request.getDocumentoFuenteId())
                .build();

        TipificacionInfraccion guardada = tipificacionRepository.save(t);

        auditoriaService.registrar("CREAR_TIPIFICACION_INFRACCION", "mercados_tipificaciones_infraccion", guardada.getId(),
                "EXITO", "Tipificación creada: " + guardada.getCodigo());

        return toResponse(guardada);
    }

    @Transactional
    public TipificacionInfraccionResponse versionar(UUID id, TipificacionInfraccionRequest request, String username) {
        TipificacionInfraccion actual = tipificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipificación no encontrada: " + id));

        // Cerrar vigencia de la versión anterior
        actual.setFechaFinVigencia(request.getFechaInicioVigencia().minusDays(1));
        actual.setEstado("HISTORICO");
        tipificacionRepository.save(actual);

        // Crear nueva versión con versionNormativa incrementada
        int nuevaVersion = (actual.getVersionNormativa() != null ? actual.getVersionNormativa() : 1) + 1;
        TipificacionInfraccion nueva = TipificacionInfraccion.builder()
                .codigo(actual.getCodigo())
                .descripcion(request.getDescripcion().trim())
                .normaReferencia(request.getNormaReferencia().trim())
                .articulo(request.getArticulo() != null ? request.getArticulo().trim() : null)
                .gravedadBase(request.getGravedadBase())
                .autoridadCompetente(request.getAutoridadCompetente().trim())
                .fechaInicioVigencia(request.getFechaInicioVigencia())
                .fechaFinVigencia(request.getFechaFinVigencia())
                .versionNormativa(nuevaVersion)
                .estado("ACTIVO")
                .documentoFuenteId(request.getDocumentoFuenteId())
                .build();

        TipificacionInfraccion guardada = tipificacionRepository.save(nueva);

        auditoriaService.registrar("VERSIONAR_TIPIFICACION_INFRACCION", "mercados_tipificaciones_infraccion", guardada.getId(),
                "EXITO", "Nueva versión " + nuevaVersion + " creada para tipificación: " + guardada.getCodigo());

        return toResponse(guardada);
    }

    public TipificacionInfraccionResponse toResponse(TipificacionInfraccion t) {
        LocalDate hoy = LocalDate.now();
        boolean esVigente = "ACTIVO".equalsIgnoreCase(t.getEstado()) &&
                !t.getFechaInicioVigencia().isAfter(hoy) &&
                (t.getFechaFinVigencia() == null || !t.getFechaFinVigencia().isBefore(hoy));

        return TipificacionInfraccionResponse.builder()
                .id(t.getId())
                .codigo(t.getCodigo())
                .descripcion(t.getDescripcion())
                .normaReferencia(t.getNormaReferencia())
                .articulo(t.getArticulo())
                .gravedadBase(t.getGravedadBase())
                .gravedadBaseLabel(t.getGravedadBase() != null ? t.getGravedadBase().name() : null)
                .autoridadCompetente(t.getAutoridadCompetente())
                .fechaInicioVigencia(t.getFechaInicioVigencia())
                .fechaFinVigencia(t.getFechaFinVigencia())
                .versionNormativa(t.getVersionNormativa())
                .estado(t.getEstado())
                .documentoFuenteId(t.getDocumentoFuenteId())
                .esVigente(esVigente)
                .build();
    }
}
