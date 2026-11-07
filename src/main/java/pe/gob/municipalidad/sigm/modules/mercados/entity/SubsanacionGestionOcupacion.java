package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.modules.mercados.enums.ResultadoSubsanacionGestion;

import java.time.OffsetDateTime;

/**
 * Registro de subsanaciones y levantamiento de observaciones presentadas por los administrados.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"observacion", "solicitud", "documento"})
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_solicitudes_subsanaciones")
public class SubsanacionGestionOcupacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "observacion_id", nullable = false)
    private ObservacionGestionOcupacion observacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudGestionOcupacion solicitud;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Builder.Default
    @Column(name = "fecha_subsanacion", nullable = false)
    private OffsetDateTime fechaSubsanacion = OffsetDateTime.now();

    @Column(name = "usuario_subsanador", nullable = false, length = 100)
    private String usuarioSubsanador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "resultado_revision", nullable = false, length = 50)
    private ResultadoSubsanacionGestion resultadoRevision = ResultadoSubsanacionGestion.PENDIENTE;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
