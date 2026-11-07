package pe.gob.municipalidad.sigm.modules.mercados.dto;

import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PuestoComercialResponse {

    private UUID id;
    private UUID mercadoId;
    private String mercadoNombre;
    private UUID sectorId;
    private String sectorNombre;
    private UUID pabellonId;
    private String pabellonNombre;
    private UUID zonaId;
    private String zonaNombre;
    private String codigo;
    private String numero;
    private String nombreReferencial;
    private String descripcion;
    private Long tipoPuestoId;
    private String tipoPuestoNombre;
    private BigDecimal areaMetrosCuadrados;
    private BigDecimal anchoMetros;
    private BigDecimal largoMetros;
    private Integer nivel;
    private String ubicacionReferencia;
    private EstadoFisicoPuesto estadoFisico;
    private String estadoFisicoDescripcion;
    private EstadoPuesto estado;
    private String estadoDescripcion;
    private boolean disponible;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PuestoComercialResponse() {
    }

    public PuestoComercialResponse(UUID id, UUID mercadoId, String mercadoNombre, UUID sectorId, String sectorNombre,
                                   UUID pabellonId, String pabellonNombre, UUID zonaId, String zonaNombre,
                                   String codigo, String numero, String nombreReferencial, String descripcion,
                                   Long tipoPuestoId, String tipoPuestoNombre, BigDecimal areaMetrosCuadrados,
                                   BigDecimal anchoMetros, BigDecimal largoMetros, Integer nivel,
                                   String ubicacionReferencia, EstadoFisicoPuesto estadoFisico, String estadoFisicoDescripcion,
                                   EstadoPuesto estado, String estadoDescripcion, boolean disponible, Long version,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.mercadoId = mercadoId;
        this.mercadoNombre = mercadoNombre;
        this.sectorId = sectorId;
        this.sectorNombre = sectorNombre;
        this.pabellonId = pabellonId;
        this.pabellonNombre = pabellonNombre;
        this.zonaId = zonaId;
        this.zonaNombre = zonaNombre;
        this.codigo = codigo;
        this.numero = numero;
        this.nombreReferencial = nombreReferencial;
        this.descripcion = descripcion;
        this.tipoPuestoId = tipoPuestoId;
        this.tipoPuestoNombre = tipoPuestoNombre;
        this.areaMetrosCuadrados = areaMetrosCuadrados;
        this.anchoMetros = anchoMetros;
        this.largoMetros = largoMetros;
        this.nivel = nivel;
        this.ubicacionReferencia = ubicacionReferencia;
        this.estadoFisico = estadoFisico;
        this.estadoFisicoDescripcion = estadoFisicoDescripcion;
        this.estado = estado;
        this.estadoDescripcion = estadoDescripcion;
        this.disponible = disponible;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PuestoComercialResponseBuilder builder() {
        return new PuestoComercialResponseBuilder();
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

    public UUID getPabellonId() { return pabellonId; }
    public void setPabellonId(UUID pabellonId) { this.pabellonId = pabellonId; }

    public String getPabellonNombre() { return pabellonNombre; }
    public void setPabellonNombre(String pabellonNombre) { this.pabellonNombre = pabellonNombre; }

    public UUID getZonaId() { return zonaId; }
    public void setZonaId(UUID zonaId) { this.zonaId = zonaId; }

    public String getZonaNombre() { return zonaNombre; }
    public void setZonaNombre(String zonaNombre) { this.zonaNombre = zonaNombre; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getNombreReferencial() { return nombreReferencial; }
    public void setNombreReferencial(String nombreReferencial) { this.nombreReferencial = nombreReferencial; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Long getTipoPuestoId() { return tipoPuestoId; }
    public void setTipoPuestoId(Long tipoPuestoId) { this.tipoPuestoId = tipoPuestoId; }

    public String getTipoPuestoNombre() { return tipoPuestoNombre; }
    public void setTipoPuestoNombre(String tipoPuestoNombre) { this.tipoPuestoNombre = tipoPuestoNombre; }

    public BigDecimal getAreaMetrosCuadrados() { return areaMetrosCuadrados; }
    public void setAreaMetrosCuadrados(BigDecimal areaMetrosCuadrados) { this.areaMetrosCuadrados = areaMetrosCuadrados; }

    public BigDecimal getAnchoMetros() { return anchoMetros; }
    public void setAnchoMetros(BigDecimal anchoMetros) { this.anchoMetros = anchoMetros; }

    public BigDecimal getLargoMetros() { return largoMetros; }
    public void setLargoMetros(BigDecimal largoMetros) { this.largoMetros = largoMetros; }

    public Integer getNivel() { return nivel; }
    public void setNivel(Integer nivel) { this.nivel = nivel; }

    public String getUbicacionReferencia() { return ubicacionReferencia; }
    public void setUbicacionReferencia(String ubicacionReferencia) { this.ubicacionReferencia = ubicacionReferencia; }

    public EstadoFisicoPuesto getEstadoFisico() { return estadoFisico; }
    public void setEstadoFisico(EstadoFisicoPuesto estadoFisico) { this.estadoFisico = estadoFisico; }

    public String getEstadoFisicoDescripcion() { return estadoFisicoDescripcion; }
    public void setEstadoFisicoDescripcion(String estadoFisicoDescripcion) { this.estadoFisicoDescripcion = estadoFisicoDescripcion; }

    public EstadoPuesto getEstado() { return estado; }
    public void setEstado(EstadoPuesto estado) { this.estado = estado; }

    public String getEstadoDescripcion() { return estadoDescripcion; }
    public void setEstadoDescripcion(String estadoDescripcion) { this.estadoDescripcion = estadoDescripcion; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class PuestoComercialResponseBuilder {
        private UUID id;
        private UUID mercadoId;
        private String mercadoNombre;
        private UUID sectorId;
        private String sectorNombre;
        private UUID pabellonId;
        private String pabellonNombre;
        private UUID zonaId;
        private String zonaNombre;
        private String codigo;
        private String numero;
        private String nombreReferencial;
        private String descripcion;
        private Long tipoPuestoId;
        private String tipoPuestoNombre;
        private BigDecimal areaMetrosCuadrados;
        private BigDecimal anchoMetros;
        private BigDecimal largoMetros;
        private Integer nivel;
        private String ubicacionReferencia;
        private EstadoFisicoPuesto estadoFisico;
        private String estadoFisicoDescripcion;
        private EstadoPuesto estado;
        private String estadoDescripcion;
        private boolean disponible;
        private Long version;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public PuestoComercialResponseBuilder id(UUID id) { this.id = id; return this; }
        public PuestoComercialResponseBuilder mercadoId(UUID mercadoId) { this.mercadoId = mercadoId; return this; }
        public PuestoComercialResponseBuilder mercadoNombre(String mercadoNombre) { this.mercadoNombre = mercadoNombre; return this; }
        public PuestoComercialResponseBuilder sectorId(UUID sectorId) { this.sectorId = sectorId; return this; }
        public PuestoComercialResponseBuilder sectorNombre(String sectorNombre) { this.sectorNombre = sectorNombre; return this; }
        public PuestoComercialResponseBuilder pabellonId(UUID pabellonId) { this.pabellonId = pabellonId; return this; }
        public PuestoComercialResponseBuilder pabellonNombre(String pabellonNombre) { this.pabellonNombre = pabellonNombre; return this; }
        public PuestoComercialResponseBuilder zonaId(UUID zonaId) { this.zonaId = zonaId; return this; }
        public PuestoComercialResponseBuilder zonaNombre(String zonaNombre) { this.zonaNombre = zonaNombre; return this; }
        public PuestoComercialResponseBuilder codigo(String codigo) { this.codigo = codigo; return this; }
        public PuestoComercialResponseBuilder numero(String numero) { this.numero = numero; return this; }
        public PuestoComercialResponseBuilder nombreReferencial(String nombreReferencial) { this.nombreReferencial = nombreReferencial; return this; }
        public PuestoComercialResponseBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public PuestoComercialResponseBuilder tipoPuestoId(Long tipoPuestoId) { this.tipoPuestoId = tipoPuestoId; return this; }
        public PuestoComercialResponseBuilder tipoPuestoNombre(String tipoPuestoNombre) { this.tipoPuestoNombre = tipoPuestoNombre; return this; }
        public PuestoComercialResponseBuilder areaMetrosCuadrados(BigDecimal areaMetrosCuadrados) { this.areaMetrosCuadrados = areaMetrosCuadrados; return this; }
        public PuestoComercialResponseBuilder anchoMetros(BigDecimal anchoMetros) { this.anchoMetros = anchoMetros; return this; }
        public PuestoComercialResponseBuilder largoMetros(BigDecimal largoMetros) { this.largoMetros = largoMetros; return this; }
        public PuestoComercialResponseBuilder nivel(Integer nivel) { this.nivel = nivel; return this; }
        public PuestoComercialResponseBuilder ubicacionReferencia(String ubicacionReferencia) { this.ubicacionReferencia = ubicacionReferencia; return this; }
        public PuestoComercialResponseBuilder estadoFisico(EstadoFisicoPuesto estadoFisico) { this.estadoFisico = estadoFisico; return this; }
        public PuestoComercialResponseBuilder estadoFisicoDescripcion(String estadoFisicoDescripcion) { this.estadoFisicoDescripcion = estadoFisicoDescripcion; return this; }
        public PuestoComercialResponseBuilder estado(EstadoPuesto estado) { this.estado = estado; return this; }
        public PuestoComercialResponseBuilder estadoDescripcion(String estadoDescripcion) { this.estadoDescripcion = estadoDescripcion; return this; }
        public PuestoComercialResponseBuilder disponible(boolean disponible) { this.disponible = disponible; return this; }
        public PuestoComercialResponseBuilder version(Long version) { this.version = version; return this; }
        public PuestoComercialResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PuestoComercialResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public PuestoComercialResponse build() {
            return new PuestoComercialResponse(id, mercadoId, mercadoNombre, sectorId, sectorNombre, pabellonId,
                    pabellonNombre, zonaId, zonaNombre, codigo, numero, nombreReferencial, descripcion, tipoPuestoId,
                    tipoPuestoNombre, areaMetrosCuadrados, anchoMetros, largoMetros, nivel, ubicacionReferencia,
                    estadoFisico, estadoFisicoDescripcion, estado, estadoDescripcion, disponible, version, createdAt, updatedAt);
        }
    }
}
