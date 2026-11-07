package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.FuncionOperativaPersonal;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_atencion_personal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionPersonalSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atencion_id", nullable = false)
    private AtencionIncidenciaSerenazgo atencion;

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

    @Column(name = "fecha_asignacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaAsignacion = OffsetDateTime.now();

    @Column(name = "fecha_desasignacion")
    private OffsetDateTime fechaDesasignacion;

    @Column(name = "motivo_desasignacion", columnDefinition = "TEXT")
    private String motivoDesasignacion;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
