package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.VersionPlanoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVersionPlano;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoElementoPlano;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio encargado del ciclo de vida y control de versiones de planos de mercados.
 */
@Slf4j
@Service
public class VersionPlanoMercadoService {

    private final VersionPlanoMercadoRepository versionRepository;
    private final PlanoMercadoRepository planoRepository;
    private final ElementoPlanoMercadoRepository elementoRepository;
    private final AuditoriaPlanoMercadoRepository auditoriaRepository;

    public VersionPlanoMercadoService(
            VersionPlanoMercadoRepository versionRepository,
            PlanoMercadoRepository planoRepository,
            ElementoPlanoMercadoRepository elementoRepository,
            AuditoriaPlanoMercadoRepository auditoriaRepository) {
        this.versionRepository = versionRepository;
        this.planoRepository = planoRepository;
        this.elementoRepository = elementoRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    /**
     * Crea un nuevo borrador independiente incrementando el número de versión.
     */
    @Transactional
    public VersionPlanoResponse crearNuevoBorrador(UUID planoId, String descripcion, String username, UUID usuarioId) {
        PlanoMercado plano = planoRepository.findById(planoId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano no encontrado con ID: " + planoId));

        Integer ultimoNumero = versionRepository.findTopByPlanoIdOrderByNumeroVersionDesc(planoId)
                .map(VersionPlanoMercado::getNumeroVersion)
                .orElse(0);

        int nuevoNumero = ultimoNumero + 1;

        VersionPlanoMercado nuevaVersion = VersionPlanoMercado.builder()
                .plano(plano)
                .numeroVersion(nuevoNumero)
                .estado(EstadoVersionPlano.BORRADOR)
                .descripcionCambios(descripcion != null ? descripcion.trim() : "Borrador de versión " + nuevoNumero)
                .usuarioCreadorId(usuarioId)
                .usuarioCreadorNombre(username)
                .build();

        nuevaVersion = versionRepository.save(nuevaVersion);

        auditoriaRepository.save(AuditoriaPlanoMercado.builder()
                .planoId(plano.getId())
                .versionPlanoId(nuevaVersion.getId())
                .tipoEvento("CREACION_BORRADOR")
                .descripcion("Creación de nuevo borrador versión " + nuevoNumero + ": " + nuevaVersion.getDescripcionCambios())
                .usuarioId(usuarioId)
                .username(username)
                .build());

        return mapearAResponse(nuevaVersion);
    }

    /**
     * Restaura una versión histórica o publicada anterior clonándola en un nuevo borrador editable.
     * Nunca sobrescribe la versión histórica original, garantizando inmutabilidad.
     */
    @Transactional
    public VersionPlanoResponse restaurarComoBorrador(UUID planoId, UUID versionOrigenId, String username, UUID usuarioId) {
        PlanoMercado plano = planoRepository.findById(planoId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano no encontrado con ID: " + planoId));

        VersionPlanoMercado versionOrigen = versionRepository.findByIdConElementos(versionOrigenId)
                .orElseThrow(() -> new ResourceNotFoundException("Versión de origen no encontrada con ID: " + versionOrigenId));

        if (!versionOrigen.getPlano().getId().equals(planoId)) {
            throw new BusinessException("La versión no pertenece al plano especificado.");
        }

        Integer ultimoNumero = versionRepository.findTopByPlanoIdOrderByNumeroVersionDesc(planoId)
                .map(VersionPlanoMercado::getNumeroVersion)
                .orElse(0);

        int nuevoNumero = ultimoNumero + 1;

        VersionPlanoMercado nuevaVersion = VersionPlanoMercado.builder()
                .plano(plano)
                .numeroVersion(nuevoNumero)
                .estado(EstadoVersionPlano.BORRADOR)
                .descripcionCambios("Restaurada a partir de versión " + versionOrigen.getNumeroVersion() + " (" + versionOrigen.getEstado() + ")")
                .usuarioCreadorId(usuarioId)
                .usuarioCreadorNombre(username)
                .imagenReferenciaUrl(versionOrigen.getImagenReferenciaUrl())
                .imagenEscala(versionOrigen.getImagenEscala())
                .imagenPosX(versionOrigen.getImagenPosX())
                .imagenPosY(versionOrigen.getImagenPosY())
                .imagenOpacidad(versionOrigen.getImagenOpacidad())
                .imagenVisible(versionOrigen.getImagenVisible())
                .imagenBloqueada(versionOrigen.getImagenBloqueada())
                .build();

        nuevaVersion = versionRepository.save(nuevaVersion);

        // Clonar elementos gráficos de la versión de origen
        List<ElementoPlanoMercado> elementosOrigen = elementoRepository.findByVersionPlanoIdOrderByOrdenCapaAscCoordenadaYAscCoordenadaXAsc(versionOrigenId);
        List<ElementoPlanoMercado> elementosClonados = new ArrayList<>();
        for (ElementoPlanoMercado elem : elementosOrigen) {
            ElementoPlanoMercado clon = ElementoPlanoMercado.builder()
                    .versionPlano(nuevaVersion)
                    .tipoElemento(elem.getTipoElemento())
                    .referenciaEntidadId(elem.getReferenciaEntidadId())
                    .codigoVisual(elem.getCodigoVisual())
                    .etiqueta(elem.getEtiqueta())
                    .coordenadaX(elem.getCoordenadaX())
                    .coordenadaY(elem.getCoordenadaY())
                    .ancho(elem.getAncho())
                    .alto(elem.getAlto())
                    .rotacion(elem.getRotacion())
                    .ordenCapa(elem.getOrdenCapa())
                    .estiloConfigurado(elem.getEstiloConfigurado())
                    .build();
            elementosClonados.add(clon);
        }
        if (!elementosClonados.isEmpty()) {
            elementoRepository.saveAll(elementosClonados);
        }

        auditoriaRepository.save(AuditoriaPlanoMercado.builder()
                .planoId(plano.getId())
                .versionPlanoId(nuevaVersion.getId())
                .tipoEvento("RESTAURACION_VERSION")
                .descripcion("Restauración de versión " + versionOrigen.getNumeroVersion() + " como nuevo borrador v" + nuevoNumero)
                .usuarioId(usuarioId)
                .username(username)
                .build());

        return mapearAResponse(nuevaVersion);
    }

    /**
     * Lista todas las versiones de un plano ordenadas por número descendente.
     */
    @Transactional(readOnly = true)
    public List<VersionPlanoResponse> listarVersiones(UUID planoId) {
        return versionRepository.findByPlanoIdOrderByNumeroVersionDesc(planoId).stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una versión específica por ID.
     */
    @Transactional(readOnly = true)
    public VersionPlanoResponse obtenerPorId(UUID versionId) {
        VersionPlanoMercado v = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Versión de plano no encontrada con ID: " + versionId));
        return mapearAResponse(v);
    }

    /**
     * Actualiza la configuración de la imagen de referencia de fondo.
     */
    @Transactional
    public VersionPlanoResponse actualizarImagenReferencia(UUID versionId, String url, BigDecimal escala, BigDecimal posX, BigDecimal posY, BigDecimal opacidad, Boolean visible, Boolean bloqueada, String username, UUID usuarioId) {
        VersionPlanoMercado v = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Versión no encontrada: " + versionId));

        if (v.getEstado() == EstadoVersionPlano.PUBLICADA || v.getEstado() == EstadoVersionPlano.ARCHIVADA) {
            throw new BusinessException("No se puede modificar una versión ya publicada o archivada.");
        }

        v.setImagenReferenciaUrl(url);
        if (escala != null) v.setImagenEscala(escala);
        if (posX != null) v.setImagenPosX(posX);
        if (posY != null) v.setImagenPosY(posY);
        if (opacidad != null) v.setImagenOpacidad(opacidad);
        if (visible != null) v.setImagenVisible(visible);
        if (bloqueada != null) v.setImagenBloqueada(bloqueada);

        v = versionRepository.save(v);

        auditoriaRepository.save(AuditoriaPlanoMercado.builder()
                .planoId(v.getPlano().getId())
                .versionPlanoId(v.getId())
                .tipoEvento("IMAGEN_REFERENCIA_ACTUALIZADA")
                .descripcion("Actualización de imagen de referencia de fondo para versión " + v.getNumeroVersion())
                .usuarioId(usuarioId)
                .username(username)
                .build());

        return mapearAResponse(v);
    }

    private VersionPlanoResponse mapearAResponse(VersionPlanoMercado v) {
        int totalElem = (int) elementoRepository.countByVersionPlanoId(v.getId());
        int totalPuestos = (int) elementoRepository.countByVersionPlanoIdAndTipoElemento(v.getId(), TipoElementoPlano.PUESTO);

        return VersionPlanoResponse.builder()
                .id(v.getId())
                .planoId(v.getPlano() != null ? v.getPlano().getId() : null)
                .numeroVersion(v.getNumeroVersion())
                .estado(v.getEstado())
                .estadoLabel(v.getEstado().getLabel())
                .descripcionCambios(v.getDescripcionCambios())
                .usuarioCreadorId(v.getUsuarioCreadorId())
                .usuarioCreadorNombre(v.getUsuarioCreadorNombre())
                .usuarioPublicadorId(v.getUsuarioPublicadorId())
                .usuarioPublicadorNombre(v.getUsuarioPublicadorNombre())
                .fechaCreacion(v.getFechaCreacion())
                .fechaPublicacion(v.getFechaPublicacion())
                .hashContenido(v.getHashContenido())
                .totalElementos(totalElem)
                .totalPuestos(totalPuestos)
                .imagenReferenciaUrl(v.getImagenReferenciaUrl())
                .imagenEscala(v.getImagenEscala())
                .imagenPosX(v.getImagenPosX())
                .imagenPosY(v.getImagenPosY())
                .imagenOpacidad(v.getImagenOpacidad())
                .imagenVisible(v.getImagenVisible())
                .imagenBloqueada(v.getImagenBloqueada())
                .version(v.getVersion())
                .build();
    }
}
