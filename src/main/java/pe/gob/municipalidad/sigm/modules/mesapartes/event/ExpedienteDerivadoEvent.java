package pe.gob.municipalidad.sigm.modules.mesapartes.event;

public class ExpedienteDerivadoEvent extends ExpedienteBaseEvent {

    private final Long oficinaDestinoId;
    private final String oficinaDestinoNombre;

    public ExpedienteDerivadoEvent(Object source, Long expedienteId, String numeroExpediente, String asunto,
                                  Long solicitanteId, String solicitanteNombre, String solicitanteEmail,
                                  Long oficinaOrigenId, String oficinaOrigenNombre,
                                  Long oficinaDestinoId, String oficinaDestinoNombre,
                                  String motivo) {
        super(source, expedienteId, numeroExpediente, asunto, solicitanteId, solicitanteNombre, solicitanteEmail, oficinaOrigenId, oficinaOrigenNombre, motivo);
        this.oficinaDestinoId = oficinaDestinoId;
        this.oficinaDestinoNombre = oficinaDestinoNombre;
    }

    public Long getOficinaDestinoId() {
        return oficinaDestinoId;
    }

    public String getOficinaDestinoNombre() {
        return oficinaDestinoNombre;
    }
}
