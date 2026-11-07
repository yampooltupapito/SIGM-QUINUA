package pe.gob.municipalidad.sigm.common.document.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Asociación entre un documento y una entidad de un módulo municipal.
 */
@Entity
@Table(name = "documento_relaciones", indexes = {
        @Index(name = "idx_doc_relaciones_doc", columnList = "documento_id"),
        @Index(name = "idx_doc_relaciones_entidad", columnList = "modulo, tipo_entidad, entidad_id")
})
public class DocumentoRelacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    @Column(name = "modulo", nullable = false, length = 50)
    private String modulo;

    @Column(name = "tipo_entidad", nullable = false, length = 50)
    private String tipoEntidad;

    @Column(name = "entidad_id", nullable = false, length = 100)
    private String entidadId;

    @Column(name = "rol_documento", nullable = false, length = 50)
    private String rolDocumento = "PRINCIPAL";

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    public DocumentoRelacion() {
    }

    public DocumentoRelacion(Long id, Documento documento, String modulo, String tipoEntidad,
                             String entidadId, String rolDocumento, OffsetDateTime fechaCreacion) {
        this.id = id;
        this.documento = documento;
        this.modulo = modulo;
        this.tipoEntidad = tipoEntidad;
        this.entidadId = entidadId;
        this.rolDocumento = rolDocumento != null ? rolDocumento : "PRINCIPAL";
        this.fechaCreacion = fechaCreacion;
    }

    public static DocumentoRelacionBuilder builder() {
        return new DocumentoRelacionBuilder();
    }

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = OffsetDateTime.now();
        }
        if (rolDocumento == null) {
            rolDocumento = "PRINCIPAL";
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Documento getDocumento() { return documento; }
    public void setDocumento(Documento documento) { this.documento = documento; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public String getTipoEntidad() { return tipoEntidad; }
    public void setTipoEntidad(String tipoEntidad) { this.tipoEntidad = tipoEntidad; }

    public String getEntidadId() { return entidadId; }
    public void setEntidadId(String entidadId) { this.entidadId = entidadId; }

    public String getRolDocumento() { return rolDocumento; }
    public void setRolDocumento(String rolDocumento) { this.rolDocumento = rolDocumento; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public static class DocumentoRelacionBuilder {
        private Long id;
        private Documento documento;
        private String modulo;
        private String tipoEntidad;
        private String entidadId;
        private String rolDocumento = "PRINCIPAL";
        private OffsetDateTime fechaCreacion;

        public DocumentoRelacionBuilder id(Long id) { this.id = id; return this; }
        public DocumentoRelacionBuilder documento(Documento documento) { this.documento = documento; return this; }
        public DocumentoRelacionBuilder modulo(String modulo) { this.modulo = modulo; return this; }
        public DocumentoRelacionBuilder tipoEntidad(String tipoEntidad) { this.tipoEntidad = tipoEntidad; return this; }
        public DocumentoRelacionBuilder entidadId(String entidadId) { this.entidadId = entidadId; return this; }
        public DocumentoRelacionBuilder rolDocumento(String rolDocumento) { this.rolDocumento = rolDocumento; return this; }
        public DocumentoRelacionBuilder fechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; return this; }

        public DocumentoRelacion build() {
            return new DocumentoRelacion(id, documento, modulo, tipoEntidad, entidadId, rolDocumento, fechaCreacion);
        }
    }
}
