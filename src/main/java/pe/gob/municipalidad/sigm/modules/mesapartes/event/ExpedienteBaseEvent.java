package pe.gob.municipalidad.sigm.modules.mesapartes.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.OffsetDateTime;

/**
 * Evento base de dominio para cambios y actuaciones en expedientes de Mesa de Partes Virtual.
 */
@Getter
public abstract class ExpedienteBaseEvent extends ApplicationEvent {

    private final Long expedienteId;
    private final String numeroExpediente;
    private final String asunto;
    private final Long solicitanteId;
    private final String solicitanteNombre;
    private final String solicitanteEmail;
    private final Long oficinaId;
    private final String oficinaNombre;
    private final String detalle;
    private final OffsetDateTime fechaEvento;

    protected ExpedienteBaseEvent(Object source,
                                  Long expedienteId,
                                  String numeroExpediente,
                                  String asunto,
                                  Long solicitanteId,
                                  String solicitanteNombre,
                                  String solicitanteEmail,
                                  Long oficinaId,
                                  String oficinaNombre,
                                  String detalle) {
        super(source);
        this.expedienteId = expedienteId;
        this.numeroExpediente = numeroExpediente;
        this.asunto = asunto;
        this.solicitanteId = solicitanteId;
        this.solicitanteNombre = solicitanteNombre;
        this.solicitanteEmail = solicitanteEmail;
        this.oficinaId = oficinaId;
        this.oficinaNombre = oficinaNombre;
        this.detalle = detalle;
        this.fechaEvento = OffsetDateTime.now();
    }
}
