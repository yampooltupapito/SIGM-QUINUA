package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVisualPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoElementoPlano;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de consulta de alta eficiencia para visor y modal de planos interactivos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultaPlanoMercadoService {

    private final PlanoMercadoRepository planoRepository;
    private final VersionPlanoMercadoRepository versionRepository;
    private final ElementoPlanoMercadoRepository elementoRepository;
    private final PuestoComercialRepository puestoRepository;
    private final EstadoVisualPuestoService estadoVisualService;
    private final VersionPlanoMercadoService versionService;

    /**
     * Obtiene la estructura completa del plano para el visor o editor interactivo,
     * inyectando estados de ocupación dinámicos calculados en tiempo real.
     */
    @Transactional(readOnly = true)
    public PlanoMercadoDetailResponse obtenerPlanoDetalleParaVisor(UUID mercadoId, UUID versionIdOpcional) {
        PlanoMercado plano = planoRepository.findActivoPorMercadoConDetalle(mercadoId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró plano configurado para el mercado con ID: " + mercadoId));

        VersionPlanoMercado version = null;
        if (versionIdOpcional != null) {
            version = versionRepository.findById(versionIdOpcional)
                    .orElseThrow(() -> new ResourceNotFoundException("Versión no encontrada con ID: " + versionIdOpcional));
        } else if (plano.getVersionActual() != null) {
            version = plano.getVersionActual();
        } else {
            // Si no hay versión actual publicada, tomar la última registrada
            version = versionRepository.findTopByPlanoIdOrderByNumeroVersionDesc(plano.getId())
                    .orElse(null);
        }

        List<ElementoPlanoResponse> elementosResponse = new ArrayList<>();
        List<UUID> puestoIdsEnPlano = new ArrayList<>();

        if (version != null) {
            List<ElementoPlanoMercado> elementos = elementoRepository.findByVersionPlanoIdOrderByOrdenCapaAscCoordenadaYAscCoordenadaXAsc(version.getId());

            // Recolectar IDs de puestos para consulta eficiente en lote
            Set<UUID> puestoIds = elementos.stream()
                    .filter(e -> e.getTipoElemento() == TipoElementoPlano.PUESTO && e.getReferenciaEntidadId() != null)
                    .map(ElementoPlanoMercado::getReferenciaEntidadId)
                    .collect(Collectors.toSet());

            puestoIdsEnPlano.addAll(puestoIds);

            // Cargar puestos en mapa por ID
            Map<UUID, PuestoComercial> mapaPuestos = puestoRepository.findAllById(puestoIds).stream()
                    .collect(Collectors.toMap(PuestoComercial::getId, p -> p));

            for (ElementoPlanoMercado elem : elementos) {
                ElementoPlanoResponse.ElementoPlanoResponseBuilder eb = ElementoPlanoResponse.builder()
                        .id(elem.getId())
                        .versionPlanoId(elem.getVersionPlano().getId())
                        .tipoElemento(elem.getTipoElemento())
                        .tipoElementoLabel(elem.getTipoElemento().getLabel())
                        .referenciaEntidadId(elem.getReferenciaEntidadId())
                        .codigoVisual(elem.getCodigoVisual())
                        .etiqueta(elem.getEtiqueta())
                        .coordenadaX(elem.getCoordenadaX())
                        .coordenadaY(elem.getCoordenadaY())
                        .ancho(elem.getAncho())
                        .alto(elem.getAlto())
                        .rotacion(elem.getRotacion())
                        .ordenCapa(elem.getOrdenCapa())
                        .estiloConfigurado(elem.getEstiloConfigurado());

                if (elem.getTipoElemento() == TipoElementoPlano.PUESTO && elem.getReferenciaEntidadId() != null) {
                    PuestoComercial puesto = mapaPuestos.get(elem.getReferenciaEntidadId());
                    if (puesto != null) {
                        EstadoVisualPuesto ev = estadoVisualService.calcularEstadoVisual(puesto);
                        eb.estadoVisual(ev)
                                .estadoVisualLabel(ev.getLabel())
                                .colorHex(ev.getColorHex())
                                .puestoNumero(puesto.getNumero())
                                .sectorNombre(puesto.getSector() != null ? puesto.getSector().getNombre() : null)
                                .pabellonNombre(puesto.getPabellon() != null ? puesto.getPabellon().getNombre() : null)
                                .ocupado(ev == EstadoVisualPuesto.OCUPADO);
                    } else {
                        eb.estadoVisual(EstadoVisualPuesto.INACTIVO)
                                .estadoVisualLabel("Puesto no encontrado")
                                .colorHex(EstadoVisualPuesto.INACTIVO.getColorHex());
                    }
                }

                elementosResponse.add(eb.build());
            }
        }

        // Estadísticas
        EstadisticasPlanoResponse stats = estadoVisualService.calcularEstadisticas(
                plano.getMercado().getId(),
                plano.getMercado().getNombre(),
                plano.getId(),
                version != null ? version.getNumeroVersion() : null,
                version != null ? version.getEstado().name() : null,
                puestoIdsEnPlano
        );

        List<VersionPlanoResponse> historial = versionService.listarVersiones(plano.getId());

        return PlanoMercadoDetailResponse.builder()
                .id(plano.getId())
                .mercadoId(plano.getMercado().getId())
                .mercadoNombre(plano.getMercado().getNombre())
                .mercadoCodigo(plano.getMercado().getCodigo())
                .codigo(plano.getCodigo())
                .nombre(plano.getNombre())
                .descripcion(plano.getDescripcion())
                .anchoBase(plano.getAnchoBase())
                .altoBase(plano.getAltoBase())
                .unidadReferencia(plano.getUnidadReferencia())
                .estado(plano.getEstado())
                .versionSeleccionada(version != null ? versionService.obtenerPorId(version.getId()) : null)
                .elementos(elementosResponse)
                .historialVersiones(historial)
                .estadisticas(stats)
                .createdAt(plano.getCreatedAt())
                .updatedAt(plano.getUpdatedAt())
                .build();
    }

    /**
     * Obtiene el detalle modal de un puesto para interacción por clic o búsqueda.
     */
    @Transactional(readOnly = true)
    public PuestoPlanoResponse obtenerDetallePuestoParaModal(UUID puestoId, Authentication auth) {
        return estadoVisualService.obtenerDetallePuestoParaPlano(puestoId, auth);
    }
}
