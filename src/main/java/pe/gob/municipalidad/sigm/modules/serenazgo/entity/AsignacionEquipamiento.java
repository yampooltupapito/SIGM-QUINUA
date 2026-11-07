package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAsignacionRecurso;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_equipamiento_asignaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionEquipamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipamiento_id", nullable = false)
    private EquipamientoSerenazgo equipamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id")
    private PersonalSerenazgo personal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id")
    private UnidadMovilSerenazgo unidad;

    @Column(name = "fecha_entrega", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaEntrega = OffsetDateTime.now();

    @Column(name = "fecha_devolucion")
    private OffsetDateTime fechaDevolucion;

    @Column(name = "cantidad", nullable = false)
    @Builder.Default
    private Integer cantidad = 1;

    @Column(name = "cantidad_devuelta", nullable = false)
    @Builder.Default
    private Integer cantidadDevuelta = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_asignacion", nullable = false, length = 30)
    @Builder.Default
    private EstadoAsignacionRecurso estadoAsignacion = EstadoAsignacionRecurso.ACTIVA;

    @Column(name = "estado_bien_entrega", length = 50)
    private String estadoBienEntrega;

    @Column(name = "estado_bien_devolucion", length = 50)
    private String estadoBienDevolucion;

    @Column(name = "motivo", nullable = false, length = 255)
    private String motivo;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_acta_id")
    private Documento documentoActa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_entrega_id")
    private Usuario usuarioEntrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_recepcion_id")
    private Usuario usuarioRecepcion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
