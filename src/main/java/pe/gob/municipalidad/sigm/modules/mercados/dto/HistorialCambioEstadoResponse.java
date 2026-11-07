package pe.gob.municipalidad.sigm.modules.mercados.dto;

import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;

import java.time.OffsetDateTime;
import java.util.UUID;

public class HistorialCambioEstadoResponse {
    private UUID id;
    private EstadoMercado estadoAnterior;
    private EstadoMercado estadoNuevo;
    private String motivo;
    private String usuarioResponsable;
    private OffsetDateTime fechaCambio;

    public HistorialCambioEstadoResponse() {
    }

    public HistorialCambioEstadoResponse(UUID id, EstadoMercado estadoAnterior, EstadoMercado estadoNuevo,
                                        String motivo, String usuarioResponsable, OffsetDateTime fechaCambio) {
        this.id = id;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.motivo = motivo;
        this.usuarioResponsable = usuarioResponsable;
        this.fechaCambio = fechaCambio;
    }

    public static HistorialCambioEstadoResponseBuilder builder() {
        return new HistorialCambioEstadoResponseBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public EstadoMercado getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(EstadoMercado estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public EstadoMercado getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(EstadoMercado estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getUsuarioResponsable() { return usuarioResponsable; }
    public void setUsuarioResponsable(String usuarioResponsable) { this.usuarioResponsable = usuarioResponsable; }

    public OffsetDateTime getFechaCambio() { return fechaCambio; }
    public void setFechaCambio(OffsetDateTime fechaCambio) { this.fechaCambio = fechaCambio; }

    public static class HistorialCambioEstadoResponseBuilder {
        private UUID id;
        private EstadoMercado estadoAnterior;
        private EstadoMercado estadoNuevo;
        private String motivo;
        private String usuarioResponsable;
        private OffsetDateTime fechaCambio;

        public HistorialCambioEstadoResponseBuilder id(UUID id) { this.id = id; return this; }
        public HistorialCambioEstadoResponseBuilder estadoAnterior(EstadoMercado estadoAnterior) { this.estadoAnterior = estadoAnterior; return this; }
        public HistorialCambioEstadoResponseBuilder estadoNuevo(EstadoMercado estadoNuevo) { this.estadoNuevo = estadoNuevo; return this; }
        public HistorialCambioEstadoResponseBuilder motivo(String motivo) { this.motivo = motivo; return this; }
        public HistorialCambioEstadoResponseBuilder usuarioResponsable(String usuarioResponsable) { this.usuarioResponsable = usuarioResponsable; return this; }
        public HistorialCambioEstadoResponseBuilder fechaCambio(OffsetDateTime fechaCambio) { this.fechaCambio = fechaCambio; return this; }

        public HistorialCambioEstadoResponse build() {
            return new HistorialCambioEstadoResponse(id, estadoAnterior, estadoNuevo, motivo, usuarioResponsable, fechaCambio);
        }
    }
}
