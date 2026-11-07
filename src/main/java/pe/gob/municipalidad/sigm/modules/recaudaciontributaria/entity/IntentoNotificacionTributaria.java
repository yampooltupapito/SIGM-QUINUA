package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ResultadoIntentoNotificacion;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Registro de actuación y visita fáctica efectuada por el notificador tributario.
 */
@Entity
@Table(name = "tributaria_intentos_notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class IntentoNotificacionTributaria extends AuditableEntity {

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

    @Column(name = "fecha_hora_intento", nullable = false)
    private OffsetDateTime fechaHoraIntento;

    @Column(name = "medio_utilizado", nullable = false, length = 60)
    private String medioUtilizado;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 40)
    private ResultadoIntentoNotificacion resultado;

    @Column(name = "ubicacion_referencia", length = 255)
    private String ubicacionReferencia;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}

