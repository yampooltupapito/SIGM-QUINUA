package pe.gob.municipalidad.sigm.common.document.entity;

import jakarta.persistence.*;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.time.OffsetDateTime;

/**
 * Registro de una versión específica de un documento.
 */
@Entity
@Table(name = "documento_versiones", uniqueConstraints = {
        @UniqueConstraint(name = "uq_doc_version", columnNames = {"documento_id", "numero_version"})
})
public class DocumentoVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    @Column(name = "numero_version", nullable = false)
    private Integer numeroVersion;

    @Column(name = "nombre_interno", nullable = false, length = 255)
    private String nombreInterno;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "hash_sha256", nullable = false, length = 64)
    private String hashSha256;

    @Column(name = "motivo_cambio", nullable = false, length = 500)
    private String motivoCambio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsable_id")
    private Usuario usuarioResponsable;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    public DocumentoVersion() {
    }

    public DocumentoVersion(Long id, Documento documento, Integer numeroVersion, String nombreInterno,
                            Long tamanoBytes, String mimeType, String hashSha256, String motivoCambio,
                            Usuario usuarioResponsable, OffsetDateTime fechaCreacion) {
        this.id = id;
        this.documento = documento;
        this.numeroVersion = numeroVersion;
        this.nombreInterno = nombreInterno;
        this.tamanoBytes = tamanoBytes;
        this.mimeType = mimeType;
        this.hashSha256 = hashSha256;
        this.motivoCambio = motivoCambio;
        this.usuarioResponsable = usuarioResponsable;
        this.fechaCreacion = fechaCreacion;
    }

    public static DocumentoVersionBuilder builder() {
        return new DocumentoVersionBuilder();
    }

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = OffsetDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Documento getDocumento() { return documento; }
    public void setDocumento(Documento documento) { this.documento = documento; }

    public Integer getNumeroVersion() { return numeroVersion; }
    public void setNumeroVersion(Integer numeroVersion) { this.numeroVersion = numeroVersion; }

    public String getNombreInterno() { return nombreInterno; }
    public void setNombreInterno(String nombreInterno) { this.nombreInterno = nombreInterno; }

    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getHashSha256() { return hashSha256; }
    public void setHashSha256(String hashSha256) { this.hashSha256 = hashSha256; }

    public String getMotivoCambio() { return motivoCambio; }
    public void setMotivoCambio(String motivoCambio) { this.motivoCambio = motivoCambio; }

    public Usuario getUsuarioResponsable() { return usuarioResponsable; }
    public void setUsuarioResponsable(Usuario usuarioResponsable) { this.usuarioResponsable = usuarioResponsable; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public static class DocumentoVersionBuilder {
        private Long id;
        private Documento documento;
        private Integer numeroVersion;
        private String nombreInterno;
        private Long tamanoBytes;
        private String mimeType;
        private String hashSha256;
        private String motivoCambio;
        private Usuario usuarioResponsable;
        private OffsetDateTime fechaCreacion;

        public DocumentoVersionBuilder id(Long id) { this.id = id; return this; }
        public DocumentoVersionBuilder documento(Documento documento) { this.documento = documento; return this; }
        public DocumentoVersionBuilder numeroVersion(Integer numeroVersion) { this.numeroVersion = numeroVersion; return this; }
        public DocumentoVersionBuilder nombreInterno(String nombreInterno) { this.nombreInterno = nombreInterno; return this; }
        public DocumentoVersionBuilder tamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; return this; }
        public DocumentoVersionBuilder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public DocumentoVersionBuilder hashSha256(String hashSha256) { this.hashSha256 = hashSha256; return this; }
        public DocumentoVersionBuilder motivoCambio(String motivoCambio) { this.motivoCambio = motivoCambio; return this; }
        public DocumentoVersionBuilder usuarioResponsable(Usuario usuarioResponsable) { this.usuarioResponsable = usuarioResponsable; return this; }
        public DocumentoVersionBuilder fechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; return this; }

        public DocumentoVersion build() {
            return new DocumentoVersion(id, documento, numeroVersion, nombreInterno, tamanoBytes, mimeType, hashSha256, motivoCambio, usuarioResponsable, fechaCreacion);
        }
    }
}
