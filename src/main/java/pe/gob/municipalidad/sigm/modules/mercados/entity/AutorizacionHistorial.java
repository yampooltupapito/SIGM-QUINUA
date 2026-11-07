package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Registro inmutable de trazabilidad de autorizaciones comerciales.
 */
@Entity
@Table(name = "mercados_autorizaciones_historial")
public class AutorizacionHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autorizacion_id", nullable = false)
    private AutorizacionComercial autorizacion;

    @Column(name = "estado_anterior", length = 30)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 30)
    private String estadoNuevo;

    @Column(name = "accion", nullable = false, length = 50)
    private String accion;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "usuario", nullable = false, length = 100)
    private String usuario;

    @Column(name = "fecha", nullable = false)
    private OffsetDateTime fecha = OffsetDateTime.now();

    @Column(name = "documento_referencia", length = 100)
    private String documentoReferencia;

    public AutorizacionHistorial() {}

    public AutorizacionHistorial(AutorizacionComercial autorizacion, String estadoAnterior, String estadoNuevo, String accion, String motivo, String usuario, String documentoReferencia) {
        this.autorizacion = autorizacion;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.accion = accion;
        this.motivo = motivo;
        this.usuario = usuario;
        this.documentoReferencia = documentoReferencia;
        this.fecha = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AutorizacionComercial getAutorizacion() { return autorizacion; }
    public void setAutorizacion(AutorizacionComercial autorizacion) { this.autorizacion = autorizacion; }

    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public OffsetDateTime getFecha() { return fecha; }
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }

    public String getDocumentoReferencia() { return documentoReferencia; }
    public void setDocumentoReferencia(String documentoReferencia) { this.documentoReferencia = documentoReferencia; }
}
