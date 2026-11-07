package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoDerivacion;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mpv_derivaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DerivacionExpediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private Expediente expediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_origen_id", nullable = false)
    private Oficina oficinaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_destino_id", nullable = false)
    private Oficina oficinaDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_derivador_id", nullable = false)
    private Usuario usuarioDerivador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_receptor_id")
    private Usuario usuarioReceptor;

    @Column(nullable = false, length = 1000)
    private String motivo;

    @Column(columnDefinition = "TEXT")
    private String indicaciones;

    @Column(name = "motivo_devolucion", columnDefinition = "TEXT")
    private String motivoDevolucion;

    @Column(name = "correlativo_derivacion", length = 50)
    private String correlativoDerivacion;

    @Column(name = "fecha_derivacion", nullable = false)
    private OffsetDateTime fechaDerivacion;

    @Column(name = "fecha_recepcion")
    private OffsetDateTime fechaRecepcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoDerivacion estado = EstadoDerivacion.PENDIENTE;

    @Column(length = 1000)
    private String observaciones;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (fechaDerivacion == null) {
            fechaDerivacion = OffsetDateTime.now();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (estado == null) {
            estado = EstadoDerivacion.PENDIENTE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
