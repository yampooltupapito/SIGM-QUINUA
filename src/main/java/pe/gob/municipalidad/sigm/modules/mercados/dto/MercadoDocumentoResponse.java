package pe.gob.municipalidad.sigm.modules.mercados.dto;

import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoDocumentoMercado;

import java.time.OffsetDateTime;
import java.util.UUID;

public class MercadoDocumentoResponse {
    private UUID id;
    private Long documentoId;
    private String nombreOriginal;
    private String extension;
    private String mimeType;
    private Long tamanoBytes;
    private TipoDocumentoMercado tipoDocumento;
    private String descripcion;
    private OffsetDateTime createdAt;
    private String createdBy;

    public MercadoDocumentoResponse() {
    }

    public MercadoDocumentoResponse(UUID id, Long documentoId, String nombreOriginal, String extension,
                                    String mimeType, Long tamanoBytes, TipoDocumentoMercado tipoDocumento,
                                    String descripcion, OffsetDateTime createdAt, String createdBy) {
        this.id = id;
        this.documentoId = documentoId;
        this.nombreOriginal = nombreOriginal;
        this.extension = extension;
        this.mimeType = mimeType;
        this.tamanoBytes = tamanoBytes;
        this.tipoDocumento = tipoDocumento;
        this.descripcion = descripcion;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public static MercadoDocumentoResponseBuilder builder() {
        return new MercadoDocumentoResponseBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Long getDocumentoId() { return documentoId; }
    public void setDocumentoId(Long documentoId) { this.documentoId = documentoId; }

    public String getNombreOriginal() { return nombreOriginal; }
    public void setNombreOriginal(String nombreOriginal) { this.nombreOriginal = nombreOriginal; }

    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }

    public TipoDocumentoMercado getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumentoMercado tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public static class MercadoDocumentoResponseBuilder {
        private UUID id;
        private Long documentoId;
        private String nombreOriginal;
        private String extension;
        private String mimeType;
        private Long tamanoBytes;
        private TipoDocumentoMercado tipoDocumento;
        private String descripcion;
        private OffsetDateTime createdAt;
        private String createdBy;

        public MercadoDocumentoResponseBuilder id(UUID id) { this.id = id; return this; }
        public MercadoDocumentoResponseBuilder documentoId(Long documentoId) { this.documentoId = documentoId; return this; }
        public MercadoDocumentoResponseBuilder nombreOriginal(String nombreOriginal) { this.nombreOriginal = nombreOriginal; return this; }
        public MercadoDocumentoResponseBuilder extension(String extension) { this.extension = extension; return this; }
        public MercadoDocumentoResponseBuilder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public MercadoDocumentoResponseBuilder tamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; return this; }
        public MercadoDocumentoResponseBuilder tipoDocumento(TipoDocumentoMercado tipoDocumento) { this.tipoDocumento = tipoDocumento; return this; }
        public MercadoDocumentoResponseBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public MercadoDocumentoResponseBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public MercadoDocumentoResponseBuilder createdBy(String createdBy) { this.createdBy = createdBy; return this; }

        public MercadoDocumentoResponse build() {
            return new MercadoDocumentoResponse(id, documentoId, nombreOriginal, extension, mimeType, tamanoBytes, tipoDocumento, descripcion, createdAt, createdBy);
        }
    }
}
