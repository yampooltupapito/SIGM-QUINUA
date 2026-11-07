package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.time.OffsetDateTime;

public class ContratoHistorialResponse {

    private Long id;
    private String estadoAnterior;
    private String estadoNuevo;
    private String accion;
    private String motivo;
    private String usuario;
    private OffsetDateTime fecha;
    private String documentoReferencia;

    public ContratoHistorialResponse() {}

    public ContratoHistorialResponse(Long id, String estadoAnterior, String estadoNuevo, String accion, String motivo, String usuario, OffsetDateTime fecha, String documentoReferencia) {
        this.id = id;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.accion = accion;
        this.motivo = motivo;
        this.usuario = usuario;
        this.fecha = fecha;
        this.documentoReferencia = documentoReferencia;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public OffsetDateTime getFecha() { return fecha; }
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }

    public String getDocumentoReferencia() { return documentoReferencia; }
    public void setDocumentoReferencia(String documentoReferencia) { this.documentoReferencia = documentoReferencia; }
}
