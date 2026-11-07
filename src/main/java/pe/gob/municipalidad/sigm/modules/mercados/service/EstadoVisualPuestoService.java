package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.EstadisticasPlanoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PuestoPlanoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVisualPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Servicio encargado de determinar el estado visual dinámico y enriquecer la información
 * de puestos comerciales a partir de las tablas de PostgreSQL (fuente de verdad).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EstadoVisualPuestoService {

    private final PuestoComercialRepository puestoRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final ContratoOcupacionRepository contratoRepository;
    private final AutorizacionComercialRepository autorizacionRepository;
    private final IncidenciaMercadoRepository incidenciaRepository;
    private final InspeccionMercadoRepository inspeccionRepository;
    private final ObligacionMercadoRepository obligacionRepository;

    /**
     * Calcula el estado visual de un puesto a partir de su estado administrativo y de ocupación vigente.
     */
    @Transactional(readOnly = true)
    public EstadoVisualPuesto calcularEstadoVisual(PuestoComercial puesto) {
        if (puesto == null) {
            return EstadoVisualPuesto.INACTIVO;
        }

        // 1. Si el puesto está inhabilitado, no asignable o con daño físico inhabilitado
        if (puesto.getEstado() == EstadoPuesto.INHABILITADO ||
            puesto.getEstado() == EstadoPuesto.NO_ASIGNABLE ||
            puesto.getEstadoFisico() == EstadoFisicoPuesto.INHABILITADO) {
            return EstadoVisualPuesto.RESTRINGIDO;
        }

        // 2. Si el puesto está en mantenimiento operativo o físico
        if (puesto.getEstado() == EstadoPuesto.EN_MANTENIMIENTO ||
            puesto.getEstadoFisico() == EstadoFisicoPuesto.EN_MANTENIMIENTO) {
            return EstadoVisualPuesto.INACTIVO;
        }

        // 3. Evaluar ocupación vigente en mercados_asignaciones
        Optional<AsignacionPuesto> asignacionOpt = asignacionRepository.findFirstByPuestoIdAndEstado(
                puesto.getId(), EstadoAsignacion.VIGENTE);

        if (asignacionOpt.isPresent()) {
            return EstadoVisualPuesto.OCUPADO;
        }

        // 4. Evaluar si tiene una asignación reservada o en proceso de formalización
        boolean tieneReserva = asignacionRepository.existsByPuestoIdAndEstado(puesto.getId(), EstadoAsignacion.APROBADA)
                || asignacionRepository.existsByPuestoIdAndEstado(puesto.getId(), EstadoAsignacion.EN_EVALUACION)
                || asignacionRepository.existsByPuestoIdAndEstado(puesto.getId(), EstadoAsignacion.SOLICITADA)
                || puesto.getEstado() == EstadoPuesto.RESERVADO;

        if (tieneReserva) {
            return EstadoVisualPuesto.RESERVADO;
        }

        // 5. Si no tiene ocupación vigente y el puesto está disponible
        return EstadoVisualPuesto.DISPONIBLE;
    }

    /**
     * Calcula estadísticas generales de ocupación para un conjunto de puestos de un plano.
     */
    @Transactional(readOnly = true)
    public EstadisticasPlanoResponse calcularEstadisticas(UUID mercadoId, String mercadoNombre, UUID planoId, Integer versionNumero, String versionEstado, List<UUID> puestoIds) {
        long total = puestoIds != null ? puestoIds.size() : 0;
        long disponibles = 0;
        long ocupados = 0;
        long reservados = 0;
        long inactivos = 0;
        long restringidos = 0;

        if (puestoIds != null && !puestoIds.isEmpty()) {
            List<PuestoComercial> puestos = puestoRepository.findAllById(puestoIds);
            for (PuestoComercial p : puestos) {
                EstadoVisualPuesto estado = calcularEstadoVisual(p);
                switch (estado) {
                    case DISPONIBLE -> disponibles++;
                    case OCUPADO -> ocupados++;
                    case RESERVADO -> reservados++;
                    case INACTIVO -> inactivos++;
                    case RESTRINGIDO -> restringidos++;
                }
            }
        }

        BigDecimal porcentajeOcupacion = BigDecimal.ZERO;
        BigDecimal porcentajeDisponibilidad = BigDecimal.ZERO;

        if (total > 0) {
            porcentajeOcupacion = BigDecimal.valueOf(ocupados)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
            porcentajeDisponibilidad = BigDecimal.valueOf(disponibles)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
        }

        return EstadisticasPlanoResponse.builder()
                .mercadoId(mercadoId)
                .mercadoNombre(mercadoNombre)
                .planoId(planoId)
                .versionNumero(versionNumero)
                .versionEstado(versionEstado)
                .totalPuestos(total)
                .puestosDisponibles(disponibles)
                .puestosOcupados(ocupados)
                .puestosReservados(reservados)
                .puestosInactivos(inactivos)
                .puestosRestringidos(restringidos)
                .porcentajeOcupacion(porcentajeOcupacion)
                .porcentajeDisponibilidad(porcentajeDisponibilidad)
                .build();
    }

    /**
     * Obtiene el detalle completo de un puesto para mostrar en el modal del plano,
     * aplicando control de confidencialidad para datos personales y financieros.
     */
    @Transactional(readOnly = true)
    public PuestoPlanoResponse obtenerDetallePuestoParaPlano(UUID puestoId, Authentication auth) {
        PuestoComercial puesto = puestoRepository.findById(puestoId)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto comercial no encontrado con ID: " + puestoId));

        EstadoVisualPuesto visual = calcularEstadoVisual(puesto);

        boolean puedeVerComerciante = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("MERCADOS_COMERCIANTE_VER") ||
                               a.getAuthority().equals("ROLE_ADMIN") ||
                               a.getAuthority().equals("ROLE_SUPERADMIN"));

        boolean puedeVerFinanzas = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("MERCADOS_FINANZAS_VER") ||
                               a.getAuthority().equals("ROLE_ADMIN") ||
                               a.getAuthority().equals("ROLE_SUPERADMIN"));

        PuestoPlanoResponse.PuestoPlanoResponseBuilder builder = PuestoPlanoResponse.builder()
                .puestoId(puesto.getId())
                .codigo(puesto.getCodigo())
                .numero(puesto.getNumero())
                .nombreReferencial(puesto.getNombreReferencial())
                .descripcion(puesto.getDescripcion())
                .areaMetrosCuadrados(puesto.getAreaMetrosCuadrados())
                .anchoMetros(puesto.getAnchoMetros())
                .largoMetros(puesto.getLargoMetros())
                .mercadoId(puesto.getMercado() != null ? puesto.getMercado().getId() : null)
                .mercadoNombre(puesto.getMercado() != null ? puesto.getMercado().getNombre() : null)
                .sectorId(puesto.getSector() != null ? puesto.getSector().getId() : null)
                .sectorNombre(puesto.getSector() != null ? puesto.getSector().getNombre() : null)
                .pabellonId(puesto.getPabellon() != null ? puesto.getPabellon().getId() : null)
                .pabellonNombre(puesto.getPabellon() != null ? puesto.getPabellon().getNombre() : null)
                .zonaId(puesto.getZona() != null ? puesto.getZona().getId() : null)
                .zonaNombre(puesto.getZona() != null ? puesto.getZona().getNombre() : null)
                .tipoPuestoNombre(puesto.getTipoPuesto() != null ? puesto.getTipoPuesto().getNombre() : null)
                .estadoFisico(puesto.getEstadoFisico() != null ? puesto.getEstadoFisico().name() : "BUENO")
                .estadoVisual(visual)
                .estadoVisualLabel(visual.getLabel())
                .colorHex(visual.getColorHex())
                .ocupado(visual == EstadoVisualPuesto.OCUPADO)
                .datosPrivadosVisibles(puedeVerComerciante)
                .finanzasVisibles(puedeVerFinanzas);

        // 1. Asignación vigente
        Optional<AsignacionPuesto> asignacionOpt = asignacionRepository.findFirstByPuestoIdAndEstado(puestoId, EstadoAsignacion.VIGENTE);
        if (asignacionOpt.isPresent()) {
            AsignacionPuesto asig = asignacionOpt.get();
            builder.asignacionId(asig.getId())
                    .asignacionCodigo(asig.getCodigoAsignacion())
                    .asignacionFechaInicio(asig.getFechaInicio())
                    .asignacionFechaFin(asig.getFechaFin())
                    .asignacionEstado(asig.getEstado().name());

            Comerciante com = asig.getComerciante();
            if (com != null) {
                builder.comercianteId(com.getId());
                if (puedeVerComerciante) {
                    builder.comercianteNombre(com.getNombreOTitular())
                            .comercianteDocumento(com.getNumeroDocumentoTitular())
                            .comercianteTipo(com.getTipoComerciante() != null ? com.getTipoComerciante().name() : null)
                            .comercianteTelefono(com.getTelefonoContacto());
                } else {
                    // Enmascarar información sensible para usuarios sin permiso
                    builder.comercianteNombre("Comerciante Autorizado Registrado")
                            .comercianteDocumento(enmascararDocumento(com.getNumeroDocumentoTitular()))
                            .comercianteTipo("CONFIDENCIAL");
                }
            }
        }

        // 2. Contrato más reciente
        List<ContratoOcupacion> contratos = contratoRepository.findByPuestoIdOrderByCreatedAtDesc(puestoId);
        if (!contratos.isEmpty()) {
            ContratoOcupacion cont = contratos.get(0);
            builder.contratoId(cont.getId())
                    .contratoCodigo(cont.getCodigoContrato())
                    .contratoFechaFin(cont.getFechaFin())
                    .contratoEstado(cont.getEstado() != null ? cont.getEstado().name() : null);
        }

        // 3. Autorización comercial más reciente
        List<AutorizacionComercial> autorizaciones = autorizacionRepository.findByPuestoIdOrderByCreatedAtDesc(puestoId);
        if (!autorizaciones.isEmpty()) {
            AutorizacionComercial aut = autorizaciones.get(0);
            builder.autorizacionId(aut.getId())
                    .autorizacionCodigo(aut.getCodigoAutorizacion())
                    .autorizacionFechaFin(aut.getFechaFin())
                    .autorizacionEstado(aut.getEstado() != null ? aut.getEstado().name() : null);
        }

        // 4. Incidencias
        List<IncidenciaMercado> incidencias = incidenciaRepository.findByPuestoIdOrderByFechaReporteDesc(puestoId);
        long pendientes = incidencias.stream().filter(i -> i.getEstado() != null && !i.getEstado().name().contains("CERRADA") && !i.getEstado().name().contains("RESUELTA")).count();
        builder.incidenciasPendientes(pendientes);
        if (!incidencias.isEmpty()) {
            builder.ultimaIncidenciaDescripcion(incidencias.get(0).getDescripcion());
        }

        // 5. Inspecciones
        List<InspeccionMercado> inspecciones = inspeccionRepository.findByPuestoIdOrderByFechaProgramadaDesc(puestoId);
        builder.inspeccionesRealizadas((long) inspecciones.size());
        if (!inspecciones.isEmpty()) {
            InspeccionMercado insp = inspecciones.get(0);
            builder.ultimaInspeccionFecha(insp.getFechaProgramada())
                    .ultimaInspeccionEstado(insp.getEstado() != null ? insp.getEstado().name() : null);
        }

        // 6. Finanzas
        if (puedeVerFinanzas) {
            List<ObligacionMercado> obligaciones = obligacionRepository.findByPuestoIdOrderByFechaVencimientoAsc(puestoId);
            BigDecimal deudaTotal = obligaciones.stream()
                    .map(ObligacionMercado::getSaldoPendiente)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            boolean tieneVencida = obligaciones.stream()
                    .anyMatch(o -> o.getFechaVencimiento() != null && o.getFechaVencimiento().isBefore(LocalDate.now()) && o.getSaldoPendiente() != null && o.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0);

            builder.totalDeudaPendiente(deudaTotal)
                    .tieneDeudaVencida(tieneVencida);
        } else {
            builder.totalDeudaPendiente(null)
                    .tieneDeudaVencida(null);
        }

        return builder.build();
    }

    private String enmascararDocumento(String documento) {
        if (documento == null || documento.length() < 4) {
            return "***";
        }
        return documento.substring(0, 2) + "****" + documento.substring(documento.length() - 2);
    }
}
