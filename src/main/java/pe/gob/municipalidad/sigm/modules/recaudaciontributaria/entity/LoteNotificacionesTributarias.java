package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoLoteNotificaciones;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad que agrupa un lote controlado de emisiones de notificaciones tributarias.
 */
@Entity
@Table(name = "tributaria_lotes_notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class LoteNotificacionesTributarias extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_notificacion_id", nullable = false)
    private TipoNotificacionTributaria tipoNotificacion;

    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaCreacion = OffsetDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoLoteNotificaciones estado = EstadoLoteNotificaciones.BORRADOR;

    @Column(name = "total_registros", nullable = false)
    @Builder.Default
    private Integer totalRegistros = 0;

    @Column(name = "total_validos", nullable = false)
    @Builder.Default
    private Integer totalValidos = 0;

    @Column(name = "total_observados", nullable = false)
    @Builder.Default
    private Integer totalObservados = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id")
    private Oficina oficina;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Version
    @Column(name = "version")
    @Builder.Default
    private Long version = 0L;
}

