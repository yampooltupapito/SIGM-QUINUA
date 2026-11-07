package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoZona;

import java.util.UUID;

public class ZonaRequest {

    @NotNull(message = "El ID del mercado es obligatorio")
    private UUID mercadoId;

    @NotBlank(message = "El código de la zona es obligatorio")
    @Size(max = 50, message = "El código no debe exceder 50 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "El código solo debe contener caracteres alfanuméricos, guiones o guiones bajos")
    private String codigo;

    @NotBlank(message = "El nombre de la zona es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no debe exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "El tipo de zona es obligatorio")
    private TipoZona tipoZona;

    private Boolean esAsignable;
    private EstadoMercado estado;
    private Integer ordenVisual;
    private Long version;

    public ZonaRequest() {
    }

    public ZonaRequest(UUID mercadoId, String codigo, String nombre, String descripcion,
                       TipoZona tipoZona, Boolean esAsignable, EstadoMercado estado,
                       Integer ordenVisual, Long version) {
        this.mercadoId = mercadoId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoZona = tipoZona;
        this.esAsignable = esAsignable != null ? esAsignable : (tipoZona != null ? tipoZona.isAsignableDefecto() : true);
        this.estado = estado != null ? estado : EstadoMercado.ACTIVO;
        this.ordenVisual = ordenVisual != null ? ordenVisual : 0;
        this.version = version;
    }

    public static ZonaRequestBuilder builder() {
        return new ZonaRequestBuilder();
    }

    public UUID getMercadoId() { return mercadoId; }
    public void setMercadoId(UUID mercadoId) { this.mercadoId = mercadoId; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public TipoZona getTipoZona() { return tipoZona; }
    public void setTipoZona(TipoZona tipoZona) { this.tipoZona = tipoZona; }

    public Boolean getEsAsignable() { return esAsignable; }
    public void setEsAsignable(Boolean esAsignable) { this.esAsignable = esAsignable; }

    public EstadoMercado getEstado() { return estado; }
    public void setEstado(EstadoMercado estado) { this.estado = estado; }

    public Integer getOrdenVisual() { return ordenVisual; }
    public void setOrdenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public static class ZonaRequestBuilder {
        private UUID mercadoId;
        private String codigo;
        private String nombre;
        private String descripcion;
        private TipoZona tipoZona;
        private Boolean esAsignable;
        private EstadoMercado estado;
        private Integer ordenVisual;
        private Long version;

        public ZonaRequestBuilder mercadoId(UUID mercadoId) { this.mercadoId = mercadoId; return this; }
        public ZonaRequestBuilder codigo(String codigo) { this.codigo = codigo; return this; }
        public ZonaRequestBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public ZonaRequestBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public ZonaRequestBuilder tipoZona(TipoZona tipoZona) { this.tipoZona = tipoZona; return this; }
        public ZonaRequestBuilder esAsignable(Boolean esAsignable) { this.esAsignable = esAsignable; return this; }
        public ZonaRequestBuilder estado(EstadoMercado estado) { this.estado = estado; return this; }
        public ZonaRequestBuilder ordenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; return this; }
        public ZonaRequestBuilder version(Long version) { this.version = version; return this; }

        public ZonaRequest build() {
            return new ZonaRequest(mercadoId, codigo, nombre, descripcion, tipoZona, esAsignable, estado, ordenVisual, version);
        }
    }
}
