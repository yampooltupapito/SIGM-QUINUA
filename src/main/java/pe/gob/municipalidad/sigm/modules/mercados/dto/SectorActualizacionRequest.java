package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SectorActualizacionRequest {

    @NotBlank(message = "El nombre del sector es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no debe exceder 500 caracteres")
    private String descripcion;

    @Size(max = 50, message = "El tipo de sector no debe exceder 50 caracteres")
    private String tipoSector;

    @Size(max = 100, message = "La categoría comercial no debe exceder 100 caracteres")
    private String categoriaComercial;

    private Integer ordenVisual;

    @NotNull(message = "La versión es obligatoria para el control de concurrencia")
    private Long version;

    public SectorActualizacionRequest() {
    }

    public SectorActualizacionRequest(String nombre, String descripcion, String tipoSector,
                                      String categoriaComercial, Integer ordenVisual, Long version) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoSector = tipoSector;
        this.categoriaComercial = categoriaComercial;
        this.ordenVisual = ordenVisual;
        this.version = version;
    }

    public static SectorActualizacionRequestBuilder builder() {
        return new SectorActualizacionRequestBuilder();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipoSector() { return tipoSector; }
    public void setTipoSector(String tipoSector) { this.tipoSector = tipoSector; }

    public String getCategoriaComercial() { return categoriaComercial; }
    public void setCategoriaComercial(String categoriaComercial) { this.categoriaComercial = categoriaComercial; }

    public Integer getOrdenVisual() { return ordenVisual; }
    public void setOrdenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public static class SectorActualizacionRequestBuilder {
        private String nombre;
        private String descripcion;
        private String tipoSector;
        private String categoriaComercial;
        private Integer ordenVisual;
        private Long version;

        public SectorActualizacionRequestBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public SectorActualizacionRequestBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public SectorActualizacionRequestBuilder tipoSector(String tipoSector) { this.tipoSector = tipoSector; return this; }
        public SectorActualizacionRequestBuilder categoriaComercial(String categoriaComercial) { this.categoriaComercial = categoriaComercial; return this; }
        public SectorActualizacionRequestBuilder ordenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; return this; }
        public SectorActualizacionRequestBuilder version(Long version) { this.version = version; return this; }

        public SectorActualizacionRequest build() {
            return new SectorActualizacionRequest(nombre, descripcion, tipoSector, categoriaComercial, ordenVisual, version);
        }
    }
}
