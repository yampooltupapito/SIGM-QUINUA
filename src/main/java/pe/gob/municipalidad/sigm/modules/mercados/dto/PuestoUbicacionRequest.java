package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class PuestoUbicacionRequest {

    private UUID nuevoSectorId;
    private UUID nuevoPabellonId;
    private UUID nuevaZonaId;

    @Size(max = 255, message = "La referencia de ubicación no debe exceder 255 caracteres")
    private String nuevaUbicacionReferencia;

    @NotBlank(message = "El motivo del cambio de ubicación es obligatorio")
    @Size(min = 5, max = 500, message = "El motivo debe tener entre 5 y 500 caracteres")
    private String motivo;

    public PuestoUbicacionRequest() {
    }

    public PuestoUbicacionRequest(UUID nuevoSectorId, UUID nuevoPabellonId, UUID nuevaZonaId,
                                  String nuevaUbicacionReferencia, String motivo) {
        this.nuevoSectorId = nuevoSectorId;
        this.nuevoPabellonId = nuevoPabellonId;
        this.nuevaZonaId = nuevaZonaId;
        this.nuevaUbicacionReferencia = nuevaUbicacionReferencia;
        this.motivo = motivo;
    }

    public static PuestoUbicacionRequestBuilder builder() {
        return new PuestoUbicacionRequestBuilder();
    }

    public UUID getNuevoSectorId() { return nuevoSectorId; }
    public void setNuevoSectorId(UUID nuevoSectorId) { this.nuevoSectorId = nuevoSectorId; }

    public UUID getNuevoPabellonId() { return nuevoPabellonId; }
    public void setNuevoPabellonId(UUID nuevoPabellonId) { this.nuevoPabellonId = nuevoPabellonId; }

    public UUID getNuevaZonaId() { return nuevaZonaId; }
    public void setNuevaZonaId(UUID nuevaZonaId) { this.nuevaZonaId = nuevaZonaId; }

    public String getNuevaUbicacionReferencia() { return nuevaUbicacionReferencia; }
    public void setNuevaUbicacionReferencia(String nuevaUbicacionReferencia) { this.nuevaUbicacionReferencia = nuevaUbicacionReferencia; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public static class PuestoUbicacionRequestBuilder {
        private UUID nuevoSectorId;
        private UUID nuevoPabellonId;
        private UUID nuevaZonaId;
        private String nuevaUbicacionReferencia;
        private String motivo;

        public PuestoUbicacionRequestBuilder nuevoSectorId(UUID nuevoSectorId) { this.nuevoSectorId = nuevoSectorId; return this; }
        public PuestoUbicacionRequestBuilder nuevoPabellonId(UUID nuevoPabellonId) { this.nuevoPabellonId = nuevoPabellonId; return this; }
        public PuestoUbicacionRequestBuilder nuevaZonaId(UUID nuevaZonaId) { this.nuevaZonaId = nuevaZonaId; return this; }
        public PuestoUbicacionRequestBuilder nuevaUbicacionReferencia(String nuevaUbicacionReferencia) { this.nuevaUbicacionReferencia = nuevaUbicacionReferencia; return this; }
        public PuestoUbicacionRequestBuilder motivo(String motivo) { this.motivo = motivo; return this; }

        public PuestoUbicacionRequest build() {
            return new PuestoUbicacionRequest(nuevoSectorId, nuevoPabellonId, nuevaZonaId, nuevaUbicacionReferencia, motivo);
        }
    }
}
