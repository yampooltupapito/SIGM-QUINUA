package pe.gob.municipalidad.sigm.modules.mercados.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class PuestoDocumentoResponse {

    private Long id;
    private UUID documentoId;
    private String tipoDocumento;
    private String nombreArchivo;
    private Long tamanoBytes;
    private String mimeType;
    private String descripcion;
    private String createdBy;
    private LocalDateTime createdAt;

    public PuestoDocumentoResponse() {
    }

    public PuestoDocumentoResponse(Long id, UUID documentoId, String tipoDocumento, String nombreArchivo,
                                   Long tamanoBytes, String mimeType, String descripcion, String createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.documentoId = documentoId;
        this.tipoDocumento = tipoDocumento;
        this.nombreArchivo = nombreArchivo;
        this.tamanoBytes = tamanoBytes;
        this.mimeType = mimeType;
        this.descripcion = descripcion;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public static PuestoDocumentoResponseBuilder builder() {
        return new PuestoDocumentoResponseBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getDocumentoId() { return documentoId; }
    public void setDocumentoId(UUID documentoId) { this.documentoId = documentoId; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class PuestoDocumentoResponseBuilder {
        private Long id;
        private UUID documentoId;
        private String tipoDocumento;
        private String nombreArchivo;
        private Long tamanoBytes;
        private String mimeType;
        private String descripcion;
        private String createdBy;
        private LocalDateTime createdAt;

        public PuestoDocumentoResponseBuilder id(Long id) { this.id = id; return this; }
        public PuestoDocumentoResponseBuilder documentoId(UUID documentoId) { this.documentoId = documentoId; return this; }
        public PuestoDocumentoResponseBuilder tipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; return this; }
        public PuestoDocumentoResponseBuilder nombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; return this; }
        public PuestoDocumentoResponseBuilder tamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; return this; }
        public PuestoDocumentoResponseBuilder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public PuestoDocumentoResponseBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public PuestoDocumentoResponseBuilder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public PuestoDocumentoResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public PuestoDocumentoResponse build() {
            return new PuestoDocumentoResponse(id, documentoId, tipoDocumento, nombreArchivo, tamanoBytes, mimeType, descripcion, createdBy, createdAt);
        }
    }
}
