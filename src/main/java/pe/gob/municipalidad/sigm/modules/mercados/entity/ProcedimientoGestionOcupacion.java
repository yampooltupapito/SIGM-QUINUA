package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoSolicitudGestion;

import java.time.OffsetDateTime;

/**
 * Catálogo de procedimientos institucionales para renovaciones, cambios de titular y transferencias.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_procedimientos_gestion")
public class ProcedimientoGestionOcupacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_solicitud", nullable = false, length = 50)
    private TipoSolicitudGestion tipoSolicitud;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "base_normativa", columnDefinition = "TEXT")
    private String baseNormativa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_competente_id")
    private Oficina oficinaCompetente;

    @Builder.Default
    @Column(name = "requiere_titular_propuesto", nullable = false)
    private Boolean requiereTitularPropuesto = false;

    @Builder.Default
    @Column(name = "permite_transferencia", nullable = false)
    private Boolean permiteTransferencia = false;

    @Builder.Default
    @Column(name = "plazo_dias_habiles")
    private Integer plazoDiasHabiles = 15;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
