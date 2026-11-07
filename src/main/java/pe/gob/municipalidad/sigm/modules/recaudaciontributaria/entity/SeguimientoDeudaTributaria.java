package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ClasificacionDeudaTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoSeguimientoDeuda;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Expediente y registro de seguimiento administrativo de una obligación tributaria pendiente o vencida.
 */
@Entity
@Table(
        name = "tributaria_seguimiento_deudas",
        uniqueConstraints = @UniqueConstraint(name = "uq_trib_seg_deuda_obligacion", columnNames = {"obligacion_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class SeguimientoDeudaTributaria extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obligacion_id", nullable = false, unique = true)
    private ObligacionTributaria obligacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private ContribuyenteTributario contribuyente;

    @Enumerated(EnumType.STRING)
    @Column(name = "clasificacion", nullable = false, length = 40)
    private ClasificacionDeudaTributaria clasificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_seguimiento", nullable = false, length = 30)
    @Builder.Default
    private EstadoSeguimientoDeuda estadoSeguimiento = EstadoSeguimientoDeuda.EN_SEGUIMIENTO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id")
    private Oficina oficina;

    @Column(name = "fecha_inicio", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaInicio = OffsetDateTime.now();

    @Column(name = "fecha_ultima_revision", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaUltimaRevision = OffsetDateTime.now();

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "seguimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaGestion DESC")
    @Builder.Default
    private List<GestionSeguimientoDeuda> gestiones = new ArrayList<>();

    @OneToMany(mappedBy = "seguimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaComprometida ASC")
    @Builder.Default
    private List<CompromisoSeguimientoDeuda> compromisos = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
