package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class PabellonRequest {

    @NotNull(message = "El ID del mercado es obligatorio")
    private UUID mercadoId;

    private UUID sectorId;

    @NotBlank(message = "El código del pabellón es obligatorio")
    @Size(max = 50, message = "El código no debe exceder 50 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "El código solo debe contener caracteres alfanuméricos, guiones o guiones bajos")
    private String codigo;

    @NotBlank(message = "El nombre del pabellón es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no debe exceder 500 caracteres")
    private String descripcion;

    @Min(value = 1, message = "El nivel o piso debe ser mayor o igual a 1")
    private Integer nivel;

    private String estado;
    private Integer ordenVisual;

    public PabellonRequest() {
    }

    public PabellonRequest(UUID mercadoId, UUID sectorId, String codigo, String nombre,
                           String descripcion, Integer nivel, String estado, Integer ordenVisual) {
        this.mercadoId = mercadoId;
        this.sectorId = sectorId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nivel = nivel != null ? nivel : 1;
        this.estado = estado;
        this.ordenVisual = ordenVisual;
    }

    public static PabellonRequestBuilder builder() {
        return new PabellonRequestBuilder();
    }

    public UUID getMercadoId() { return mercadoId; }
    public void setMercadoId(UUID mercadoId) { this.mercadoId = mercadoId; }

    public UUID getSectorId() { return sectorId; }
    public void setSectorId(UUID sectorId) { this.sectorId = sectorId; }

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

    public static class PabellonRequestBuilder {
        private UUID mercadoId;
        private UUID sectorId;
        private String codigo;
        private String nombre;
        private String descripcion;
        private Integer nivel = 1;
        private String estado;
        private Integer ordenVisual;

        public PabellonRequestBuilder mercadoId(UUID mercadoId) { this.mercadoId = mercadoId; return this; }
        public PabellonRequestBuilder sectorId(UUID sectorId) { this.sectorId = sectorId; return this; }
        public PabellonRequestBuilder codigo(String codigo) { this.codigo = codigo; return this; }
        public PabellonRequestBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public PabellonRequestBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public PabellonRequestBuilder nivel(Integer nivel) { this.nivel = nivel; return this; }
        public PabellonRequestBuilder estado(String estado) { this.estado = estado; return this; }
        public PabellonRequestBuilder ordenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; return this; }

        public PabellonRequest build() {
            return new PabellonRequest(mercadoId, sectorId, codigo, nombre, descripcion, nivel, estado, ordenVisual);
        }
    }
}
