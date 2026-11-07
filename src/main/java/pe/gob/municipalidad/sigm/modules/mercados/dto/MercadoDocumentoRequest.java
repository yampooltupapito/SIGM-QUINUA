package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoDocumentoMercado;

public class MercadoDocumentoRequest {

    @NotNull(message = "El ID del documento institucional es obligatorio")
    private Long documentoId;

    @NotNull(message = "El tipo de documento es obligatorio")
    private TipoDocumentoMercado tipoDocumento;

    @Size(max = 255, message = "La descripción no debe exceder 255 caracteres")
    private String descripcion;

    public MercadoDocumentoRequest() {
    }

    public MercadoDocumentoRequest(Long documentoId, TipoDocumentoMercado tipoDocumento, String descripcion) {
        this.documentoId = documentoId;
        this.tipoDocumento = tipoDocumento;
        this.descripcion = descripcion;
    }

    public static MercadoDocumentoRequestBuilder builder() {
        return new MercadoDocumentoRequestBuilder();
    }

    public Long getDocumentoId() { return documentoId; }
    public void setDocumentoId(Long documentoId) { this.documentoId = documentoId; }

    public TipoDocumentoMercado getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumentoMercado tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public static class MercadoDocumentoRequestBuilder {
        private Long documentoId;
        private TipoDocumentoMercado tipoDocumento;
        private String descripcion;

        public MercadoDocumentoRequestBuilder documentoId(Long documentoId) {
            this.documentoId = documentoId;
            return this;
        }

        public MercadoDocumentoRequestBuilder tipoDocumento(TipoDocumentoMercado tipoDocumento) {
            this.tipoDocumento = tipoDocumento;
            return this;
        }

        public MercadoDocumentoRequestBuilder descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public MercadoDocumentoRequest build() {
            return new MercadoDocumentoRequest(documentoId, tipoDocumento, descripcion);
        }
    }
}
