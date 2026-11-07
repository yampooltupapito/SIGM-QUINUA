package pe.gob.municipalidad.sigm.modules.mesapartes.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ExpedienteHistorial;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.event.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteHistorialRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Motor de workflow y transiciones de estado para expedientes de Mesa de Partes Virtual.
 * Garantiza la atomicidad de los cambios de estado, la inmutabilidad del historial
 * y la publicación coherente de eventos de dominio.
 */
@Service
public class ExpedienteWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(ExpedienteWorkflowService.class);

    // Mapa de transiciones permitidas: EstadoOrigen -> Set de EstadosDestino
    private static final Map<EstadoExpediente, Set<EstadoExpediente>> TRANSICIONES_PERMITIDAS = new EnumMap<>(EstadoExpediente.class);

    static {
        TRANSICIONES_PERMITIDAS.put(EstadoExpediente.BORRADOR, EnumSet.of(EstadoExpediente.PRESENTADO, EstadoExpediente.ANULADO));
        TRANSICIONES_PERMITIDAS.put(EstadoExpediente.PRESENTADO, EnumSet.of(EstadoExpediente.RECIBIDO, EstadoExpediente.EN_REVISION, EstadoExpediente.EN_ATENCION, EstadoExpediente.OBSERVADO, EstadoExpediente.ANULADO));
        TRANSICIONES_PERMITIDAS.put(EstadoExpediente.RECIBIDO, EnumSet.of(EstadoExpediente.EN_REVISION, EstadoExpediente.DERIVADO, EstadoExpediente.EN_ATENCION, EstadoExpediente.OBSERVADO, EstadoExpediente.ANULADO));
        TRANSICIONES_PERMITIDAS.put(EstadoExpediente.EN_REVISION, EnumSet.of(EstadoExpediente.OBSERVADO, EstadoExpediente.DERIVADO, EstadoExpediente.EN_ATENCION, EstadoExpediente.RESUELTO, EstadoExpediente.ANULADO));
        TRANSICIONES_PERMITIDAS.put(EstadoExpediente.OBSERVADO, EnumSet.of(EstadoExpediente.EN_SUBSANACION, EstadoExpediente.EN_REVISION, EstadoExpediente.EN_ATENCION, EstadoExpediente.RECIBIDO, EstadoExpediente.ANULADO, EstadoExpediente.ARCHIVADO));
        TRANSICIONES_PERMITIDAS.put(EstadoExpediente.EN_SUBSANACION, EnumSet.of(EstadoExpediente.EN_REVISION, EstadoExpediente.RECIBIDO, EstadoExpediente.EN_ATENCION, EstadoExpediente.OBSERVADO, EstadoExpediente.RESUELTO));
        TRANSICIONES_PERMITIDAS.put(EstadoExpediente.DERIVADO, EnumSet.of(EstadoExpediente.RECIBIDO, EstadoExpediente.EN_ATENCION, EstadoExpediente.DERIVADO, EstadoExpediente.EN_REVISION, EstadoExpediente.OBSERVADO, EstadoExpediente.RESUELTO));
        TRANSICIONES_PERMITIDAS.put(EstadoExpediente.EN_ATENCION, EnumSet.of(EstadoExpediente.RESUELTO, EstadoExpediente.OBSERVADO, EstadoExpediente.DERIVADO, EstadoExpediente.EN_REVISION));
        TRANSICIONES_PERMITIDAS.put(EstadoExpediente.RESUELTO, EnumSet.of(EstadoExpediente.NOTIFICADO, EstadoExpediente.ARCHIVADO, EstadoExpediente.EN_ATENCION));
        TRANSICIONES_PERMITIDAS.put(EstadoExpediente.NOTIFICADO, EnumSet.of(EstadoExpediente.ARCHIVADO, EstadoExpediente.EN_ATENCION));
        TRANSICIONES_PERMITIDAS.put(EstadoExpediente.ARCHIVADO, EnumSet.of(EstadoExpediente.EN_ATENCION));
        TRANSICIONES_PERMITIDAS.put(EstadoExpediente.ANULADO, Collections.emptySet());
    }

    private final ExpedienteRepository expedienteRepository;
    private final ExpedienteHistorialRepository historialRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ExpedienteWorkflowService(ExpedienteRepository expedienteRepository,
                                     ExpedienteHistorialRepository historialRepository,
                                     ApplicationEventPublisher eventPublisher) {
        this.expedienteRepository = expedienteRepository;
        this.historialRepository = historialRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Valida si una transición de estado es legal en el grafo de estados.
     */
    public boolean esTransicionPermitida(EstadoExpediente origen, EstadoExpediente destino) {
        if (origen == null || destino == null) {
            return false;
        }
        Set<EstadoExpediente> permitidos = TRANSICIONES_PERMITIDAS.get(origen);
        return permitidos != null && permitidos.contains(destino);
    }

    /**
     * Ejecuta una transición de estado de manera atómica, registrando el historial y emitiendo eventos.
     */
    @Transactional
    public Expediente transicionar(Expediente expediente,
                                   EstadoExpediente nuevoEstado,
                                   String accion,
                                   String observacion,
                                   Usuario usuarioResponsable,
                                   Oficina oficina) {
        EstadoExpediente estadoAnterior = expediente.getEstadoActual();

        if (!esTransicionPermitida(estadoAnterior, nuevoEstado)) {
            log.warn("Intento de transición ilegal para expediente ID {}: {} -> {}", expediente.getId(), estadoAnterior, nuevoEstado);
            throw new BusinessException(String.format("Transición no permitida: no es posible pasar de estado '%s' a '%s'.", estadoAnterior, nuevoEstado));
        }

        // Actualizar estado del expediente
        expediente.setEstadoActual(nuevoEstado);
        OffsetDateTime ahora = OffsetDateTime.now();

        if (nuevoEstado == EstadoExpediente.PRESENTADO && expediente.getFechaPresentacion() == null) {
            expediente.setFechaPresentacion(ahora);
        } else if (nuevoEstado == EstadoExpediente.RECIBIDO && expediente.getFechaRecepcion() == null) {
            expediente.setFechaRecepcion(ahora);
        } else if (nuevoEstado == EstadoExpediente.RESUELTO || nuevoEstado == EstadoExpediente.ARCHIVADO || nuevoEstado == EstadoExpediente.ANULADO) {
            if (expediente.getFechaCierre() == null) {
                expediente.setFechaCierre(ahora);
            }
        }

        Expediente guardado = expedienteRepository.save(expediente);

        // Registro de historial inmutable
        String correlacionId = UUID.randomUUID().toString();
        ExpedienteHistorial historial = ExpedienteHistorial.builder()
                .expediente(guardado)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(nuevoEstado)
                .accion((accion != null && !accion.isBlank()) ? accion : "CAMBIO_ESTADO_" + nuevoEstado)
                .observacion(observacion)
                .usuarioResponsable(usuarioResponsable)
                .oficina(oficina != null ? oficina : guardado.getOficinaResponsable())
                .fechaMovimiento(ahora)
                .identificadorCorrelacion(correlacionId)
                .build();

        historialRepository.save(historial);

        // Publicar evento de dominio correspondiente
        publicarEvento(guardado, nuevoEstado, observacion);

        log.info("Expediente {} transicionó de {} a {} (Acción: {})", guardado.getNumeroExpediente(), estadoAnterior, nuevoEstado, accion);
        return guardado;
    }

    private void publicarEvento(Expediente expediente, EstadoExpediente nuevoEstado, String detalle) {
        Long solicitanteId = expediente.getSolicitante() != null ? expediente.getSolicitante().getId() : null;
        String solicitanteNombre = expediente.getSolicitante() != null ? expediente.getSolicitante().getNombreCompleto() : "Ciudadano";
        String solicitanteEmail = (expediente.getSolicitante() != null && expediente.getSolicitante().getCorreo() != null)
                ? expediente.getSolicitante().getCorreo() : null;

        Long oficinaId = expediente.getOficinaResponsable() != null ? expediente.getOficinaResponsable().getId() : null;
        String oficinaNombre = expediente.getOficinaResponsable() != null ? expediente.getOficinaResponsable().getNombre() : "Mesa de Partes";

        switch (nuevoEstado) {
            case PRESENTADO:
                eventPublisher.publishEvent(new ExpedientePresentadoEvent(this, expediente.getId(),
                        expediente.getNumeroExpediente(), expediente.getAsunto(), solicitanteId, solicitanteNombre,
                        solicitanteEmail, oficinaId, oficinaNombre, detalle));
                break;
            case RECIBIDO:
                eventPublisher.publishEvent(new ExpedienteRecibidoEvent(this, expediente.getId(),
                        expediente.getNumeroExpediente(), expediente.getAsunto(), solicitanteId, solicitanteNombre,
                        solicitanteEmail, oficinaId, oficinaNombre, detalle));
                break;
            case OBSERVADO:
                eventPublisher.publishEvent(new ExpedienteObservadoEvent(this, expediente.getId(),
                        expediente.getNumeroExpediente(), expediente.getAsunto(), solicitanteId, solicitanteNombre,
                        solicitanteEmail, oficinaId, oficinaNombre, detalle));
                break;
            case RESUELTO:
                eventPublisher.publishEvent(new ExpedienteResueltoEvent(this, expediente.getId(),
                        expediente.getNumeroExpediente(), expediente.getAsunto(), solicitanteId, solicitanteNombre,
                        solicitanteEmail, oficinaId, oficinaNombre, detalle));
                break;
            case ARCHIVADO:
                eventPublisher.publishEvent(new ExpedienteArchivadoEvent(this, expediente.getId(),
                        expediente.getNumeroExpediente(), expediente.getAsunto(), solicitanteId, solicitanteNombre,
                        solicitanteEmail, oficinaId, oficinaNombre, detalle));
                break;
            default:
                break;
        }
    }
}
