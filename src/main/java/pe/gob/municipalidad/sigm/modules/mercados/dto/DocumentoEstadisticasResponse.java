package pe.gob.municipalidad.sigm.modules.mercados.dto;

public class DocumentoEstadisticasResponse {

    private long contratosRegistrados;
    private long contratosVigentes;
    private long contratosVencidos;
    private long contratosSuspendidos;
    private long contratosRevision;

    private long autorizacionesRegistradas;
    private long autorizacionesVigentes;
    private long autorizacionesProximasVencer;
    private long autorizacionesVencidas;
    private long autorizacionesRevocadas;

    private long totalDocumentos;

    public DocumentoEstadisticasResponse() {}

    public DocumentoEstadisticasResponse(long contratosRegistrados, long contratosVigentes, long contratosVencidos, long contratosSuspendidos, long contratosRevision, long autorizacionesRegistradas, long autorizacionesVigentes, long autorizacionesProximasVencer, long autorizacionesVencidas, long autorizacionesRevocadas, long totalDocumentos) {
        this.contratosRegistrados = contratosRegistrados;
        this.contratosVigentes = contratosVigentes;
        this.contratosVencidos = contratosVencidos;
        this.contratosSuspendidos = contratosSuspendidos;
        this.contratosRevision = contratosRevision;
        this.autorizacionesRegistradas = autorizacionesRegistradas;
        this.autorizacionesVigentes = autorizacionesVigentes;
        this.autorizacionesProximasVencer = autorizacionesProximasVencer;
        this.autorizacionesVencidas = autorizacionesVencidas;
        this.autorizacionesRevocadas = autorizacionesRevocadas;
        this.totalDocumentos = totalDocumentos;
    }

    public long getContratosRegistrados() { return contratosRegistrados; }
    public void setContratosRegistrados(long contratosRegistrados) { this.contratosRegistrados = contratosRegistrados; }

    public long getContratosVigentes() { return contratosVigentes; }
    public void setContratosVigentes(long contratosVigentes) { this.contratosVigentes = contratosVigentes; }

    public long getContratosVencidos() { return contratosVencidos; }
    public void setContratosVencidos(long contratosVencidos) { this.contratosVencidos = contratosVencidos; }

    public long getContratosSuspendidos() { return contratosSuspendidos; }
    public void setContratosSuspendidos(long contratosSuspendidos) { this.contratosSuspendidos = contratosSuspendidos; }

    public long getContratosRevision() { return contratosRevision; }
    public void setContratosRevision(long contratosRevision) { this.contratosRevision = contratosRevision; }

    public long getAutorizacionesRegistradas() { return autorizacionesRegistradas; }
    public void setAutorizacionesRegistradas(long autorizacionesRegistradas) { this.autorizacionesRegistradas = autorizacionesRegistradas; }

    public long getAutorizacionesVigentes() { return autorizacionesVigentes; }
    public void setAutorizacionesVigentes(long autorizacionesVigentes) { this.autorizacionesVigentes = autorizacionesVigentes; }

    public long getAutorizacionesProximasVencer() { return autorizacionesProximasVencer; }
    public void setAutorizacionesProximasVencer(long autorizacionesProximasVencer) { this.autorizacionesProximasVencer = autorizacionesProximasVencer; }

    public long getAutorizacionesVencidas() { return autorizacionesVencidas; }
    public void setAutorizacionesVencidas(long autorizacionesVencidas) { this.autorizacionesVencidas = autorizacionesVencidas; }

    public long getAutorizacionesRevocadas() { return autorizacionesRevocadas; }
    public void setAutorizacionesRevocadas(long autorizacionesRevocadas) { this.autorizacionesRevocadas = autorizacionesRevocadas; }

    public long getTotalDocumentos() { return totalDocumentos; }
    public void setTotalDocumentos(long totalDocumentos) { this.totalDocumentos = totalDocumentos; }
}
