package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;

import java.time.OffsetDateTime;

/**
 * Registro de evaluaciones, observaciones y decisiones técnicas sobre solicitudes de asignación.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"asignacion", "documentoSustento"})
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_asignaciones_evaluaciones")
public class AsignacionEvaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_id", nullable = false)
    private AsignacionPuesto asignacion;

    @Column(name = "tipo_evaluacion", nullable = false, length = 50)
    private String tipoEvaluacion;

    @Column(name = "resultado", nullable = false, length = 50)
    private String resultado;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_sustento_id")
    private Documento documentoSustento;

    @Column(name = "usuario_evaluador", nullable = false, length = 100)
    private String usuarioEvaluador;

    @Builder.Default
    @Column(name = "fecha_evaluacion", nullable = false)
    private OffsetDateTime fechaEvaluacion = OffsetDateTime.now();

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
