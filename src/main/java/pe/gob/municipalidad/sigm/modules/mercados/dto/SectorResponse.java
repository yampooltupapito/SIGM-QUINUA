package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class SectorResponse {
    private UUID id;
    private UUID mercadoId;
    private String mercadoNombre;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String tipoSector;
    private String categoriaComercial;
    private String estado;
    private Integer ordenVisual;
    private long totalPabellones;
    private long totalPuestos;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SectorResponse() {
    }

    public SectorResponse(UUID id, UUID mercadoId, String mercadoNombre, String codigo, String nombre,
                          String descripcion, String tipoSector, String categoriaComercial, String estado,
                          Integer ordenVisual, long totalPabellones, long totalPuestos, Long version,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.mercadoId = mercadoId;
        this.mercadoNombre = mercadoNombre;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoSector = tipoSector;
        this.categoriaComercial = categoriaComercial;
        this.estado = estado;
        this.ordenVisual = ordenVisual;
        this.totalPabellones = totalPabellones;
        this.totalPuestos = totalPuestos;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SectorResponseBuilder builder() {
        return new SectorResponseBuilder();
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

    public String getTipoSector() { return tipoSector; }
    public void setTipoSector(String tipoSector) { this.tipoSector = tipoSector; }

    public String getCategoriaComercial() { return categoriaComercial; }
    public void setCategoriaComercial(String categoriaComercial) { this.categoriaComercial = categoriaComercial; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getOrdenVisual() { return ordenVisual; }
    public void setOrdenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; }

    public long getTotalPabellones() { return totalPabellones; }
    public void setTotalPabellones(long totalPabellones) { this.totalPabellones = totalPabellones; }

    public long getTotalPuestos() { return totalPuestos; }
    public void setTotalPuestos(long totalPuestos) { this.totalPuestos = totalPuestos; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class SectorResponseBuilder {
        private UUID id;
        private UUID mercadoId;
        private String mercadoNombre;
        private String codigo;
        private String nombre;
        private String descripcion;
        private String tipoSector;
        private String categoriaComercial;
        private String estado;
        private Integer ordenVisual;
        private long totalPabellones;
        private long totalPuestos;
        private Long version;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public SectorResponseBuilder id(UUID id) { this.id = id; return this; }
        public SectorResponseBuilder mercadoId(UUID mercadoId) { this.mercadoId = mercadoId; return this; }
        public SectorResponseBuilder mercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; return this; }
        public SectorResponseBuilder codigo(String codigo) { this.codigo = codigo; return this; }
        public SectorResponseBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public SectorResponseBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public SectorResponseBuilder tipoSector(String tipoSector) { this.tipoSector = tipoSector; return this; }
        public SectorResponseBuilder categoriaComercial(String categoriaComercial) { this.categoriaComercial = categoriaComercial; return this; }
        public SectorResponseBuilder estado(String estado) { this.estado = estado; return this; }
        public SectorResponseBuilder ordenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; return this; }
        public SectorResponseBuilder totalPabellones(long totalPabellones) { this.totalPabellones = totalPabellones; return this; }
        public SectorResponseBuilder totalPuestos(long totalPuestos) { this.totalPuestos = totalPuestos; return this; }
        public SectorResponseBuilder version(Long version) { this.version = version; return this; }
        public SectorResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public SectorResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public SectorResponse build() {
            return new SectorResponse(id, mercadoId, mercadoNombre, codigo, nombre, descripcion,
                    tipoSector, categoriaComercial, estado, ordenVisual, totalPabellones, totalPuestos,
                    version, createdAt, updatedAt);
        }
    }
}
