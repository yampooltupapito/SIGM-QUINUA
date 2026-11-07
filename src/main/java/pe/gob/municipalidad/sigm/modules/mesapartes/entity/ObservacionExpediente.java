package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoObservacion;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mpv_observaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObservacionExpediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private Expediente expediente;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_observador_id", nullable = false)
    private Usuario usuarioObservador;

    @Column(name = "fecha_registro", nullable = false)
    private OffsetDateTime fechaRegistro;

    @Column(name = "fecha_notificacion")
    private OffsetDateTime fechaNotificacion;

    @Column(name = "fecha_limite")
    private OffsetDateTime fechaLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoObservacion estado = EstadoObservacion.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_observacion", nullable = false, length = 50)
    @Builder.Default
    private pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoObservacionExpediente tipoObservacion = pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoObservacionExpediente.FORMA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.VisibilidadObservacion visibilidad = pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.VisibilidadObservacion.CIUDADANO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisito_id")
    private RequisitoDocumental requisito;

    @Column(name = "fecha_respuesta")
    private OffsetDateTime fechaRespuesta;

    @OneToMany(mappedBy = "observacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SubsanacionExpediente> subsanaciones = new ArrayList<>();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = OffsetDateTime.now();
        }
        if (estado == null) {
            estado = EstadoObservacion.PENDIENTE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
