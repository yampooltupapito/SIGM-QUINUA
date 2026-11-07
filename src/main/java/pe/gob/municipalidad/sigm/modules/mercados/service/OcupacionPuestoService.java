package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mercados.dto.*;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionDevolucion;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PuestoComercial;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoMovimientoOcupacion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AsignacionDevolucionRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.AsignacionPuestoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.PuestoComercialRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class OcupacionPuestoService {

    private final AsignacionPuestoRepository asignacionRepository;
    private final PuestoComercialRepository puestoRepository;
    private final AsignacionDevolucionRepository devolucionRepository;
    private final DocumentoRepository documentoRepository;
    private final HistorialOcupacionService historialService;
    private final AuditoriaAdministrativaService auditoriaService;

    public OcupacionPuestoService(
            AsignacionPuestoRepository asignacionRepository,
            PuestoComercialRepository puestoRepository,
            AsignacionDevolucionRepository devolucionRepository,
            DocumentoRepository documentoRepository,
            HistorialOcupacionService historialService,
            AuditoriaAdministrativaService auditoriaService) {
        this.asignacionRepository = asignacionRepository;
        this.puestoRepository = puestoRepository;
        this.devolucionRepository = devolucionRepository;
        this.documentoRepository = documentoRepository;
        this.historialService = historialService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public AsignacionDetailResponse activarOcupacion(UUID asignacionId) {
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        if (!asignacion.getEstado().permiteActivacion()) {
            throw new BusinessException("Solo se pueden activar asignaciones que se encuentren en estado APROBADA. Estado actual: " + asignacion.getEstado());
        }

        PuestoComercial puesto = asignacion.getPuesto();

        // Control estricto de concurrencia e incompatibilidad en BD y Backend
        if (asignacionRepository.existsByPuestoIdAndEstado(puesto.getId(), EstadoAsignacion.VIGENTE)) {
            throw new ConflictException("Conflicto de ocupación: El puesto '" + puesto.getCodigo() +
                    "' ya cuenta con otra asignación VIGENTE activa en este momento.");
        }

        if (puesto.getEstado() != EstadoPuesto.DISPONIBLE) {
            throw new ConflictException("El puesto '" + puesto.getCodigo() +
                    "' no se encuentra disponible físicamente para ocupación. Estado actual: " + puesto.getEstado());
        }

        EstadoAsignacion estadoAnterior = asignacion.getEstado();
        asignacion.setEstado(EstadoAsignacion.VIGENTE);
        if (asignacion.getFechaInicio() == null) {
            asignacion.setFechaInicio(LocalDate.now());
        }

        try {
            asignacionRepository.saveAndFlush(asignacion);
        } catch (DataIntegrityViolationException ex) {
            log.error("Violación de restricción única en BD al activar asignación para puesto {}", puesto.getCodigo(), ex);
            throw new ConflictException("Incompatibilidad concurrente: Ya existe una asignación vigente para el puesto " + puesto.getCodigo());
        }

        EstadoPuesto anteriorPuesto = puesto.getEstado();
        puesto.setEstado(EstadoPuesto.OCUPADO);
        puestoRepository.save(puesto);

        String usuario = obtenerUsuarioAutenticado();
        historialService.registrarMovimiento(
                puesto,
                asignacion.getComerciante(),
                asignacion,
                TipoMovimientoOcupacion.ACTIVACION,
                "Activación de ocupación efectiva de puesto comercial",
                asignacion.getDocumentoSustento(),
                estadoAnterior.name(),
                EstadoAsignacion.VIGENTE.name(),
                "Puesto pasó de " + anteriorPuesto + " a OCUPADO"
        );

        auditoriaService.registrar("ACTIVAR_OCUPACION_PUESTO", "mercados_asignaciones", asignacion.getId(), "EXITO",
                "Ocupación activada para puesto " + puesto.getCodigo() + " - Comerciante " + asignacion.getComerciante().getCodigoComerciante());

        return toDetailResponse(asignacion);
    }

    @Transactional
    public AsignacionDetailResponse suspenderAsignacion(UUID asignacionId, AsignacionSuspensionRequest request) {
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        if (!asignacion.getEstado().permiteSuspension()) {
            throw new BusinessException("Solo se pueden suspender asignaciones que se encuentren en estado VIGENTE. Estado actual: " + asignacion.getEstado());
        }

        String usuario = obtenerUsuarioAutenticado();
        Documento doc = null;
        if (request.getDocumentoSustentoId() != null) {
            doc = documentoRepository.findById(request.getDocumentoSustentoId()).orElse(null);
        }

        EstadoAsignacion anterior = asignacion.getEstado();
        asignacion.setEstado(EstadoAsignacion.SUSPENDIDA);
        asignacion.setMotivoSuspension(request.getMotivoSuspension());
        asignacionRepository.save(asignacion);

        historialService.registrarMovimiento(
                asignacion.getPuesto(),
                asignacion.getComerciante(),
                asignacion,
                TipoMovimientoOcupacion.SUSPENSION,
                "Suspensión temporal: " + request.getMotivoSuspension(),
                doc,
                anterior.name(),
                EstadoAsignacion.SUSPENDIDA.name(),
                "Reserva de puesto mantenida: " + request.isMantieneReservaPuesto()
        );

        auditoriaService.registrar("SUSPENDER_ASIGNACION_PUESTO", "mercados_asignaciones", asignacion.getId(), "EXITO",
                "Asignación " + asignacion.getCodigoAsignacion() + " suspendida por " + usuario);

        return toDetailResponse(asignacion);
    }

    @Transactional
    public AsignacionDetailResponse reanudarAsignacion(UUID asignacionId, String motivo) {
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        if (!asignacion.getEstado().permiteReanudacion()) {
            throw new BusinessException("Solo se pueden reanudar asignaciones con estado SUSPENDIDA. Estado actual: " + asignacion.getEstado());
        }

        PuestoComercial puesto = asignacion.getPuesto();
        // Verificar que no haya otra vigente
        if (asignacionRepository.existsByPuestoIdAndEstado(puesto.getId(), EstadoAsignacion.VIGENTE)) {
            throw new ConflictException("No se puede reanudar: el puesto ya registra otra asignación vigente.");
        }

        EstadoAsignacion anterior = asignacion.getEstado();
        asignacion.setEstado(EstadoAsignacion.VIGENTE);
        asignacionRepository.save(asignacion);

        historialService.registrarMovimiento(
                puesto,
                asignacion.getComerciante(),
                asignacion,
                TipoMovimientoOcupacion.REANUDACION,
                "Reanudación de asignación comercial: " + (motivo != null ? motivo : "Subsanación de causales de suspensión"),
                null,
                anterior.name(),
                EstadoAsignacion.VIGENTE.name(),
                null
        );

        auditoriaService.registrar("REANUDAR_ASIGNACION_PUESTO", "mercados_asignaciones", asignacion.getId(), "EXITO",
                "Reanudada asignación " + asignacion.getCodigoAsignacion());

        return toDetailResponse(asignacion);
    }

    @Transactional
    public AsignacionDetailResponse finalizarAsignacion(UUID asignacionId, AsignacionFinalizacionRequest request) {
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        if (!asignacion.getEstado().permiteFinalizacion()) {
            throw new BusinessException("Solo se pueden finalizar asignaciones en estado VIGENTE o SUSPENDIDA. Estado actual: " + asignacion.getEstado());
        }

        String usuario = obtenerUsuarioAutenticado();
        Documento doc = null;
        if (request.getDocumentoSustentoId() != null) {
            doc = documentoRepository.findById(request.getDocumentoSustentoId()).orElse(null);
        }

        EstadoAsignacion anterior = asignacion.getEstado();
        asignacion.setEstado(EstadoAsignacion.FINALIZADA);
        asignacion.setMotivoFinalizacion(request.getMotivoFinalizacion());
        if (request.getFechaEfectivaCierre() != null) {
            asignacion.setFechaFin(request.getFechaEfectivaCierre());
        } else if (asignacion.getFechaFin() == null) {
            asignacion.setFechaFin(LocalDate.now());
        }

        asignacionRepository.save(asignacion);

        PuestoComercial puesto = asignacion.getPuesto();

        // Si se indicó registro inmediato de devolución
        if (request.isRegistrarDevolucionInmediata()) {
            DevolucionPuestoRequest devReq = DevolucionPuestoRequest.builder()
                    .estadoFisicoPuesto(request.getEstadoFisicoPuesto() != null ? request.getEstadoFisicoPuesto() : "BUENO")
                    .observaciones(request.getObservacionesDevolucion() != null ? request.getObservacionesDevolucion() : "Devolución registrada al finalizar asignación")
                    .documentoActaId(request.getDocumentoActaId())
                    .build();
            registrarDevolucion(asignacionId, devReq);
        } else {
            // Puesto queda liberado administrativamente
            EstadoPuesto estadoPuestoAnterior = puesto.getEstado();
            puesto.setEstado(EstadoPuesto.DISPONIBLE);
            puestoRepository.save(puesto);

            historialService.registrarMovimiento(
                    puesto,
                    asignacion.getComerciante(),
                    asignacion,
                    TipoMovimientoOcupacion.FINALIZACION,
                    "Finalización de asignación: " + request.getMotivoFinalizacion(),
                    doc,
                    anterior.name(),
                    EstadoAsignacion.FINALIZADA.name(),
                    "Puesto cambió de " + estadoPuestoAnterior + " a DISPONIBLE"
            );
        }

        auditoriaService.registrar("FINALIZAR_ASIGNACION_PUESTO", "mercados_asignaciones", asignacion.getId(), "EXITO",
                "Finalizada asignación " + asignacion.getCodigoAsignacion() + " - " + request.getMotivoFinalizacion());

        return toDetailResponse(asignacion);
    }

    @Transactional
    public DevolucionPuestoResponse registrarDevolucion(UUID asignacionId, DevolucionPuestoRequest request) {
        AsignacionPuesto asignacion = asignacionRepository.findByIdCompleto(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        if (!asignacion.getEstado().permiteDevolucion()) {
            throw new BusinessException("No procede registrar devolución para una asignación en estado: " + asignacion.getEstado());
        }

        Optional<AsignacionDevolucion> existente = devolucionRepository.findFirstByAsignacionIdOrderByFechaDevolucionDesc(asignacionId);
        if (existente.isPresent()) {
            throw new ConflictException("Ya existe un acta de devolución registrada para esta asignación.");
        }

        String usuario = obtenerUsuarioAutenticado();
        Documento acta = null;
        if (request.getDocumentoActaId() != null) {
            acta = documentoRepository.findById(request.getDocumentoActaId()).orElse(null);
        }

        PuestoComercial puesto = asignacion.getPuesto();

        AsignacionDevolucion devolucion = AsignacionDevolucion.builder()
                .asignacion(asignacion)
                .puesto(puesto)
                .fechaDevolucion(OffsetDateTime.now())
                .responsableRecepcion(usuario)
                .estadoFisicoPuesto(request.getEstadoFisicoPuesto() != null ? request.getEstadoFisicoPuesto() : "BUENO")
                .observaciones(request.getObservaciones())
                .documentoActa(acta)
                .build();

        AsignacionDevolucion guardada = devolucionRepository.save(devolucion);

        // Actualizar estado del puesto según estado físico recibido
        EstadoPuesto anteriorPuesto = puesto.getEstado();
        if ("MALO".equalsIgnoreCase(request.getEstadoFisicoPuesto()) || "REQUIERE_MANTENIMIENTO".equalsIgnoreCase(request.getEstadoFisicoPuesto())) {
            puesto.setEstado(EstadoPuesto.EN_MANTENIMIENTO);
        } else {
            puesto.setEstado(EstadoPuesto.DISPONIBLE);
        }
        puestoRepository.save(puesto);

        historialService.registrarMovimiento(
                puesto,
                asignacion.getComerciante(),
                asignacion,
                TipoMovimientoOcupacion.DEVOLUCION,
                "Devolución física y acta de recepción de puesto comercial (" + request.getEstadoFisicoPuesto() + ")",
                acta,
                anteriorPuesto.name(),
                puesto.getEstado().name(),
                request.getObservaciones()
        );

        auditoriaService.registrar("DEVOLUCION_PUESTO_COMERCIAL", "mercados_asignaciones_devoluciones", guardada.getId(), "EXITO",
                "Puesto " + puesto.getCodigo() + " recibido por " + usuario + " en estado: " + request.getEstadoFisicoPuesto());

        return DevolucionPuestoResponse.builder()
                .id(guardada.getId())
                .asignacionId(asignacion.getId())
                .puestoId(puesto.getId())
                .fechaDevolucion(guardada.getFechaDevolucion())
                .responsableRecepcion(guardada.getResponsableRecepcion())
                .estadoFisicoPuesto(guardada.getEstadoFisicoPuesto())
                .observaciones(guardada.getObservaciones())
                .documentoActaId(acta != null ? acta.getId() : null)
                .documentoActaNombre(acta != null ? acta.getNombreOriginal() : null)
                .build();
    }

    private String obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "SISTEMA";
    }

    private AsignacionDetailResponse toDetailResponse(AsignacionPuesto a) {
        String titular = "-";
        String doc = "-";
        if (a.getComerciante() != null) {
            if (a.getComerciante().getPersona() != null) {
                titular = a.getComerciante().getPersona().getNombreCompleto();
                doc = a.getComerciante().getPersona().getNumeroDocumento();
            } else if (a.getComerciante().getPersonaJuridica() != null) {
                titular = a.getComerciante().getPersonaJuridica().getRazonSocial();
                doc = a.getComerciante().getPersonaJuridica().getRuc();
            }
        }

        return AsignacionDetailResponse.builder()
                .id(a.getId())
                .codigoAsignacion(a.getCodigoAsignacion())
                .puestoId(a.getPuesto() != null ? a.getPuesto().getId() : null)
                .puestoCodigo(a.getPuesto() != null ? a.getPuesto().getCodigo() : null)
                .puestoNumero(a.getPuesto() != null ? a.getPuesto().getNumero() : null)
                .puestoEstado(a.getPuesto() != null ? a.getPuesto().getEstado().name() : null)
                .puestoArea(a.getPuesto() != null ? a.getPuesto().getAreaMetrosCuadrados() : null)
                .tipoPuestoNombre(a.getPuesto() != null && a.getPuesto().getTipoPuesto() != null ? a.getPuesto().getTipoPuesto().getNombre() : null)
                .mercadoId(a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getId() : null)
                .mercadoNombre(a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getNombre() : null)
                .mercadoCodigo(a.getPuesto() != null && a.getPuesto().getMercado() != null ? a.getPuesto().getMercado().getCodigo() : null)
                .sectorNombre(a.getPuesto() != null && a.getPuesto().getSector() != null ? a.getPuesto().getSector().getNombre() : null)
                .pabellonNombre(a.getPuesto() != null && a.getPuesto().getPabellon() != null ? a.getPuesto().getPabellon().getNombre() : null)
                .comercianteId(a.getComerciante() != null ? a.getComerciante().getId() : null)
                .comercianteCodigo(a.getComerciante() != null ? a.getComerciante().getCodigoComerciante() : null)
                .comercianteTipo(a.getComerciante() != null && a.getComerciante().getTipoComerciante() != null ? a.getComerciante().getTipoComerciante().name() : null)
                .titularNombre(titular)
                .titularDocumento(doc)
                .comercianteTelefono(a.getComerciante() != null ? a.getComerciante().getTelefonoContacto() : null)
                .comercianteCorreo(a.getComerciante() != null ? a.getComerciante().getCorreoContacto() : null)
                .comercianteDireccion(a.getComerciante() != null ? a.getComerciante().getDireccionContacto() : null)
                .comercianteEstado(a.getComerciante() != null && a.getComerciante().getEstado() != null ? a.getComerciante().getEstado().name() : null)
                .fechaSolicitud(a.getFechaSolicitud())
                .fechaInicio(a.getFechaInicio())
                .fechaFin(a.getFechaFin())
                .estado(a.getEstado())
                .estadoDescripcion(a.getEstado() != null ? a.getEstado().getDescripcion() : null)
                .badgeClase(a.getEstado() != null ? a.getEstado().getBadgeClase() : null)
                .tipoAsignacion(a.getTipoAsignacion())
                .motivo(a.getMotivo())
                .observaciones(a.getObservaciones())
                .usuarioRegistro(a.getUsuarioRegistro())
                .usuarioEvaluacion(a.getUsuarioEvaluacion())
                .fechaEvaluacion(a.getFechaEvaluacion())
                .observacionesEvaluacion(a.getObservacionesEvaluacion())
                .usuarioAprobacion(a.getUsuarioAprobacion())
                .fechaAprobacion(a.getFechaAprobacion())
                .motivoRechazo(a.getMotivoRechazo())
                .motivoSuspension(a.getMotivoSuspension())
                .motivoFinalizacion(a.getMotivoFinalizacion())
                .documentoSustentoId(a.getDocumentoSustento() != null ? a.getDocumentoSustento().getId() : null)
                .documentoSustentoNombre(a.getDocumentoSustento() != null ? a.getDocumentoSustento().getNombreOriginal() : null)
                .expedienteId(a.getExpedienteId())
                .version(a.getVersion())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
