package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.time.LocalDateTime;

public class HistorialUbicacionResponse {

    private Long id;
    private String sectorAnteriorNombre;
    private String sectorNuevoNombre;
    private String pabellonAnteriorNombre;
    private String pabellonNuevoNombre;
    private String zonaAnteriorNombre;
    private String zonaNuevaNombre;
    private String ubicacionRefAnterior;
    private String ubicacionRefNueva;
    private String motivo;
    private String usuarioResponsable;
    private LocalDateTime fechaCambio;

    public HistorialUbicacionResponse() {
    }

    public HistorialUbicacionResponse(Long id, String sectorAnteriorNombre, String sectorNuevoNombre,
                                      String pabellonAnteriorNombre, String pabellonNuevoNombre,
                                      String zonaAnteriorNombre, String zonaNuevaNombre,
                                      String ubicacionRefAnterior, String ubicacionRefNueva,
                                      String motivo, String usuarioResponsable, LocalDateTime fechaCambio) {
        this.id = id;
        this.sectorAnteriorNombre = sectorAnteriorNombre;
        this.sectorNuevoNombre = sectorNuevoNombre;
        this.pabellonAnteriorNombre = pabellonAnteriorNombre;
        this.pabellonNuevoNombre = pabellonNuevoNombre;
        this.zonaAnteriorNombre = zonaAnteriorNombre;
        this.zonaNuevaNombre = zonaNuevaNombre;
        this.ubicacionRefAnterior = ubicacionRefAnterior;
        this.ubicacionRefNueva = ubicacionRefNueva;
        this.motivo = motivo;
        this.usuarioResponsable = usuarioResponsable;
        this.fechaCambio = fechaCambio;
    }

    public static HistorialUbicacionResponseBuilder builder() {
        return new HistorialUbicacionResponseBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSectorAnteriorNombre() { return sectorAnteriorNombre; }
    public void setSectorAnteriorNombre(String sectorAnteriorNombre) { this.sectorAnteriorNombre = sectorAnteriorNombre; }

    public String getSectorNuevoNombre() { return sectorNuevoNombre; }
    public void setSectorNuevoNombre(String sectorNuevoNombre) { this.sectorNuevoNombre = sectorNuevoNombre; }

    public String getPabellonAnteriorNombre() { return pabellonAnteriorNombre; }
    public void setPabellonAnteriorNombre(String pabellonAnteriorNombre) { this.pabellonAnteriorNombre = pabellonAnteriorNombre; }

    public String getPabellonNuevoNombre() { return pabellonNuevoNombre; }
    public void setPabellonNuevoNombre(String pabellonNuevoNombre) { this.pabellonNuevoNombre = pabellonNuevoNombre; }

    public String getZonaAnteriorNombre() { return zonaAnteriorNombre; }
    public void setZonaAnteriorNombre(String zonaAnteriorNombre) { this.zonaAnteriorNombre = zonaAnteriorNombre; }

    public String getZonaNuevaNombre() { return zonaNuevaNombre; }
    public void setZonaNuevaNombre(String zonaNuevaNombre) { this.zonaNuevaNombre = zonaNuevaNombre; }

    public String getUbicacionRefAnterior() { return ubicacionRefAnterior; }
    public void setUbicacionRefAnterior(String ubicacionRefAnterior) { this.ubicacionRefAnterior = ubicacionRefAnterior; }

    public String getUbicacionRefNueva() { return ubicacionRefNueva; }
    public void setUbicacionRefNueva(String ubicacionRefNueva) { this.ubicacionRefNueva = ubicacionRefNueva; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getUsuarioResponsable() { return usuarioResponsable; }
    public void setUsuarioResponsable(String usuarioResponsable) { this.usuarioResponsable = usuarioResponsable; }

    public LocalDateTime getFechaCambio() { return fechaCambio; }
    public void setFechaCambio(LocalDateTime fechaCambio) { this.fechaCambio = fechaCambio; }

    public static class HistorialUbicacionResponseBuilder {
        private Long id;
        private String sectorAnteriorNombre;
        private String sectorNuevoNombre;
        private String pabellonAnteriorNombre;
        private String pabellonNuevoNombre;
        private String zonaAnteriorNombre;
        private String zonaNuevaNombre;
        private String ubicacionRefAnterior;
        private String ubicacionRefNueva;
        private String motivo;
        private String usuarioResponsable;
        private LocalDateTime fechaCambio;

        public HistorialUbicacionResponseBuilder id(Long id) { this.id = id; return this; }
        public HistorialUbicacionResponseBuilder sectorAnteriorNombre(String sectorAnteriorNombre) { this.sectorAnteriorNombre = sectorAnteriorNombre; return this; }
        public HistorialUbicacionResponseBuilder sectorNuevoNombre(String sectorNuevoNombre) { this.sectorNuevoNombre = sectorNuevoNombre; return this; }
        public HistorialUbicacionResponseBuilder pabellonAnteriorNombre(String pabellonAnteriorNombre) { this.pabellonAnteriorNombre = pabellonAnteriorNombre; return this; }
        public HistorialUbicacionResponseBuilder pabellonNuevoNombre(String pabellonNuevoNombre) { this.pabellonNuevoNombre = pabellonNuevoNombre; return this; }
        public HistorialUbicacionResponseBuilder zonaAnteriorNombre(String zonaAnteriorNombre) { this.zonaAnteriorNombre = zonaAnteriorNombre; return this; }
        public HistorialUbicacionResponseBuilder zonaNuevaNombre(String zonaNuevaNombre) { this.zonaNuevaNombre = zonaNuevaNombre; return this; }
        public HistorialUbicacionResponseBuilder ubicacionRefAnterior(String ubicacionRefAnterior) { this.ubicacionRefAnterior = ubicacionRefAnterior; return this; }
        public HistorialUbicacionResponseBuilder ubicacionRefNueva(String ubicacionRefNueva) { this.ubicacionRefNueva = ubicacionRefNueva; return this; }
        public HistorialUbicacionResponseBuilder motivo(String motivo) { this.motivo = motivo; return this; }
        public HistorialUbicacionResponseBuilder usuarioResponsable(String usuarioResponsable) { this.usuarioResponsable = usuarioResponsable; return this; }
        public HistorialUbicacionResponseBuilder fechaCambio(LocalDateTime fechaCambio) { this.fechaCambio = fechaCambio; return this; }

        public HistorialUbicacionResponse build() {
            return new HistorialUbicacionResponse(id, sectorAnteriorNombre, sectorNuevoNombre, pabellonAnteriorNombre,
                    pabellonNuevoNombre, zonaAnteriorNombre, zonaNuevaNombre, ubicacionRefAnterior, ubicacionRefNueva,
                    motivo, usuarioResponsable, fechaCambio);
        }
    }
}
