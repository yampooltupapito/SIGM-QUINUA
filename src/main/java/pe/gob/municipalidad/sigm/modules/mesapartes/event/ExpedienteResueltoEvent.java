package pe.gob.municipalidad.sigm.modules.mesapartes.event;

public class ExpedienteResueltoEvent extends ExpedienteBaseEvent {
    public ExpedienteResueltoEvent(Object source, Long expedienteId, String numeroExpediente, String asunto,
                                   Long solicitanteId, String solicitanteNombre, String solicitanteEmail,
                                   Long oficinaId, String oficinaNombre, String resolucionDetalle) {
        super(source, expedienteId, numeroExpediente, asunto, solicitanteId, solicitanteNombre, solicitanteEmail, oficinaId, oficinaNombre, resolucionDetalle);
    }
}
