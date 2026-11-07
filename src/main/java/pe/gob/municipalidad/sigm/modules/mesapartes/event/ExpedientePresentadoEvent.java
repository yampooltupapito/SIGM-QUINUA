package pe.gob.municipalidad.sigm.modules.mesapartes.event;

public class ExpedientePresentadoEvent extends ExpedienteBaseEvent {
    public ExpedientePresentadoEvent(Object source, Long expedienteId, String numeroExpediente, String asunto,
                                   Long solicitanteId, String solicitanteNombre, String solicitanteEmail,
                                   Long oficinaId, String oficinaNombre, String detalle) {
        super(source, expedienteId, numeroExpediente, asunto, solicitanteId, solicitanteNombre, solicitanteEmail, oficinaId, oficinaNombre, detalle);
    }
}
