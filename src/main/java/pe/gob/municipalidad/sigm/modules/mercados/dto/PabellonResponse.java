package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class PabellonResponse {
    private UUID id;
    private UUID mercadoId;
    private String mercadoNombre;
    private UUID sectorId;
    private String sectorNombre;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Integer nivel;
    private String estado;
    private Integer ordenVisual;
    private long totalPuestos;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PabellonResponse() {
    }

    public PabellonResponse(UUID id, UUID mercadoId, String mercadoNombre, UUID sectorId,
                            String sectorNombre, String codigo, String nombre, String descripcion,
                            Integer nivel, String estado, Integer ordenVisual, long totalPuestos,
                            Long version, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.mercadoId = mercadoId;
        this.mercadoNombre = mercadoNombre;
        this.sectorId = sectorId;
        this.sectorNombre = sectorNombre;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nivel = nivel;
        this.estado = estado;
        this.ordenVisual = ordenVisual;
        this.totalPuestos = totalPuestos;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PabellonResponseBuilder builder() {
        return new PabellonResponseBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getMercadoId() { return mercadoId; }
    public void setMercadoId(UUID mercadoId) { this.mercadoId = mercadoId; }

    public String getMercadoNombre() { return mercadoNombre; }
    public void setMercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; }

    public UUID getSectorId() { return sectorId; }
    public void setSectorId(UUID sectorId) { this.sectorId = sectorId; }

    public String getSectorNombre() { return sectorNombre; }
    public void setSectorNombre(String sectorNombre) { this.sectorNombre = sectorNombre; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getNivel() { return nivel; }
    public void setNivel(Integer nivel) { this.nivel = nivel; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getOrdenVisual() { return ordenVisual; }
    public void setOrdenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; }

    public long getTotalPuestos() { return totalPuestos; }
    public void setTotalPuestos(long totalPuestos) { this.totalPuestos = totalPuestos; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class PabellonResponseBuilder {
        private UUID id;
        private UUID mercadoId;
        private String mercadoNombre;
        private UUID sectorId;
        private String sectorNombre;
        private String codigo;
        private String nombre;
        private String descripcion;
        private Integer nivel;
        private String estado;
        private Integer ordenVisual;
        private long totalPuestos;
        private Long version;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public PabellonResponseBuilder id(UUID id) { this.id = id; return this; }
        public PabellonResponseBuilder mercadoId(UUID mercadoId) { this.mercadoId = mercadoId; return this; }
        public PabellonResponseBuilder mercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; return this; }
        public PabellonResponseBuilder sectorId(UUID sectorId) { this.sectorId = sectorId; return this; }
        public PabellonResponseBuilder sectorNombre(String sectorNombre) { this.sectorNombre = sectorNombre; return this; }
        public PabellonResponseBuilder codigo(String codigo) { this.codigo = codigo; return this; }
        public PabellonResponseBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public PabellonResponseBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public PabellonResponseBuilder nivel(Integer nivel) { this.nivel = nivel; return this; }
        public PabellonResponseBuilder estado(String estado) { this.estado = estado; return this; }
        public PabellonResponseBuilder ordenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; return this; }
        public PabellonResponseBuilder totalPuestos(long totalPuestos) { this.totalPuestos = totalPuestos; return this; }
        public PabellonResponseBuilder version(Long version) { this.version = version; return this; }
        public PabellonResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PabellonResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public PabellonResponse build() {
            return new PabellonResponse(id, mercadoId, mercadoNombre, sectorId, sectorNombre,
                    codigo, nombre, descripcion, nivel, estado, ordenVisual, totalPuestos,
                    version, createdAt, updatedAt);
        }
    }
}
