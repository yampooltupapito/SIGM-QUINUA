package pe.gob.municipalidad.sigm.modules.mercados.dto;

import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class MercadoResponse {
    private UUID id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String direccion;
    private String referenciaUbicacion;
    private String distrito;
    private String provincia;
    private String departamento;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private Integer capacidadReferencial;
    private EstadoMercado estado;
    private Long oficinaAdministradoraId;
    private String oficinaAdministradoraNombre;
    private LocalDate fechaRegistro;
    private long totalSectores;
    private long totalPuestos;
    private long puestosDisponibles;
    private long puestosOcupados;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MercadoResponse() {
    }

    public MercadoResponse(UUID id, String codigo, String nombre, String descripcion, String direccion,
                           String referenciaUbicacion, String distrito, String provincia, String departamento,
                           BigDecimal latitud, BigDecimal longitud, Integer capacidadReferencial,
                           EstadoMercado estado, Long oficinaAdministradoraId, String oficinaAdministradoraNombre,
                           LocalDate fechaRegistro, long totalSectores, long totalPuestos,
                           long puestosDisponibles, long puestosOcupados, Long version,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.referenciaUbicacion = referenciaUbicacion;
        this.distrito = distrito;
        this.provincia = provincia;
        this.departamento = departamento;
        this.latitud = latitud;
        this.longitud = longitud;
        this.capacidadReferencial = capacidadReferencial;
        this.estado = estado;
        this.oficinaAdministradoraId = oficinaAdministradoraId;
        this.oficinaAdministradoraNombre = oficinaAdministradoraNombre;
        this.fechaRegistro = fechaRegistro;
        this.totalSectores = totalSectores;
        this.totalPuestos = totalPuestos;
        this.puestosDisponibles = puestosDisponibles;
        this.puestosOcupados = puestosOcupados;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MercadoResponseBuilder builder() {
        return new MercadoResponseBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getReferenciaUbicacion() { return referenciaUbicacion; }
    public void setReferenciaUbicacion(String referenciaUbicacion) { this.referenciaUbicacion = referenciaUbicacion; }

    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal latitud) { this.latitud = latitud; }

    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal longitud) { this.longitud = longitud; }

    public Integer getCapacidadReferencial() { return capacidadReferencial; }
    public void setCapacidadReferencial(Integer capacidadReferencial) { this.capacidadReferencial = capacidadReferencial; }

    public EstadoMercado getEstado() { return estado; }
    public void setEstado(EstadoMercado estado) { this.estado = estado; }

    public Long getOficinaAdministradoraId() { return oficinaAdministradoraId; }
    public void setOficinaAdministradoraId(Long oficinaAdministradoraId) { this.oficinaAdministradoraId = oficinaAdministradoraId; }

    public String getOficinaAdministradoraNombre() { return oficinaAdministradoraNombre; }
    public void setOficinaAdministradoraNombre(String oficinaAdministradoraNombre) { this.oficinaAdministradoraNombre = oficinaAdministradoraNombre; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public long getTotalSectores() { return totalSectores; }
    public void setTotalSectores(long totalSectores) { this.totalSectores = totalSectores; }

    public long getTotalPuestos() { return totalPuestos; }
    public void setTotalPuestos(long totalPuestos) { this.totalPuestos = totalPuestos; }

    public long getPuestosDisponibles() { return puestosDisponibles; }
    public void setPuestosDisponibles(long puestosDisponibles) { this.puestosDisponibles = puestosDisponibles; }

    public long getPuestosOcupados() { return puestosOcupados; }
    public void setPuestosOcupados(long puestosOcupados) { this.puestosOcupados = puestosOcupados; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class MercadoResponseBuilder {
        private UUID id;
        private String codigo;
        private String nombre;
        private String descripcion;
        private String direccion;
        private String referenciaUbicacion;
        private String distrito;
        private String provincia;
        private String departamento;
        private BigDecimal latitud;
        private BigDecimal longitud;
        private Integer capacidadReferencial;
        private EstadoMercado estado;
        private Long oficinaAdministradoraId;
        private String oficinaAdministradoraNombre;
        private LocalDate fechaRegistro;
        private long totalSectores;
        private long totalPuestos;
        private long puestosDisponibles;
        private long puestosOcupados;
        private Long version;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public MercadoResponseBuilder id(UUID id) { this.id = id; return this; }
        public MercadoResponseBuilder codigo(String codigo) { this.codigo = codigo; return this; }
        public MercadoResponseBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public MercadoResponseBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public MercadoResponseBuilder direccion(String direccion) { this.direccion = direccion; return this; }
        public MercadoResponseBuilder referenciaUbicacion(String referenciaUbicacion) { this.referenciaUbicacion = referenciaUbicacion; return this; }
        public MercadoResponseBuilder distrito(String distrito) { this.distrito = distrito; return this; }
        public MercadoResponseBuilder provincia(String provincia) { this.provincia = provincia; return this; }
        public MercadoResponseBuilder departamento(String departamento) { this.departamento = departamento; return this; }
        public MercadoResponseBuilder latitud(BigDecimal latitud) { this.latitud = latitud; return this; }
        public MercadoResponseBuilder longitud(BigDecimal longitud) { this.longitud = longitud; return this; }
        public MercadoResponseBuilder capacidadReferencial(Integer capacidadReferencial) { this.capacidadReferencial = capacidadReferencial; return this; }
        public MercadoResponseBuilder estado(EstadoMercado estado) { this.estado = estado; return this; }
        public MercadoResponseBuilder oficinaAdministradoraId(Long oficinaAdministradoraId) { this.oficinaAdministradoraId = oficinaAdministradoraId; return this; }
        public MercadoResponseBuilder oficinaAdministradoraNombre(String oficinaAdministradoraNombre) { this.oficinaAdministradoraNombre = oficinaAdministradoraNombre; return this; }
        public MercadoResponseBuilder fechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; return this; }
        public MercadoResponseBuilder totalSectores(long totalSectores) { this.totalSectores = totalSectores; return this; }
        public MercadoResponseBuilder totalPuestos(long totalPuestos) { this.totalPuestos = totalPuestos; return this; }
        public MercadoResponseBuilder puestosDisponibles(long puestosDisponibles) { this.puestosDisponibles = puestosDisponibles; return this; }
        public MercadoResponseBuilder puestosOcupados(long puestosOcupados) { this.puestosOcupados = puestosOcupados; return this; }
        public MercadoResponseBuilder version(Long version) { this.version = version; return this; }
        public MercadoResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public MercadoResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public MercadoResponse build() {
            return new MercadoResponse(id, codigo, nombre, descripcion, direccion, referenciaUbicacion,
                    distrito, provincia, departamento, latitud, longitud, capacidadReferencial, estado,
                    oficinaAdministradoraId, oficinaAdministradoraNombre, fechaRegistro, totalSectores,
                    totalPuestos, puestosDisponibles, puestosOcupados, version, createdAt, updatedAt);
        }
    }
}
