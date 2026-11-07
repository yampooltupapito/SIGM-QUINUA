package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoActuacionIncidencia;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "mercados_incidencias_actuaciones")
public class ActuacionIncidenciaMercado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidencia_id", nullable = false)
    private IncidenciaMercado incidencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_actuacion", nullable = false, length = 50)
    private TipoActuacionIncidencia tipoActuacion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private OffsetDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Usuario responsable;

    @Column(name = "documento_id")
    private UUID documentoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 30)
    private EstadoIncidencia estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", length = 30)
    private EstadoIncidencia estadoNuevo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public ActuacionIncidenciaMercado() {
    }

    public ActuacionIncidenciaMercado(UUID id, IncidenciaMercado incidencia, TipoActuacionIncidencia tipoActuacion,
                                    String descripcion, OffsetDateTime fecha, Usuario responsable,
                                    UUID documentoId, EstadoIncidencia estadoAnterior, EstadoIncidencia estadoNuevo,
                                    OffsetDateTime createdAt) {
        this.id = id;
        this.incidencia = incidencia;
        this.tipoActuacion = tipoActuacion;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.responsable = responsable;
        this.documentoId = documentoId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public IncidenciaMercado getIncidencia() { return incidencia; }
    public void setIncidencia(IncidenciaMercado incidencia) { this.incidencia = incidencia; }

    public TipoActuacionIncidencia getTipoActuacion() { return tipoActuacion; }
    public void setTipoActuacion(TipoActuacionIncidencia tipoActuacion) { this.tipoActuacion = tipoActuacion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public OffsetDateTime getFecha() { return fecha; }
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }

    public Usuario getResponsable() { return responsable; }
    public void setResponsable(Usuario responsable) { this.responsable = responsable; }

    public UUID getDocumentoId() { return documentoId; }
    public void setDocumentoId(UUID documentoId) { this.documentoId = documentoId; }

    public EstadoIncidencia getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(EstadoIncidencia estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public EstadoIncidencia getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(EstadoIncidencia estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActuacionIncidenciaMercado that = (ActuacionIncidenciaMercado) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private IncidenciaMercado incidencia;
        private TipoActuacionIncidencia tipoActuacion;
        private String descripcion;
        private OffsetDateTime fecha;
        private Usuario responsable;
        private UUID documentoId;
        private EstadoIncidencia estadoAnterior;
        private EstadoIncidencia estadoNuevo;
        private OffsetDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder incidencia(IncidenciaMercado incidencia) { this.incidencia = incidencia; return this; }
        public Builder tipoActuacion(TipoActuacionIncidencia tipoActuacion) { this.tipoActuacion = tipoActuacion; return this; }
        public Builder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public Builder fecha(OffsetDateTime fecha) { this.fecha = fecha; return this; }
        public Builder responsable(Usuario responsable) { this.responsable = responsable; return this; }
        public Builder documentoId(UUID documentoId) { this.documentoId = documentoId; return this; }
        public Builder estadoAnterior(EstadoIncidencia estadoAnterior) { this.estadoAnterior = estadoAnterior; return this; }
        public Builder estadoNuevo(EstadoIncidencia estadoNuevo) { this.estadoNuevo = estadoNuevo; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ActuacionIncidenciaMercado build() {
            return new ActuacionIncidenciaMercado(id, incidencia, tipoActuacion, descripcion, fecha,
                    responsable, documentoId, estadoAnterior, estadoNuevo, createdAt);
        }
    }
}
