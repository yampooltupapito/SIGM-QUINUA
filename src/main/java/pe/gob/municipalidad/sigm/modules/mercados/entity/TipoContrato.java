package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Catálogo configurable de tipos de contrato de ocupación de mercado.
 */
@Entity
@Table(name = "mercados_tipos_contrato")
public class TipoContrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "base_normativa", columnDefinition = "TEXT")
    private String baseNormativa;

    @Column(name = "vigencia_meses_defecto")
    private Integer vigenciaMesesDefecto = 12;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVO";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public TipoContrato() {}

    public TipoContrato(Long id, String codigo, String nombre, String descripcion, String baseNormativa, Integer vigenciaMesesDefecto, String estado) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.baseNormativa = baseNormativa;
        this.vigenciaMesesDefecto = vigenciaMesesDefecto;
        this.estado = estado;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getBaseNormativa() { return baseNormativa; }
    public void setBaseNormativa(String baseNormativa) { this.baseNormativa = baseNormativa; }

    public Integer getVigenciaMesesDefecto() { return vigenciaMesesDefecto; }
    public void setVigenciaMesesDefecto(Integer vigenciaMesesDefecto) { this.vigenciaMesesDefecto = vigenciaMesesDefecto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
