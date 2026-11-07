package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoInspeccionMercado;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_inspecciones")
public class InspeccionMercado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo_inspeccion", nullable = false, unique = true, length = 50)
    private String codigoInspeccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_inspeccion", nullable = false, length = 50)
    private TipoInspeccionMercado tipoInspeccion;

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
    @JoinColumn(name = "inspeccion_origen_id")
    private InspeccionMercado inspeccionOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantilla_id")
    private PlantillaInspeccion plantilla;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDate fechaProgramada;

    @Column(name = "hora_programada")
    private LocalTime horaProgramada;

    @Column(name = "fecha_inicio_real")
    private OffsetDateTime fechaInicioReal;

    @Column(name = "fecha_fin_real")
    private OffsetDateTime fechaFinReal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoInspeccion estado = EstadoInspeccion.BORRADOR;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @Column(name = "objetivo", nullable = false, columnDefinition = "TEXT")
    private String objetivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_responsable_id")
    private Usuario inspectorResponsable;

    @Column(name = "expediente_id")
    private UUID expedienteId;

    @Column(name = "observaciones_generales", columnDefinition = "TEXT")
    private String observacionesGenerales;

    @Column(name = "resultado_general", length = 30)
    private String resultadoGeneral;

    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    @OneToMany(mappedBy = "inspeccion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InspeccionParticipante> participantes = new ArrayList<>();

    @OneToMany(mappedBy = "inspeccion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RespuestaVerificacionInspeccion> verificaciones = new ArrayList<>();

    @OneToMany(mappedBy = "inspeccion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<HallazgoInspeccionMercado> hallazgos = new ArrayList<>();

    @OneToMany(mappedBy = "inspeccion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EvidenciaInspeccionMercado> evidencias = new ArrayList<>();

    @OneToOne(mappedBy = "inspeccion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ActaInspeccionMercado acta;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
