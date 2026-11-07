package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;

import java.math.BigDecimal;
import java.util.UUID;

public class PuestoComercialCreateRequest {

    @NotNull(message = "El ID del mercado es obligatorio")
    private UUID mercadoId;

    private UUID sectorId;
    private UUID pabellonId;
    private UUID zonaId;

    @NotNull(message = "El tipo de puesto es obligatorio")
    private Long tipoPuestoId;

    @NotBlank(message = "El código del puesto es obligatorio")
    @Size(max = 50, message = "El código no debe exceder 50 caracteres")
    private String codigo;

    @NotBlank(message = "El número del puesto es obligatorio")
    @Size(max = 30, message = "El número no debe exceder 30 caracteres")
    private String numero;

    @Size(max = 150, message = "El nombre referencial no debe exceder 150 caracteres")
    private String nombreReferencial;

    @Size(max = 500, message = "La descripción no debe exceder 500 caracteres")
    private String descripcion;

    @DecimalMin(value = "0.00", message = "El área debe ser mayor o igual a 0")
    private BigDecimal areaMetrosCuadrados;

    @DecimalMin(value = "0.00", message = "El ancho debe ser mayor o igual a 0")
    private BigDecimal anchoMetros;

    @DecimalMin(value = "0.00", message = "El largo debe ser mayor o igual a 0")
    private BigDecimal largoMetros;

    @Min(value = 1, message = "El nivel debe ser como mínimo 1")
    private Integer nivel = 1;

    @Size(max = 255, message = "La referencia de ubicación no debe exceder 255 caracteres")
    private String ubicacionReferencia;

    private EstadoFisicoPuesto estadoFisico = EstadoFisicoPuesto.OPERATIVO;
    private EstadoPuesto estado = EstadoPuesto.DISPONIBLE;

    public PuestoComercialCreateRequest() {
    }

    public PuestoComercialCreateRequest(UUID mercadoId, UUID sectorId, UUID pabellonId, UUID zonaId, Long tipoPuestoId,
                                        String codigo, String numero, String nombreReferencial, String descripcion,
                                        BigDecimal areaMetrosCuadrados, BigDecimal anchoMetros, BigDecimal largoMetros,
                                        Integer nivel, String ubicacionReferencia, EstadoFisicoPuesto estadoFisico, EstadoPuesto estado) {
        this.mercadoId = mercadoId;
        this.sectorId = sectorId;
        this.pabellonId = pabellonId;
        this.zonaId = zonaId;
        this.tipoPuestoId = tipoPuestoId;
        this.codigo = codigo;
        this.numero = numero;
        this.nombreReferencial = nombreReferencial;
        this.descripcion = descripcion;
        this.areaMetrosCuadrados = areaMetrosCuadrados;
        this.anchoMetros = anchoMetros;
        this.largoMetros = largoMetros;
        this.nivel = nivel != null ? nivel : 1;
        this.ubicacionReferencia = ubicacionReferencia;
        this.estadoFisico = estadoFisico != null ? estadoFisico : EstadoFisicoPuesto.OPERATIVO;
        this.estado = estado != null ? estado : EstadoPuesto.DISPONIBLE;
    }

    public static PuestoComercialCreateRequestBuilder builder() {
        return new PuestoComercialCreateRequestBuilder();
    }

    public UUID getMercadoId() { return mercadoId; }
    public void setMercadoId(UUID mercadoId) { this.mercadoId = mercadoId; }

    public UUID getSectorId() { return sectorId; }
    public void setSectorId(UUID sectorId) { this.sectorId = sectorId; }

    public UUID getPabellonId() { return pabellonId; }
    public void setPabellonId(UUID pabellonId) { this.pabellonId = pabellonId; }

    public UUID getZonaId() { return zonaId; }
    public void setZonaId(UUID zonaId) { this.zonaId = zonaId; }

    public Long getTipoPuestoId() { return tipoPuestoId; }
    public void setTipoPuestoId(Long tipoPuestoId) { this.tipoPuestoId = tipoPuestoId; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getNombreReferencial() { return nombreReferencial; }
    public void setNombreReferencial(String nombreReferencial) { this.nombreReferencial = nombreReferencial; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

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

    public EstadoPuesto getEstado() { return estado; }
    public void setEstado(EstadoPuesto estado) { this.estado = estado; }

    public static class PuestoComercialCreateRequestBuilder {
        private UUID mercadoId;
        private UUID sectorId;
        private UUID pabellonId;
        private UUID zonaId;
        private Long tipoPuestoId;
        private String codigo;
        private String numero;
        private String nombreReferencial;
        private String descripcion;
        private BigDecimal areaMetrosCuadrados;
        private BigDecimal anchoMetros;
        private BigDecimal largoMetros;
        private Integer nivel = 1;
        private String ubicacionReferencia;
        private EstadoFisicoPuesto estadoFisico = EstadoFisicoPuesto.OPERATIVO;
        private EstadoPuesto estado = EstadoPuesto.DISPONIBLE;

        public PuestoComercialCreateRequestBuilder mercadoId(UUID mercadoId) { this.mercadoId = mercadoId; return this; }
        public PuestoComercialCreateRequestBuilder sectorId(UUID sectorId) { this.sectorId = sectorId; return this; }
        public PuestoComercialCreateRequestBuilder pabellonId(UUID pabellonId) { this.pabellonId = pabellonId; return this; }
        public PuestoComercialCreateRequestBuilder zonaId(UUID zonaId) { this.zonaId = zonaId; return this; }
        public PuestoComercialCreateRequestBuilder tipoPuestoId(Long tipoPuestoId) { this.tipoPuestoId = tipoPuestoId; return this; }
        public PuestoComercialCreateRequestBuilder codigo(String codigo) { this.codigo = codigo; return this; }
        public PuestoComercialCreateRequestBuilder numero(String numero) { this.numero = numero; return this; }
        public PuestoComercialCreateRequestBuilder nombreReferencial(String nombreReferencial) { this.nombreReferencial = nombreReferencial; return this; }
        public PuestoComercialCreateRequestBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public PuestoComercialCreateRequestBuilder areaMetrosCuadrados(BigDecimal areaMetrosCuadrados) { this.areaMetrosCuadrados = areaMetrosCuadrados; return this; }
        public PuestoComercialCreateRequestBuilder anchoMetros(BigDecimal anchoMetros) { this.anchoMetros = anchoMetros; return this; }
        public PuestoComercialCreateRequestBuilder largoMetros(BigDecimal largoMetros) { this.largoMetros = largoMetros; return this; }
        public PuestoComercialCreateRequestBuilder nivel(Integer nivel) { this.nivel = nivel; return this; }
        public PuestoComercialCreateRequestBuilder ubicacionReferencia(String ubicacionReferencia) { this.ubicacionReferencia = ubicacionReferencia; return this; }
        public PuestoComercialCreateRequestBuilder estadoFisico(EstadoFisicoPuesto estadoFisico) { this.estadoFisico = estadoFisico; return this; }
        public PuestoComercialCreateRequestBuilder estado(EstadoPuesto estado) { this.estado = estado; return this; }

        public PuestoComercialCreateRequest build() {
            return new PuestoComercialCreateRequest(mercadoId, sectorId, pabellonId, zonaId, tipoPuestoId, codigo, numero,
                    nombreReferencial, descripcion, areaMetrosCuadrados, anchoMetros, largoMetros, nivel, ubicacionReferencia, estadoFisico, estado);
        }
    }
}
