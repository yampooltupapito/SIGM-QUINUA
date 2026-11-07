package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import pe.gob.municipalidad.sigm.common.model.Oficina;

import java.time.OffsetDateTime;

/**
 * Catálogo configurable de tipos de autorización comercial en mercados.
 */
@Entity
@Table(name = "mercados_tipos_autorizacion")
public class TipoAutorizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "procedimiento_relacionado", length = 150)
    private String procedimientoRelacionado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_competente_id")
    private Oficina oficinaCompetente;

    @Column(name = "vigencia_meses_defecto")
    private Integer vigenciaMesesDefecto = 12;

    @Column(name = "fuente_normativa", columnDefinition = "TEXT")
    private String fuenteNormativa;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVO";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public TipoAutorizacion() {}

    public TipoAutorizacion(Long id, String codigo, String nombre, String descripcion, String procedimientoRelacionado, Oficina oficinaCompetente, Integer vigenciaMesesDefecto, String fuenteNormativa, String estado) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.procedimientoRelacionado = procedimientoRelacionado;
        this.oficinaCompetente = oficinaCompetente;
        this.vigenciaMesesDefecto = vigenciaMesesDefecto;
        this.fuenteNormativa = fuenteNormativa;
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

    public String getProcedimientoRelacionado() { return procedimientoRelacionado; }
    public void setProcedimientoRelacionado(String procedimientoRelacionado) { this.procedimientoRelacionado = procedimientoRelacionado; }

    public Oficina getOficinaCompetente() { return oficinaCompetente; }
    public void setOficinaCompetente(Oficina oficinaCompetente) { this.oficinaCompetente = oficinaCompetente; }

    public Integer getVigenciaMesesDefecto() { return vigenciaMesesDefecto; }
    public void setVigenciaMesesDefecto(Integer vigenciaMesesDefecto) { this.vigenciaMesesDefecto = vigenciaMesesDefecto; }

    public String getFuenteNormativa() { return fuenteNormativa; }
    public void setFuenteNormativa(String fuenteNormativa) { this.fuenteNormativa = fuenteNormativa; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
