package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ElementoPlanoRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.GuardarPlanoRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.VersionPlanoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVersionPlano;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoElementoPlano;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * Servicio encargado de la validación técnica, guardado y edición de lienzos y elementos del plano.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EditorPlanoMercadoService {

    private final PlanoMercadoRepository planoRepository;
    private final VersionPlanoMercadoRepository versionRepository;
    private final ElementoPlanoMercadoRepository elementoRepository;
    private final PuestoComercialRepository puestoRepository;
    private final AuditoriaPlanoMercadoRepository auditoriaRepository;
    private final VersionPlanoMercadoService versionService;

    /**
     * Guarda los cambios de diseño en una versión borrador, validando geometrías,
     * consistencia con el mercado y previniendo puestos duplicados.
     */
    @Transactional
    public VersionPlanoResponse guardarBorrador(UUID planoId, GuardarPlanoRequest request, String username, UUID usuarioId) {
        log.info("Guardando borrador de plano ID: {}, versión: {}", planoId, request.getVersionId());

        PlanoMercado plano = planoRepository.findById(planoId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano no encontrado con ID: " + planoId));

        VersionPlanoMercado version = versionRepository.findById(request.getVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("Versión no encontrada con ID: " + request.getVersionId()));

        if (!version.getPlano().getId().equals(planoId)) {
            throw new BusinessException("La versión no pertenece al plano especificado.");
        }

        if (version.getEstado() == EstadoVersionPlano.PUBLICADA || version.getEstado() == EstadoVersionPlano.ARCHIVADA) {
            throw new BusinessException("No se puede editar una versión ya publicada o archivada.");
        }

        // Control de concurrencia optimista
        if (request.getVersion() != null && version.getVersion() != null && !request.getVersion().equals(version.getVersion())) {
            throw new BusinessException("Conflicto de concurrencia: Otro usuario modificó esta versión mientras la editabas. Por favor recarga el editor.");
        }

        // Validar elementos y pertenencia de puestos al mercado
        Set<UUID> puestosAsignadosEnPlano = new HashSet<>();
        List<ElementoPlanoMercado> nuevosElementos = new ArrayList<>();

        if (request.getElementos() != null) {
            for (ElementoPlanoRequest elemReq : request.getElementos()) {
                if (elemReq.getAncho() == null || elemReq.getAncho().compareTo(BigDecimal.ZERO) <= 0 ||
                    elemReq.getAlto() == null || elemReq.getAlto().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException("Las dimensiones de cada elemento deben ser mayores a cero.");
                }

                if (elemReq.getTipoElemento() == TipoElementoPlano.PUESTO) {
                    if (elemReq.getReferenciaEntidadId() == null) {
                        throw new BusinessException("Todo elemento de tipo PUESTO debe estar vinculado a un puesto comercial real.");
                    }

                    // Validar existencia y que pertenezca al mismo mercado
                    PuestoComercial puesto = puestoRepository.findById(elemReq.getReferenciaEntidadId())
                            .orElseThrow(() -> new BusinessException("El puesto vinculado no existe en el sistema: " + elemReq.getReferenciaEntidadId()));

                    if (puesto.getMercado() == null || !puesto.getMercado().getId().equals(plano.getMercado().getId())) {
                        throw new BusinessException("El puesto '" + puesto.getCodigo() + "' pertenece a otro mercado y no puede vincularse a este plano.");
                    }

                    // Validar duplicados en la misma versión
                    if (!puestosAsignadosEnPlano.add(elemReq.getReferenciaEntidadId())) {
                        throw new BusinessException("El puesto '" + puesto.getCodigo() + "' aparece duplicado en la distribución.");
                    }
                }

                ElementoPlanoMercado elemento = ElementoPlanoMercado.builder()
                        .versionPlano(version)
                        .tipoElemento(elemReq.getTipoElemento())
                        .referenciaEntidadId(elemReq.getReferenciaEntidadId())
                        .codigoVisual(elemReq.getCodigoVisual() != null ? elemReq.getCodigoVisual().trim() : "ELEM")
                        .etiqueta(elemReq.getEtiqueta())
                        .coordenadaX(elemReq.getCoordenadaX() != null ? elemReq.getCoordenadaX() : BigDecimal.ZERO)
                        .coordenadaY(elemReq.getCoordenadaY() != null ? elemReq.getCoordenadaY() : BigDecimal.ZERO)
                        .ancho(elemReq.getAncho())
                        .alto(elemReq.getAlto())
                        .rotacion(elemReq.getRotacion() != null ? elemReq.getRotacion() : BigDecimal.ZERO)
                        .ordenCapa(elemReq.getOrdenCapa() != null ? elemReq.getOrdenCapa() : 0)
                        .estiloConfigurado(elemReq.getEstiloConfigurado())
                        .build();

                nuevosElementos.add(elemento);
            }
        }

        // Reemplazar elementos existentes
        elementoRepository.deleteByVersionPlanoId(version.getId());
        elementoRepository.saveAll(nuevosElementos);

        // Actualizar datos de la versión
        if (request.getDescripcionCambios() != null) {
            version.setDescripcionCambios(request.getDescripcionCambios().trim());
        }

        if (request.getImagenReferenciaUrl() != null) {
            version.setImagenReferenciaUrl(request.getImagenReferenciaUrl());
        }
        if (request.getImagenEscala() != null) version.setImagenEscala(request.getImagenEscala());
        if (request.getImagenPosX() != null) version.setImagenPosX(request.getImagenPosX());
        if (request.getImagenPosY() != null) version.setImagenPosY(request.getImagenPosY());
        if (request.getImagenOpacidad() != null) version.setImagenOpacidad(request.getImagenOpacidad());
        if (request.getImagenVisible() != null) version.setImagenVisible(request.getImagenVisible());
        if (request.getImagenBloqueada() != null) version.setImagenBloqueada(request.getImagenBloqueada());

        // Calcular hash SHA-256 de integridad de los elementos guardados
        version.setHashContenido(calcularHash(nuevosElementos));

        version = versionRepository.save(version);

        auditoriaRepository.save(AuditoriaPlanoMercado.builder()
                .planoId(plano.getId())
                .versionPlanoId(version.getId())
                .tipoEvento("GUARDADO_DISTRIBUCION")
                .descripcion("Guardado de borrador v" + version.getNumeroVersion() + " con " + nuevosElementos.size() + " elementos (" + puestosAsignadosEnPlano.size() + " puestos)")
                .usuarioId(usuarioId)
                .username(username)
                .build());

        return versionService.obtenerPorId(version.getId());
    }

    private String calcularHash(List<ElementoPlanoMercado> elementos) {
        try {
            StringBuilder sb = new StringBuilder();
            for (ElementoPlanoMercado e : elementos) {
                sb.append(e.getTipoElemento()).append(";")
                        .append(e.getCodigoVisual()).append(";")
                        .append(e.getReferenciaEntidadId()).append(";")
                        .append(e.getCoordenadaX()).append(";")
                        .append(e.getCoordenadaY()).append(";")
                        .append(e.getAncho()).append(";")
                        .append(e.getAlto()).append(";")
                        .append(e.getRotacion()).append("|");
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            log.warn("No se pudo calcular hash SHA-256 de distribución", e);
            return null;
        }
    }
}
