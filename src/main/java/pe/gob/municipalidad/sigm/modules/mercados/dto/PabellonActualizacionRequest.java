package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class PabellonActualizacionRequest {

    private UUID sectorId;

    @NotBlank(message = "El nombre del pabellón es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no debe exceder 500 caracteres")
    private String descripcion;

    @Min(value = 1, message = "El nivel o piso debe ser mayor o igual a 1")
    private Integer nivel;

    private Integer ordenVisual;

    @NotNull(message = "La versión es obligatoria para el control de concurrencia")
    private Long version;

    public PabellonActualizacionRequest() {
    }

    public PabellonActualizacionRequest(UUID sectorId, String nombre, String descripcion,
                                        Integer nivel, Integer ordenVisual, Long version) {
        this.sectorId = sectorId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nivel = nivel != null ? nivel : 1;
        this.ordenVisual = ordenVisual;
        this.version = version;
    }

    public static PabellonActualizacionRequestBuilder builder() {
        return new PabellonActualizacionRequestBuilder();
    }

    public UUID getSectorId() { return sectorId; }
    public void setSectorId(UUID sectorId) { this.sectorId = sectorId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getNivel() { return nivel; }
    public void setNivel(Integer nivel) { this.nivel = nivel; }

    public Integer getOrdenVisual() { return ordenVisual; }
    public void setOrdenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public static class PabellonActualizacionRequestBuilder {
        private UUID sectorId;
        private String nombre;
        private String descripcion;
        private Integer nivel = 1;
        private Integer ordenVisual;
        private Long version;

        public PabellonActualizacionRequestBuilder sectorId(UUID sectorId) { this.sectorId = sectorId; return this; }
        public PabellonActualizacionRequestBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public PabellonActualizacionRequestBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public PabellonActualizacionRequestBuilder nivel(Integer nivel) { this.nivel = nivel; return this; }
        public PabellonActualizacionRequestBuilder ordenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; return this; }
        public PabellonActualizacionRequestBuilder version(Long version) { this.version = version; return this; }

        public PabellonActualizacionRequest build() {
            return new PabellonActualizacionRequest(sectorId, nombre, descripcion, nivel, ordenVisual, version);
        }
    }
}
