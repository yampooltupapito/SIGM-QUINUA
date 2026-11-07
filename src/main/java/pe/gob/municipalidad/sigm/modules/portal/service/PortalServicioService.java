package pe.gob.municipalidad.sigm.modules.portal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalServicioDtos;
import pe.gob.municipalidad.sigm.modules.portal.entity.PortalServicio;
import pe.gob.municipalidad.sigm.modules.portal.repository.PortalServicioRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para la gestión y consulta del catálogo de servicios municipales
 * del Portal Ciudadano de SIGM-QUINUA.
 * Garantiza que únicamente se expongan los cuatro servicios municipales autorizados:
 * 1. Mesa de Partes Virtual
 * 2. Mercados Municipales
 * 3. Seguridad Ciudadana y Serenazgo
 * 4. Recaudación Tributaria Municipal
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalServicioService {

    public static final Set<String> CODIGOS_SERVICIOS_OFICIALES = Set.of(
            "SRV_MESA_PARTES",
            "SRV_MERCADOS",
            "SRV_SEGURIDAD",
            "SRV_RENTAS"
    );

    private final PortalServicioRepository servicioRepository;

    @Transactional(readOnly = true)
    public List<PortalServicioDtos.ServicioResponse> listarServiciosPublicos() {
        List<PortalServicioDtos.ServicioResponse> activos = servicioRepository.findByEstadoOrderByOrdenAsc("ACTIVO")
                .stream()
                .filter(s -> CODIGOS_SERVICIOS_OFICIALES.contains(s.getCodigo()))
                .map(this::toResponse)
                .collect(Collectors.toList());

        if (activos.isEmpty()) {
            log.info("Catálogo de servicios en base de datos vacío o en inicialización. Retornando los 4 servicios oficiales por defecto.");
            return obtenerServiciosOficialesFallback();
        }

        return activos;
    }

    @Transactional(readOnly = true)
    public List<PortalServicioDtos.ServicioResponse> listarServiciosAdmin() {
        List<PortalServicioDtos.ServicioResponse> servicios = servicioRepository.findAllByOrderByOrdenAsc()
                .stream()
                .filter(s -> CODIGOS_SERVICIOS_OFICIALES.contains(s.getCodigo()))
                .map(this::toResponse)
                .collect(Collectors.toList());

        if (servicios.isEmpty()) {
            return obtenerServiciosOficialesFallback();
        }

        return servicios;
    }

    @Transactional(readOnly = true)
    public PortalServicioDtos.ServicioResponse obtenerServicioPorId(Long id) {
        PortalServicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio municipal no encontrado con ID: " + id));
        return toResponse(servicio);
    }

    @Transactional
    public PortalServicioDtos.ServicioResponse actualizarServicio(Long id, PortalServicioDtos.ServicioUpdateRequest request) {
        PortalServicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio municipal no encontrado con ID: " + id));

        servicio.setNombre(request.getNombre().trim());
        servicio.setDescripcion(request.getDescripcion().trim());
        servicio.setIcono(request.getIcono().trim());
        if (request.getOrden() != null) {
            servicio.setOrden(request.getOrden());
        }
        if (request.getEstadoDisponibilidad() != null) {
            servicio.setEstadoDisponibilidad(request.getEstadoDisponibilidad().toUpperCase());
        }
        servicio.setUrlAcceso(request.getUrlAcceso() != null ? request.getUrlAcceso().trim() : null);
        if (request.getEstado() != null) {
            servicio.setEstado(request.getEstado().toUpperCase());
        }

        PortalServicio actualizado = servicioRepository.save(servicio);
        log.info("Servicio municipal actualizado con ID: {}", actualizado.getId());
        return toResponse(actualizado);
    }

    public List<PortalServicioDtos.ServicioResponse> obtenerServiciosOficialesFallback() {
        return List.of(
                PortalServicioDtos.ServicioResponse.builder()
                        .id(1L)
                        .codigo("SRV_MESA_PARTES")
                        .nombre("Mesa de Partes Virtual")
                        .descripcion("Presentación digital de solicitudes, trámites TUPA y documentos oficiales con cargo digital y seguimiento en tiempo real.")
                        .icono("file-text")
                        .orden(1)
                        .estadoDisponibilidad("DISPONIBLE")
                        .urlAcceso("/mesa-partes")
                        .estado("ACTIVO")
                        .build(),
                PortalServicioDtos.ServicioResponse.builder()
                        .id(2L)
                        .codigo("SRV_MERCADOS")
                        .nombre("Mercados Municipales")
                        .descripcion("Gestión y control de puestos de abasto, padrón de comerciantes, contratos de concesión y liquidación de sisa comercial.")
                        .icono("store")
                        .orden(2)
                        .estadoDisponibilidad("DISPONIBLE")
                        .urlAcceso("/portal/comerciante/dashboard")
                        .estado("ACTIVO")
                        .build(),
                PortalServicioDtos.ServicioResponse.builder()
                        .id(3L)
                        .codigo("SRV_SEGURIDAD")
                        .nombre("Seguridad Ciudadana y Serenazgo")
                        .descripcion("Central de alertas vecinales de Serenazgo Quinua, reporte directo de emergencias distritales y patrullaje integrado por cuadrantes.")
                        .icono("shield-check")
                        .orden(3)
                        .estadoDisponibilidad("DISPONIBLE")
                        .urlAcceso("/portal/seguridad/reportar")
                        .estado("ACTIVO")
                        .build(),
                PortalServicioDtos.ServicioResponse.builder()
                        .id(4L)
                        .codigo("SRV_RENTAS")
                        .nombre("Recaudación Tributaria Municipal")
                        .descripcion("Consulta en línea de predios, cálculo y pago de impuesto predial y arbitrios municipales, estados de cuenta y fraccionamientos.")
                        .icono("landmark")
                        .orden(4)
                        .estadoDisponibilidad("DISPONIBLE")
                        .urlAcceso("/portal/tributario/dashboard")
                        .estado("ACTIVO")
                        .build()
        );
    }

    private PortalServicioDtos.ServicioResponse toResponse(PortalServicio s) {
        return PortalServicioDtos.ServicioResponse.builder()
                .id(s.getId())
                .codigo(s.getCodigo())
                .nombre(s.getNombre())
                .descripcion(s.getDescripcion())
                .icono(s.getIcono())
                .orden(s.getOrden())
                .estadoDisponibilidad(s.getEstadoDisponibilidad())
                .urlAcceso(s.getUrlAcceso())
                .estado(s.getEstado())
                .build();
    }
}
