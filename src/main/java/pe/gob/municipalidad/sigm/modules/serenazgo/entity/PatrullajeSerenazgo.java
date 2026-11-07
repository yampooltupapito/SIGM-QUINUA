package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPatrullajeSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoPatrullajeSerenazgo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_patrullajes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatrullajeSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private SectorPatrullaje sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id")
    private ZonaPatrullaje zona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id")
    private RutaPatrullaje ruta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programacion_servicio_id")
    private ProgramacionServicioSerenazgo programacionServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_movil_id")
    private UnidadMovilSerenazgo unidadMovil;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_patrullaje", nullable = false, length = 40)
    @Builder.Default
    private TipoPatrullajeSerenazgo tipoPatrullaje = TipoPatrullajeSerenazgo.PATRULLAJE_MUNICIPAL;

    @Column(name = "oficina_responsable_id")
    private Long oficinaResponsableId;

    // Fechas Programadas
    @Column(name = "fecha_hora_inicio_programada")
    private OffsetDateTime fechaHoraInicioProgramada;

    @Column(name = "fecha_hora_fin_programada")
    private OffsetDateTime fechaHoraFinProgramada;

    // Fechas Reales Efectivas
    @Column(name = "fecha_hora_inicio_real")
    private OffsetDateTime fechaHoraInicioReal;

    @Column(name = "fecha_hora_fin_real")
    private OffsetDateTime fechaHoraFinReal;

    // Campos históricos compatibles
    @Column(name = "fecha_inicio", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaInicio = OffsetDateTime.now();

    @Column(name = "fecha_fin")
    private OffsetDateTime fechaFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = false)
    private PersonalSerenazgo responsable;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoPatrullajeSerenazgo estado = EstadoPatrullajeSerenazgo.PROGRAMADO;

    @Column(name = "objetivo_operativo", columnDefinition = "TEXT")
    private String objetivoOperativo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "resultado_administrativo", columnDefinition = "TEXT")
    private String resultadoAdministrativo;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "km_inicio", precision = 8, scale = 2)
    private BigDecimal kmInicio;

    @Column(name = "km_fin", precision = 8, scale = 2)
    private BigDecimal kmFin;

    @Column(name = "motivo_pausa", columnDefinition = "TEXT")
    private String motivoPausa;

    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    @Column(name = "cambio_ruta_motivo", columnDefinition = "TEXT")
    private String cambioRutaMotivo;

    @Column(name = "es_extraordinario", nullable = false)
    @Builder.Default
    private Boolean esExtraordinario = false;

    @Column(name = "motivo_extraordinario", columnDefinition = "TEXT")
    private String motivoExtraordinario;

    @OneToMany(mappedBy = "patrullaje", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ParticipantePatrullajeSerenazgo> participantes = new ArrayList<>();

    @OneToMany(mappedBy = "patrullaje", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PuntoRecorridoPatrullaje> puntosRecorrido = new ArrayList<>();

    @OneToMany(mappedBy = "patrullaje", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordenVisita ASC")
    @Builder.Default
    private List<PatrullajeVisitaPunto> visitasPuntos = new ArrayList<>();

    @OneToMany(mappedBy = "patrullaje", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaHora DESC")
    @Builder.Default
    private List<PatrullajeNovedad> novedades = new ArrayList<>();

    @OneToMany(mappedBy = "patrullaje", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PatrullajeEquipamiento> equipos = new ArrayList<>();

    @OneToMany(mappedBy = "patrullaje", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha DESC")
    @Builder.Default
    private List<PatrullajeHistorial> historial = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
