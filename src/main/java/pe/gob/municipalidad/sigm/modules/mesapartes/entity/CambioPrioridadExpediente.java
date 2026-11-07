package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.PrioridadExpediente;

import java.time.OffsetDateTime;

/**
 * Registro de auditoría de modificaciones a la prioridad de atención de un expediente.
 */
@Entity
@Table(name = "mpv_cambios_prioridad")
public class CambioPrioridadExpediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private Expediente expediente;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad_anterior", nullable = false, length = 20)
    private PrioridadExpediente prioridadAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad_nueva", nullable = false, length = 20)
    private PrioridadExpediente prioridadNueva;

    @Column(nullable = false, length = 1000)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_cambio", nullable = false)
    private OffsetDateTime fechaCambio;

    public CambioPrioridadExpediente() {
    }

    public CambioPrioridadExpediente(Long id, Expediente expediente, PrioridadExpediente prioridadAnterior,
                                     PrioridadExpediente prioridadNueva, String motivo, Usuario usuario, OffsetDateTime fechaCambio) {
        this.id = id;
        this.expediente = expediente;
        this.prioridadAnterior = prioridadAnterior;
        this.prioridadNueva = prioridadNueva;
        this.motivo = motivo;
        this.usuario = usuario;
        this.fechaCambio = fechaCambio;
    }

    public static CambioPrioridadExpedienteBuilder builder() {
        return new CambioPrioridadExpedienteBuilder();
    }

    @PrePersist
    protected void onCreate() {
        if (fechaCambio == null) {
            fechaCambio = OffsetDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Expediente getExpediente() { return expediente; }
    public void setExpediente(Expediente expediente) { this.expediente = expediente; }

    public PrioridadExpediente getPrioridadAnterior() { return prioridadAnterior; }
    public void setPrioridadAnterior(PrioridadExpediente prioridadAnterior) { this.prioridadAnterior = prioridadAnterior; }

    public PrioridadExpediente getPrioridadNueva() { return prioridadNueva; }
    public void setPrioridadNueva(PrioridadExpediente prioridadNueva) { this.prioridadNueva = prioridadNueva; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public OffsetDateTime getFechaCambio() { return fechaCambio; }
    public void setFechaCambio(OffsetDateTime fechaCambio) { this.fechaCambio = fechaCambio; }

    public static class CambioPrioridadExpedienteBuilder {
        private Long id;
        private Expediente expediente;
        private PrioridadExpediente prioridadAnterior;
        private PrioridadExpediente prioridadNueva;
        private String motivo;
        private Usuario usuario;
        private OffsetDateTime fechaCambio;

        public CambioPrioridadExpedienteBuilder id(Long id) { this.id = id; return this; }
        public CambioPrioridadExpedienteBuilder expediente(Expediente expediente) { this.expediente = expediente; return this; }
        public CambioPrioridadExpedienteBuilder prioridadAnterior(PrioridadExpediente prioridadAnterior) { this.prioridadAnterior = prioridadAnterior; return this; }
        public CambioPrioridadExpedienteBuilder prioridadNueva(PrioridadExpediente prioridadNueva) { this.prioridadNueva = prioridadNueva; return this; }
        public CambioPrioridadExpedienteBuilder motivo(String motivo) { this.motivo = motivo; return this; }
        public CambioPrioridadExpedienteBuilder usuario(Usuario usuario) { this.usuario = usuario; return this; }
        public CambioPrioridadExpedienteBuilder fechaCambio(OffsetDateTime fechaCambio) { this.fechaCambio = fechaCambio; return this; }

        public CambioPrioridadExpediente build() {
            return new CambioPrioridadExpediente(id, expediente, prioridadAnterior, prioridadNueva, motivo, usuario, fechaCambio);
        }
    }
}
