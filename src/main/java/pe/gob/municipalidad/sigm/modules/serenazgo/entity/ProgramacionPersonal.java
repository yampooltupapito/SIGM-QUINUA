package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAsignacionPersonal;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.FuncionOperativaPersonal;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_programacion_personal", uniqueConstraints = {
        @UniqueConstraint(name = "uq_prog_personal", columnNames = {"programacion_id", "personal_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramacionPersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programacion_id", nullable = false)
    private ProgramacionServicioSerenazgo programacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private PersonalSerenazgo personal;

    @Enumerated(EnumType.STRING)
    @Column(name = "funcion_operativa", nullable = false, length = 50)
    @Builder.Default
    private FuncionOperativaPersonal funcionOperativa = FuncionOperativaPersonal.PATRULLERO;

    @Column(name = "es_responsable", nullable = false)
    @Builder.Default
    private Boolean esResponsable = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_asignacion", nullable = false, length = 30)
    @Builder.Default
    private EstadoAsignacionPersonal estadoAsignacion = EstadoAsignacionPersonal.ASIGNADO;

    @Column(name = "fecha_asignacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaAsignacion = OffsetDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsable_id")
    private Usuario usuarioResponsable;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
