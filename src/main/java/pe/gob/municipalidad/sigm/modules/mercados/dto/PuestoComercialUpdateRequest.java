package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class PuestoComercialUpdateRequest {

    @Size(max = 150, message = "El nombre referencial no debe exceder 150 caracteres")
    private String nombreReferencial;

    @NotNull(message = "El tipo de puesto es obligatorio")
    private Long tipoPuestoId;

    @Size(max = 500, message = "La descripción no debe exceder 500 caracteres")
    private String descripcion;

    @DecimalMin(value = "0.00", message = "El área debe ser mayor o igual a 0")
    private BigDecimal areaMetrosCuadrados;

    @DecimalMin(value = "0.00", message = "El ancho debe ser mayor o igual a 0")
    private BigDecimal anchoMetros;

    @DecimalMin(value = "0.00", message = "El largo debe ser mayor o igual a 0")
    private BigDecimal largoMetros;

    @Min(value = 1, message = "El nivel debe ser como mínimo 1")
    private Integer nivel;

    @Size(max = 255, message = "La referencia de ubicación no debe exceder 255 caracteres")
    private String ubicacionReferencia;

    @NotNull(message = "La versión del registro es obligatoria para control de concurrencia")
    private Long version;

    public PuestoComercialUpdateRequest() {
    }

    public PuestoComercialUpdateRequest(String nombreReferencial, Long tipoPuestoId, String descripcion,
                                        BigDecimal areaMetrosCuadrados, BigDecimal anchoMetros, BigDecimal largoMetros,
                                        Integer nivel, String ubicacionReferencia, Long version) {
        this.nombreReferencial = nombreReferencial;
        this.tipoPuestoId = tipoPuestoId;
        this.descripcion = descripcion;
        this.areaMetrosCuadrados = areaMetrosCuadrados;
        this.anchoMetros = anchoMetros;
        this.largoMetros = largoMetros;
        this.nivel = nivel;
        this.ubicacionReferencia = ubicacionReferencia;
        this.version = version;
    }

    public static PuestoComercialUpdateRequestBuilder builder() {
        return new PuestoComercialUpdateRequestBuilder();
    }

    public String getNombreReferencial() { return nombreReferencial; }
    public void setNombreReferencial(String nombreReferencial) { this.nombreReferencial = nombreReferencial; }

    public Long getTipoPuestoId() { return tipoPuestoId; }
    public void setTipoPuestoId(Long tipoPuestoId) { this.tipoPuestoId = tipoPuestoId; }

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

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public static class PuestoComercialUpdateRequestBuilder {
        private String nombreReferencial;
        private Long tipoPuestoId;
        private String descripcion;
        private BigDecimal areaMetrosCuadrados;
        private BigDecimal anchoMetros;
        private BigDecimal largoMetros;
        private Integer nivel;
        private String ubicacionReferencia;
        private Long version;

        public PuestoComercialUpdateRequestBuilder nombreReferencial(String nombreReferencial) { this.nombreReferencial = nombreReferencial; return this; }
        public PuestoComercialUpdateRequestBuilder tipoPuestoId(Long tipoPuestoId) { this.tipoPuestoId = tipoPuestoId; return this; }
        public PuestoComercialUpdateRequestBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public PuestoComercialUpdateRequestBuilder areaMetrosCuadrados(BigDecimal areaMetrosCuadrados) { this.areaMetrosCuadrados = areaMetrosCuadrados; return this; }
        public PuestoComercialUpdateRequestBuilder anchoMetros(BigDecimal anchoMetros) { this.anchoMetros = anchoMetros; return this; }
        public PuestoComercialUpdateRequestBuilder largoMetros(BigDecimal largoMetros) { this.largoMetros = largoMetros; return this; }
        public PuestoComercialUpdateRequestBuilder nivel(Integer nivel) { this.nivel = nivel; return this; }
        public PuestoComercialUpdateRequestBuilder ubicacionReferencia(String ubicacionReferencia) { this.ubicacionReferencia = ubicacionReferencia; return this; }
        public PuestoComercialUpdateRequestBuilder version(Long version) { this.version = version; return this; }

        public PuestoComercialUpdateRequest build() {
            return new PuestoComercialUpdateRequest(nombreReferencial, tipoPuestoId, descripcion, areaMetrosCuadrados,
                    anchoMetros, largoMetros, nivel, ubicacionReferencia, version);
        }
    }
}
