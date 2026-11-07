package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAsignacionRecurso;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_unidades_asignaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionUnidadMovil {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private UnidadMovilSerenazgo unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private PersonalSerenazgo personal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id")
    private Oficina oficina;

    @Column(name = "fecha_inicio", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaInicio = OffsetDateTime.now();

    @Column(name = "fecha_fin_programada")
    private OffsetDateTime fechaFinProgramada;

    @Column(name = "fecha_devolucion_real")
    private OffsetDateTime fechaDevolucionReal;

    @Column(name = "kilometraje_salida", precision = 10, scale = 2)
    private BigDecimal kilometrajeSalida;

    @Column(name = "kilometraje_retorno", precision = 10, scale = 2)
    private BigDecimal kilometrajeRetorno;

    @Column(name = "motivo_servicio", nullable = false, length = 255)
    private String motivoServicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_asignacion", nullable = false, length = 30)
    @Builder.Default
    private EstadoAsignacionRecurso estadoAsignacion = EstadoAsignacionRecurso.ACTIVA;

    @Column(name = "estado_inicial_unidad", length = 30)
    private String estadoInicialUnidad;

    @Column(name = "estado_retorno_unidad", length = 30)
    private String estadoRetornoUnidad;

    @Column(name = "observaciones_entrega", columnDefinition = "TEXT")
    private String observacionesEntrega;

    @Column(name = "observaciones_devolucion", columnDefinition = "TEXT")
    private String observacionesDevolucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_entrega_id")
    private Usuario usuarioEntrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_recepcion_id")
    private Usuario usuarioRecepcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_acta_entrega_id")
    private Documento documentoActaEntrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_acta_devolucion_id")
    private Documento documentoActaDevolucion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
