package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoEvaluacionInfraccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.SentidoDecisionInfraccion;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_presuntas_infracciones")
public class PresuntaInfraccionMercado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidencia_id")
    private IncidenciaMercado incidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspeccion_id")
    private InspeccionMercado inspeccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hallazgo_id")
    private HallazgoInspeccionMercado hallazgo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mercado_id", nullable = false)
    private MercadoMunicipal mercado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private SectorMercado sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pabellon_id")
    private PabellonMercado pabellon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id")
    private PuestoComercial puesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comerciante_id")
    private Comerciante comerciante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipificacion_id")
    private TipificacionInfraccion tipificacion;

    @Column(name = "descripcion_hechos", nullable = false, columnDefinition = "TEXT")
    private String descripcionHechos;

    @Column(name = "fecha_deteccion", nullable = false)
    private LocalDate fechaDeteccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_evaluacion", nullable = false, length = 30)
    @Builder.Default
    private EstadoEvaluacionInfraccion estadoEvaluacion = EstadoEvaluacionInfraccion.REGISTRADA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_evaluacion_id")
    private Usuario responsableEvaluacion;

    @Column(name = "informe_evaluacion", columnDefinition = "TEXT")
    private String informeEvaluacion;

    @Column(name = "fecha_evaluacion")
    private OffsetDateTime fechaEvaluacion;

    @Column(name = "motivo_descarte", columnDefinition = "TEXT")
    private String motivoDescarte;

    @Column(name = "expediente_id")
    private UUID expedienteId;

    @Column(name = "expediente_numero", length = 64)
    private String expedienteNumero;

    @Column(name = "tiene_descargo", nullable = false)
    @Builder.Default
    private Boolean tieneDescargo = false;

    @Column(name = "fecha_descargo")
    private OffsetDateTime fechaDescargo;

    @Column(name = "documento_descargo_ref", length = 255)
    private String documentoDescargoRef;

    @Column(name = "resumen_descargo", columnDefinition = "TEXT")
    private String resumenDescargo;

    @Column(name = "resolucion_decision_ref", length = 255)
    private String resolucionDecisionRef;

    @Column(name = "fecha_decision")
    private LocalDate fechaDecision;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentido_decision", length = 50)
    private SentidoDecisionInfraccion sentidoDecision;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;
}
