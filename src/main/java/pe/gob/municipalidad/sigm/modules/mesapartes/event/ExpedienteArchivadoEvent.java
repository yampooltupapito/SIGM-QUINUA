package pe.gob.municipalidad.sigm.modules.mesapartes.event;

public class ExpedienteArchivadoEvent extends ExpedienteBaseEvent {
    public ExpedienteArchivadoEvent(Object source, Long expedienteId, String numeroExpediente, String asunto,
                                    Long solicitanteId, String solicitanteNombre, String solicitanteEmail,
                                    Long oficinaId, String oficinaNombre, String motivoArchivo) {
        super(source, expedienteId, numeroExpediente, asunto, solicitanteId, solicitanteNombre, solicitanteEmail, oficinaId, oficinaNombre, motivoArchivo);
    }
}
