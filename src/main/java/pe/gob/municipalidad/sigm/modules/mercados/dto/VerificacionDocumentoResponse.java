package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.time.LocalDate;

public class VerificacionDocumentoResponse {

    private String codigo;
    private String tipoDocumento;
    private String tipoNombre;
    private String estadoVerificacion;
    private boolean esValido;
    private LocalDate fechaEmision;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String mercadoNombre;
    private String puestoCodigo;
    private String titularEnmascarado;
    private String oficinaEmisora;
    private String hashSha256;
    private String mensaje;

    public VerificacionDocumentoResponse() {}

    public VerificacionDocumentoResponse(String codigo, String tipoDocumento, String tipoNombre, String estadoVerificacion, boolean esValido, LocalDate fechaEmision, LocalDate fechaInicio, LocalDate fechaFin, String mercadoNombre, String puestoCodigo, String titularEnmascarado, String oficinaEmisora, String hashSha256, String mensaje) {
        this.codigo = codigo;
        this.tipoDocumento = tipoDocumento;
        this.tipoNombre = tipoNombre;
        this.estadoVerificacion = estadoVerificacion;
        this.esValido = esValido;
        this.fechaEmision = fechaEmision;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.mercadoNombre = mercadoNombre;
        this.puestoCodigo = puestoCodigo;
        this.titularEnmascarado = titularEnmascarado;
        this.oficinaEmisora = oficinaEmisora;
        this.hashSha256 = hashSha256;
        this.mensaje = mensaje;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getTipoNombre() { return tipoNombre; }
    public void setTipoNombre(String tipoNombre) { this.tipoNombre = tipoNombre; }

    public String getEstadoVerificacion() { return estadoVerificacion; }
    public void setEstadoVerificacion(String estadoVerificacion) { this.estadoVerificacion = estadoVerificacion; }

    public boolean isEsValido() { return esValido; }
    public void setEsValido(boolean esValido) { this.esValido = esValido; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public String getPuestoCodigo() { return puestoCodigo; }
    public void setPuestoCodigo(String puestoCodigo) { this.puestoCodigo = puestoCodigo; }

    public String getTitularEnmascarado() { return titularEnmascarado; }
    public void setTitularEnmascarado(String titularEnmascarado) { this.titularEnmascarado = titularEnmascarado; }

    public String getOficinaEmisora() { return oficinaEmisora; }
    public void setOficinaEmisora(String oficinaEmisora) { this.oficinaEmisora = oficinaEmisora; }

    public String getHashSha256() { return hashSha256; }
    public void setHashSha256(String hashSha256) { this.hashSha256 = hashSha256; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
