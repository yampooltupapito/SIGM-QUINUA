package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoSubsanacion;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mpv_subsanaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubsanacionExpediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "observacion_id", nullable = false)
    private ObservacionExpediente observacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private Expediente expediente;

    @Column(name = "respuesta_ciudadano", nullable = false, columnDefinition = "TEXT")
    private String respuestaCiudadano;

    @Column(name = "fecha_subsanacion", nullable = false)
    private OffsetDateTime fechaSubsanacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_subsanador_id")
    private Usuario usuarioSubsanador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoSubsanacion estado = EstadoSubsanacion.PRESENTADA;

    @PrePersist
    protected void onCreate() {
        if (fechaSubsanacion == null) {
            fechaSubsanacion = OffsetDateTime.now();
        }
        if (estado == null) {
            estado = EstadoSubsanacion.PRESENTADA;
        }
    }
}
