package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PublicarPlanoRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.VersionPlanoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVersionPlano;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio encargado de la aprobación formal, publicación oficial y archivo histórico de versiones de planos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PublicacionPlanoMercadoService {

    private final PlanoMercadoRepository planoRepository;
    private final VersionPlanoMercadoRepository versionRepository;
    private final ElementoPlanoMercadoRepository elementoRepository;
    private final AuditoriaPlanoMercadoRepository auditoriaRepository;
    private final VersionPlanoMercadoService versionService;

    /**
     * Publica oficialmente una versión técnica del plano, archivando la versión vigente anterior.
     */
    @Transactional
    public VersionPlanoResponse publicarVersion(UUID planoId, UUID versionId, PublicarPlanoRequest request, String username, UUID usuarioId) {
        log.info("Publicando versión ID: {} para plano ID: {} por usuario: {}", versionId, planoId, username);

        PlanoMercado plano = planoRepository.findById(planoId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano no encontrado con ID: " + planoId));

        VersionPlanoMercado version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Versión no encontrada con ID: " + versionId));

        if (!version.getPlano().getId().equals(planoId)) {
            throw new BusinessException("La versión no pertenece al plano especificado.");
        }

        if (version.getEstado() == EstadoVersionPlano.PUBLICADA) {
            throw new BusinessException("Esta versión ya se encuentra publicada y vigente.");
        }

        if (version.getEstado() == EstadoVersionPlano.ARCHIVADA) {
            throw new BusinessException("Una versión archivada no puede publicarse directamente; debe restaurarse como borrador primero.");
        }

        long totalElementos = elementoRepository.countByVersionPlanoId(version.getId());
        if (totalElementos == 0) {
            throw new BusinessException("No se puede publicar una versión sin elementos gráficos distribuidos.");
        }

        // Archivar versión actualmente publicada (si existe)
        List<VersionPlanoMercado> publicadasAnteriores = versionRepository.findByPlanoIdAndEstadoOrderByNumeroVersionDesc(
                planoId, EstadoVersionPlano.PUBLICADA);

        for (VersionPlanoMercado anterior : publicadasAnteriores) {
            anterior.setEstado(EstadoVersionPlano.ARCHIVADA);
            versionRepository.save(anterior);
            log.info("Versión anterior v{} archivada con éxito.", anterior.getNumeroVersion());
        }

        // Publicar la nueva versión
        version.setEstado(EstadoVersionPlano.PUBLICADA);
        version.setFechaPublicacion(OffsetDateTime.now());
        version.setUsuarioPublicadorId(usuarioId);
        version.setUsuarioPublicadorNombre(username);
        if (request != null && request.getObservaciones() != null && !request.getObservaciones().isBlank()) {
            String desc = (version.getDescripcionCambios() != null ? version.getDescripcionCambios() + " | " : "") +
                          "Publicación: " + request.getObservaciones().trim();
            version.setDescripcionCambios(desc);
        }

        version = versionRepository.save(version);

        // Actualizar referencia en PlanoMercado
        plano.setVersionActual(version);
        planoRepository.save(plano);

        auditoriaRepository.save(AuditoriaPlanoMercado.builder()
                .planoId(plano.getId())
                .versionPlanoId(version.getId())
                .tipoEvento("PUBLICACION_VERSION")
                .descripcion("Publicación oficial de versión " + version.getNumeroVersion() + " como plano vigente del mercado " + plano.getMercado().getNombre())
                .usuarioId(usuarioId)
                .username(username)
                .build());

        return versionService.obtenerPorId(version.getId());
    }
}
