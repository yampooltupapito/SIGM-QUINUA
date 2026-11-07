package pe.gob.municipalidad.sigm.common.document.entity;

import jakarta.persistence.*;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.time.OffsetDateTime;

/**
 * Registro de auditoría para accesos, descargas y operaciones sobre documentos.
 */
@Entity
@Table(name = "documento_accesos", indexes = {
        @Index(name = "idx_doc_accesos_doc", columnList = "documento_id"),
        @Index(name = "idx_doc_accesos_usuario", columnList = "usuario_id")
})
public class DocumentoAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 50)
    private AccionAuditoriaDocumento accion;

    @Column(name = "direccion_ip", length = 45)
    private String direccionIp;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "fecha_acceso", nullable = false, updatable = false)
    private OffsetDateTime fechaAcceso;

    @Column(name = "resultado", nullable = false, length = 20)
    private String resultado = "EXITOSO";

    public DocumentoAcceso() {
    }

    public DocumentoAcceso(Long id, Documento documento, Usuario usuario, AccionAuditoriaDocumento accion,
                           String direccionIp, String userAgent, OffsetDateTime fechaAcceso, String resultado) {
        this.id = id;
        this.documento = documento;
        this.usuario = usuario;
        this.accion = accion;
        this.direccionIp = direccionIp;
        this.userAgent = userAgent;
        this.fechaAcceso = fechaAcceso;
        this.resultado = resultado != null ? resultado : "EXITOSO";
    }

    public static DocumentoAccesoBuilder builder() {
        return new DocumentoAccesoBuilder();
    }

    @PrePersist
    public void prePersist() {
        if (fechaAcceso == null) {
            fechaAcceso = OffsetDateTime.now();
        }
        if (resultado == null) {
            resultado = "EXITOSO";
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Documento getDocumento() { return documento; }
    public void setDocumento(Documento documento) { this.documento = documento; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public AccionAuditoriaDocumento getAccion() { return accion; }
    public void setAccion(AccionAuditoriaDocumento accion) { this.accion = accion; }

    public String getDireccionIp() { return direccionIp; }
    public void setDireccionIp(String direccionIp) { this.direccionIp = direccionIp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public OffsetDateTime getFechaAcceso() { return fechaAcceso; }
    public void setFechaAcceso(OffsetDateTime fechaAcceso) { this.fechaAcceso = fechaAcceso; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public static class DocumentoAccesoBuilder {
        private Long id;
        private Documento documento;
        private Usuario usuario;
        private AccionAuditoriaDocumento accion;
        private String direccionIp;
        private String userAgent;
        private OffsetDateTime fechaAcceso;
        private String resultado = "EXITOSO";

        public DocumentoAccesoBuilder id(Long id) { this.id = id; return this; }
        public DocumentoAccesoBuilder documento(Documento documento) { this.documento = documento; return this; }
        public DocumentoAccesoBuilder usuario(Usuario usuario) { this.usuario = usuario; return this; }
        public DocumentoAccesoBuilder accion(AccionAuditoriaDocumento accion) { this.accion = accion; return this; }
        public DocumentoAccesoBuilder direccionIp(String direccionIp) { this.direccionIp = direccionIp; return this; }
        public DocumentoAccesoBuilder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
        public DocumentoAccesoBuilder fechaAcceso(OffsetDateTime fechaAcceso) { this.fechaAcceso = fechaAcceso; return this; }
        public DocumentoAccesoBuilder resultado(String resultado) { this.resultado = resultado; return this; }

        public DocumentoAcceso build() {
            return new DocumentoAcceso(id, documento, usuario, accion, direccionIp, userAgent, fechaAcceso, resultado);
        }
    }
}
