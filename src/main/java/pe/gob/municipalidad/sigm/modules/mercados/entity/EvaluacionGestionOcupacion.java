package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.modules.mercados.enums.SentidoEvaluacionGestion;

import java.time.OffsetDateTime;

/**
 * Registro de evaluaciones técnicas y jurídicas realizadas sobre una solicitud de gestión de ocupación.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"solicitud", "documentoSustento"})
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_solicitudes_evaluaciones")
public class EvaluacionGestionOcupacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudGestionOcupacion solicitud;

    @Builder.Default
    @Column(name = "fecha_evaluacion", nullable = false)
    private OffsetDateTime fechaEvaluacion = OffsetDateTime.now();

    @Column(name = "usuario_evaluador", nullable = false, length = 100)
    private String usuarioEvaluador;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentido_evaluacion", nullable = false, length = 50)
    private SentidoEvaluacionGestion sentidoEvaluacion;

    @Column(name = "informe_tecnico", nullable = false, columnDefinition = "TEXT")
    private String informeTecnico;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_sustento_id")
    private Documento documentoSustento;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
