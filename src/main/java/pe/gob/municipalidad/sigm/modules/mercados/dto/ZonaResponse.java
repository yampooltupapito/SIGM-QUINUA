package pe.gob.municipalidad.sigm.modules.mercados.dto;

import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoZona;

import java.time.LocalDateTime;
import java.util.UUID;

public class ZonaResponse {
    private UUID id;
    private UUID mercadoId;
    private String mercadoNombre;
    private String codigo;
    private String nombre;
    private String descripcion;
    private TipoZona tipoZona;
    private String tipoZonaDescripcion;
    private Boolean esAsignable;
    private EstadoMercado estado;
    private Integer ordenVisual;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ZonaResponse() {
    }

    public ZonaResponse(UUID id, UUID mercadoId, String mercadoNombre, String codigo, String nombre,
                        String descripcion, TipoZona tipoZona, String tipoZonaDescripcion,
                        Boolean esAsignable, EstadoMercado estado, Integer ordenVisual, Long version,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.mercadoId = mercadoId;
        this.mercadoNombre = mercadoNombre;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoZona = tipoZona;
        this.tipoZonaDescripcion = tipoZonaDescripcion != null ? tipoZonaDescripcion : (tipoZona != null ? tipoZona.getDescripcion() : null);
        this.esAsignable = esAsignable;
        this.estado = estado;
        this.ordenVisual = ordenVisual;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ZonaResponseBuilder builder() {
        return new ZonaResponseBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getMercadoId() { return mercadoId; }
    public void setMercadoId(UUID mercadoId) { this.mercadoId = mercadoId; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public TipoZona getTipoZona() { return tipoZona; }
    public void setTipoZona(TipoZona tipoZona) {
        this.tipoZona = tipoZona;
        if (tipoZona != null) {
            this.tipoZonaDescripcion = tipoZona.getDescripcion();
        }
    }

    public String getTipoZonaDescripcion() { return tipoZonaDescripcion; }
    public void setTipoZonaDescripcion(String tipoZonaDescripcion) { this.tipoZonaDescripcion = tipoZonaDescripcion; }

    public Boolean getEsAsignable() { return esAsignable; }
    public void setEsAsignable(Boolean esAsignable) { this.esAsignable = esAsignable; }

    public EstadoMercado getEstado() { return estado; }
    public void setEstado(EstadoMercado estado) { this.estado = estado; }

    public Integer getOrdenVisual() { return ordenVisual; }
    public void setOrdenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class ZonaResponseBuilder {
        private UUID id;
        private UUID mercadoId;
        private String mercadoNombre;
        private String codigo;
        private String nombre;
        private String descripcion;
        private TipoZona tipoZona;
        private String tipoZonaDescripcion;
        private Boolean esAsignable;
        private EstadoMercado estado;
        private Integer ordenVisual;
        private Long version;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public ZonaResponseBuilder id(UUID id) { this.id = id; return this; }
        public ZonaResponseBuilder mercadoId(UUID mercadoId) { this.mercadoId = mercadoId; return this; }
        public ZonaResponseBuilder mercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; return this; }
        public ZonaResponseBuilder codigo(String codigo) { this.codigo = codigo; return this; }
        public ZonaResponseBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public ZonaResponseBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public ZonaResponseBuilder tipoZona(TipoZona tipoZona) {
            this.tipoZona = tipoZona;
            if (tipoZona != null) {
                this.tipoZonaDescripcion = tipoZona.getDescripcion();
            }
            return this;
        }
        public ZonaResponseBuilder tipoZonaDescripcion(String tipoZonaDescripcion) { this.tipoZonaDescripcion = tipoZonaDescripcion; return this; }
        public ZonaResponseBuilder esAsignable(Boolean esAsignable) { this.esAsignable = esAsignable; return this; }
        public ZonaResponseBuilder estado(EstadoMercado estado) { this.estado = estado; return this; }
        public ZonaResponseBuilder ordenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; return this; }
        public ZonaResponseBuilder version(Long version) { this.version = version; return this; }
        public ZonaResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ZonaResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public ZonaResponse build() {
            return new ZonaResponse(id, mercadoId, mercadoNombre, codigo, nombre, descripcion,
                    tipoZona, tipoZonaDescripcion, esAsignable, estado, ordenVisual, version,
                    createdAt, updatedAt);
        }
    }
}
