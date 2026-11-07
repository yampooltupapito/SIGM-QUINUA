package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;

import java.math.BigDecimal;

public class MercadoRequest {

    @NotBlank(message = "El código del mercado es obligatorio")
    @Size(max = 50, message = "El código no debe exceder 50 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "El código solo debe contener caracteres alfanuméricos, guiones o guiones bajos")
    private String codigo;

    @NotBlank(message = "El nombre del mercado es obligatorio")
    @Size(max = 150, message = "El nombre no debe exceder 150 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no debe exceder 500 caracteres")
    private String descripcion;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255, message = "La dirección no debe exceder 255 caracteres")
    private String direccion;

    @Size(max = 255, message = "La referencia no debe exceder 255 caracteres")
    private String referenciaUbicacion;

    @Size(max = 100, message = "El distrito no debe exceder 100 caracteres")
    private String distrito;

    @Size(max = 100, message = "La provincia no debe exceder 100 caracteres")
    private String provincia;

    @Size(max = 100, message = "El departamento no debe exceder 100 caracteres")
    private String departamento;

    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90 grados")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90 grados")
    private BigDecimal latitud;

    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180 grados")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180 grados")
    private BigDecimal longitud;

    @Min(value = 0, message = "La capacidad referencial no puede ser negativa")
    private Integer capacidadReferencial;

    private EstadoMercado estado;
    private Long oficinaAdministradoraId;
    private Long version;

    public MercadoRequest() {
    }

    public MercadoRequest(String codigo, String nombre, String descripcion, String direccion,
                          String referenciaUbicacion, String distrito, String provincia, String departamento,
                          BigDecimal latitud, BigDecimal longitud, Integer capacidadReferencial,
                          EstadoMercado estado, Long oficinaAdministradoraId, Long version) {
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
        this.version = version;
    }

    public static MercadoRequestBuilder builder() {
        return new MercadoRequestBuilder();
    }

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

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public static class MercadoRequestBuilder {
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
        private Long version;

        public MercadoRequestBuilder codigo(String codigo) { this.codigo = codigo; return this; }
        public MercadoRequestBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public MercadoRequestBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public MercadoRequestBuilder direccion(String direccion) { this.direccion = direccion; return this; }
        public MercadoRequestBuilder referenciaUbicacion(String referenciaUbicacion) { this.referenciaUbicacion = referenciaUbicacion; return this; }
        public MercadoRequestBuilder distrito(String distrito) { this.distrito = distrito; return this; }
        public MercadoRequestBuilder provincia(String provincia) { this.provincia = provincia; return this; }
        public MercadoRequestBuilder departamento(String departamento) { this.departamento = departamento; return this; }
        public MercadoRequestBuilder latitud(BigDecimal latitud) { this.latitud = latitud; return this; }
        public MercadoRequestBuilder longitud(BigDecimal longitud) { this.longitud = longitud; return this; }
        public MercadoRequestBuilder capacidadReferencial(Integer capacidadReferencial) { this.capacidadReferencial = capacidadReferencial; return this; }
        public MercadoRequestBuilder estado(EstadoMercado estado) { this.estado = estado; return this; }
        public MercadoRequestBuilder oficinaAdministradoraId(Long oficinaAdministradoraId) { this.oficinaAdministradoraId = oficinaAdministradoraId; return this; }
        public MercadoRequestBuilder version(Long version) { this.version = version; return this; }

        public MercadoRequest build() {
            return new MercadoRequest(codigo, nombre, descripcion, direccion, referenciaUbicacion,
                    distrito, provincia, departamento, latitud, longitud, capacidadReferencial, estado,
                    oficinaAdministradoraId, version);
        }
    }
}
