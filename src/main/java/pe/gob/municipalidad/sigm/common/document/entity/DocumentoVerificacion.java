package pe.gob.municipalidad.sigm.common.document.entity;

import jakarta.persistence.*;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.time.OffsetDateTime;

/**
 * Registro de verificación de integridad criptográfica SHA-256 de un documento.
 */
@Entity
@Table(name = "documento_verificaciones", indexes = {
        @Index(name = "idx_doc_verif_doc", columnList = "documento_id")
})
public class DocumentoVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    @Column(name = "version_evaluada", nullable = false)
    private Integer versionEvaluada;

    @Column(name = "hash_esperado", nullable = false, length = 64)
    private String hashEsperado;

    @Column(name = "hash_calculado", nullable = false, length = 64)
    private String hashCalculado;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 20)
    private ResultadoVerificacion resultado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verificado_por_id")
    private Usuario verificadoPor;

    @Column(name = "fecha_verificacion", nullable = false, updatable = false)
    private OffsetDateTime fechaVerificacion;

    @Column(name = "detalle", length = 500)
    private String detalle;

    public DocumentoVerificacion() {
    }

    public DocumentoVerificacion(Long id, Documento documento, Integer versionEvaluada, String hashEsperado,
                                 String hashCalculado, ResultadoVerificacion resultado, Usuario verificadoPor,
                                 OffsetDateTime fechaVerificacion, String detalle) {
        this.id = id;
        this.documento = documento;
        this.versionEvaluada = versionEvaluada;
        this.hashEsperado = hashEsperado;
        this.hashCalculado = hashCalculado;
        this.resultado = resultado;
        this.verificadoPor = verificadoPor;
        this.fechaVerificacion = fechaVerificacion;
        this.detalle = detalle;
    }

    public static DocumentoVerificacionBuilder builder() {
        return new DocumentoVerificacionBuilder();
    }

    @PrePersist
    public void prePersist() {
        if (fechaVerificacion == null) {
            fechaVerificacion = OffsetDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Documento getDocumento() { return documento; }
    public void setDocumento(Documento documento) { this.documento = documento; }

    public Integer getVersionEvaluada() { return versionEvaluada; }
    public void setVersionEvaluada(Integer versionEvaluada) { this.versionEvaluada = versionEvaluada; }

    public String getHashEsperado() { return hashEsperado; }
    public void setHashEsperado(String hashEsperado) { this.hashEsperado = hashEsperado; }

    public String getHashCalculado() { return hashCalculado; }
    public void setHashCalculado(String hashCalculado) { this.hashCalculado = hashCalculado; }

    public ResultadoVerificacion getResultado() { return resultado; }
    public void setResultado(ResultadoVerificacion resultado) { this.resultado = resultado; }

    public Usuario getVerificadoPor() { return verificadoPor; }
    public void setVerificadoPor(Usuario verificadoPor) { this.verificadoPor = verificadoPor; }

    public OffsetDateTime getFechaVerificacion() { return fechaVerificacion; }
    public void setFechaVerificacion(OffsetDateTime fechaVerificacion) { this.fechaVerificacion = fechaVerificacion; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public static class DocumentoVerificacionBuilder {
        private Long id;
        private Documento documento;
        private Integer versionEvaluada;
        private String hashEsperado;
        private String hashCalculado;
        private ResultadoVerificacion resultado;
        private Usuario verificadoPor;
        private OffsetDateTime fechaVerificacion;
        private String detalle;

        public DocumentoVerificacionBuilder id(Long id) { this.id = id; return this; }
        public DocumentoVerificacionBuilder documento(Documento documento) { this.documento = documento; return this; }
        public DocumentoVerificacionBuilder versionEvaluada(Integer versionEvaluada) { this.versionEvaluada = versionEvaluada; return this; }
        public DocumentoVerificacionBuilder hashEsperado(String hashEsperado) { this.hashEsperado = hashEsperado; return this; }
        public DocumentoVerificacionBuilder hashCalculado(String hashCalculado) { this.hashCalculado = hashCalculado; return this; }
        public DocumentoVerificacionBuilder resultado(ResultadoVerificacion resultado) { this.resultado = resultado; return this; }
        public DocumentoVerificacionBuilder verificadoPor(Usuario verificadoPor) { this.verificadoPor = verificadoPor; return this; }
        public DocumentoVerificacionBuilder fechaVerificacion(OffsetDateTime fechaVerificacion) { this.fechaVerificacion = fechaVerificacion; return this; }
        public DocumentoVerificacionBuilder detalle(String detalle) { this.detalle = detalle; return this; }

        public DocumentoVerificacion build() {
            return new DocumentoVerificacion(id, documento, versionEvaluada, hashEsperado, hashCalculado, resultado, verificadoPor, fechaVerificacion, detalle);
        }
    }
}
