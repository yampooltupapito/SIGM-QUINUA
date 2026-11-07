package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class SectorRequest {

    @NotNull(message = "El ID del mercado es obligatorio")
    private UUID mercadoId;

    @NotBlank(message = "El código del sector es obligatorio")
    @Size(max = 50, message = "El código no debe exceder 50 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "El código solo debe contener caracteres alfanuméricos, guiones o guiones bajos")
    private String codigo;

    @NotBlank(message = "El nombre del sector es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no debe exceder 500 caracteres")
    private String descripcion;

    @Size(max = 50, message = "El tipo de sector no debe exceder 50 caracteres")
    private String tipoSector;

    @Size(max = 100, message = "La categoría comercial no debe exceder 100 caracteres")
    private String categoriaComercial;

    private String estado;
    private Integer ordenVisual;

    public SectorRequest() {
    }

    public SectorRequest(UUID mercadoId, String codigo, String nombre, String descripcion,
                         String tipoSector, String categoriaComercial, String estado, Integer ordenVisual) {
        this.mercadoId = mercadoId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoSector = tipoSector;
        this.categoriaComercial = categoriaComercial;
        this.estado = estado;
        this.ordenVisual = ordenVisual;
    }

    public static SectorRequestBuilder builder() {
        return new SectorRequestBuilder();
    }

    public UUID getMercadoId() { return mercadoId; }
    public void setMercadoId(UUID mercadoId) { this.mercadoId = mercadoId; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipoSector() { return tipoSector; }
    public void setTipoSector(String tipoSector) { this.tipoSector = tipoSector; }

    public String getCategoriaComercial() { return categoriaComercial; }
    public void setCategoriaComercial(String categoriaComercial) { this.categoriaComercial = categoriaComercial; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getOrdenVisual() { return ordenVisual; }
    public void setOrdenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; }

    public static class SectorRequestBuilder {
        private UUID mercadoId;
        private String codigo;
        private String nombre;
        private String descripcion;
        private String tipoSector;
        private String categoriaComercial;
        private String estado;
        private Integer ordenVisual;

        public SectorRequestBuilder mercadoId(UUID mercadoId) { this.mercadoId = mercadoId; return this; }
        public SectorRequestBuilder codigo(String codigo) { this.codigo = codigo; return this; }
        public SectorRequestBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public SectorRequestBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public SectorRequestBuilder tipoSector(String tipoSector) { this.tipoSector = tipoSector; return this; }
        public SectorRequestBuilder categoriaComercial(String categoriaComercial) { this.categoriaComercial = categoriaComercial; return this; }
        public SectorRequestBuilder estado(String estado) { this.estado = estado; return this; }
        public SectorRequestBuilder ordenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; return this; }

        public SectorRequest build() {
            return new SectorRequest(mercadoId, codigo, nombre, descripcion, tipoSector, categoriaComercial, estado, ordenVisual);
        }
    }
}
