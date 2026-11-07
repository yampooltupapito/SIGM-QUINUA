package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAsignacionNotificador;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Asignación operativa de diligenciamiento de notificación a un notificador institucional.
 */
@Entity
@Table(name = "tributaria_asignaciones_notificador")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class AsignacionNotificadorTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacion_id", nullable = false)
    private NotificacionTributaria notificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificador_id", nullable = false)
    private Usuario notificador;

    @Column(name = "fecha_asignacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaAsignacion = OffsetDateTime.now();

    @Column(name = "fecha_programada")
    private LocalDate fechaProgramada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoAsignacionNotificador estado = EstadoAsignacionNotificador.ASIGNADA;

    @Column(name = "sector_ruta", length = 150)
    private String sectorRuta;

    @Column(name = "motivo_reasignacion", columnDefinition = "TEXT")
    private String motivoReasignacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Version
    @Column(name = "version")
    @Builder.Default
    private Long version = 0L;
}

