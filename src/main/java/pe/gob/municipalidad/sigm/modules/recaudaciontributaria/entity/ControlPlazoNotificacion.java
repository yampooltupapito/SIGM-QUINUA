package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPlazoNotificacion;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Control y cómputo de plazos procesales legales derivados de la notificación válida.
 */
@Entity
@Table(name = "tributaria_controles_plazo_notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class ControlPlazoNotificacion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacion_id", nullable = false, unique = true)
    private NotificacionTributaria notificacion;

    @Column(name = "tipo_plazo", nullable = false, length = 60)
    private String tipoPlazo;

    @Column(name = "dias_habiles", nullable = false)
    @Builder.Default
    private Boolean diasHabiles = true;

    @Column(name = "numero_dias", nullable = false)
    private Integer numeroDias;

    @Column(name = "fecha_hito_inicial", nullable = false)
    private LocalDate fechaHitoInicial;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "dias_restantes", nullable = false)
    @Builder.Default
    private Integer diasRestantes = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_plazo", nullable = false, length = 30)
    @Builder.Default
    private EstadoPlazoNotificacion estadoPlazo = EstadoPlazoNotificacion.VIGENTE;

    @Column(name = "fecha_cumplimiento")
    private LocalDate fechaCumplimiento;

    @Column(name = "motivo_suspension", columnDefinition = "TEXT")
    private String motivoSuspension;
}

