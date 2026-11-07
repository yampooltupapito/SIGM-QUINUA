package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.time.LocalDate;
import java.util.UUID;

public class ComercianteReporteItem {
    private UUID id;
    private String codigoComerciante;
    private String tipoComerciante;
    private String nombreORazonSocial;
    private String tipoDocumento;
    private String numeroDocumento;
    private String telefono;
    private String correo;
    private String estado;
    private LocalDate fechaRegistro;
    private int asignacionesVigentes;
    private String mercadosRelacionados;
    private String girosComerciales;

    public ComercianteReporteItem() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigoComerciante() { return codigoComerciante; }
    public void setCodigoComerciante(String codigoComerciante) { this.codigoComerciante = codigoComerciante; }

    public String getTipoComerciante() { return tipoComerciante; }
    public void setTipoComerciante(String tipoComerciante) { this.tipoComerciante = tipoComerciante; }

    public String getNombreORazonSocial() { return nombreORazonSocial; }
    public void setNombreORazonSocial(String nombreORazonSocial) { this.nombreORazonSocial = nombreORazonSocial; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public int getAsignacionesVigentes() { return asignacionesVigentes; }
    public void setAsignacionesVigentes(int asignacionesVigentes) { this.asignacionesVigentes = asignacionesVigentes; }

    public String getMercadosRelacionados() { return mercadosRelacionados; }
    public void setMercadosRelacionados(String mercadosRelacionados) { this.mercadosRelacionados = mercadosRelacionados; }

    public String getGirosComerciales() { return girosComerciales; }
    public void setGirosComerciales(String girosComerciales) { this.girosComerciales = girosComerciales; }

    public String getCodigo() { return codigoComerciante; }
    public String getTipoPersona() { return tipoComerciante; }
    public String getRazonSocialONombre() { return nombreORazonSocial; }
    public String getMercadoNombre() { return mercadosRelacionados; }
    public int getPuestosAsignados() { return asignacionesVigentes; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ComercianteReporteItem item = new ComercianteReporteItem();

        public Builder id(UUID id) { item.id = id; return this; }
        public Builder codigoComerciante(String codigoComerciante) { item.codigoComerciante = codigoComerciante; return this; }
        public Builder tipoComerciante(String tipoComerciante) { item.tipoComerciante = tipoComerciante; return this; }
        public Builder nombreORazonSocial(String nombreORazonSocial) { item.nombreORazonSocial = nombreORazonSocial; return this; }
        public Builder tipoDocumento(String tipoDocumento) { item.tipoDocumento = tipoDocumento; return this; }
        public Builder numeroDocumento(String numeroDocumento) { item.numeroDocumento = numeroDocumento; return this; }
        public Builder telefono(String telefono) { item.telefono = telefono; return this; }
        public Builder correo(String correo) { item.correo = correo; return this; }
        public Builder estado(String estado) { item.estado = estado; return this; }
        public Builder fechaRegistro(LocalDate fechaRegistro) { item.fechaRegistro = fechaRegistro; return this; }
        public Builder asignacionesVigentes(int asignacionesVigentes) { item.asignacionesVigentes = asignacionesVigentes; return this; }
        public Builder mercadosRelacionados(String mercadosRelacionados) { item.mercadosRelacionados = mercadosRelacionados; return this; }
        public Builder girosComerciales(String girosComerciales) { item.girosComerciales = girosComerciales; return this; }

        public ComercianteReporteItem build() {
            return item;
        }
    }
}
