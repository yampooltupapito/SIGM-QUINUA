package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoObservacionGestion;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Observaciones y requerimientos emitidos durante la revisión de una solicitud de ocupación.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"solicitud", "documentoSustento", "subsanaciones"})
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_solicitudes_observaciones")
public class ObservacionGestionOcupacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudGestionOcupacion solicitud;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "requisito_relacionado", length = 255)
    private String requisitoRelacionado;

    @Builder.Default
    @Column(name = "fecha_observacion", nullable = false)
    private OffsetDateTime fechaObservacion = OffsetDateTime.now();

    @Column(name = "usuario_observador", nullable = false, length = 100)
    private String usuarioObservador;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoObservacionGestion estado = EstadoObservacionGestion.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private Documento documentoSustento;

    @Builder.Default
    @OneToMany(mappedBy = "observacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubsanacionGestionOcupacion> subsanaciones = new ArrayList<>();

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
