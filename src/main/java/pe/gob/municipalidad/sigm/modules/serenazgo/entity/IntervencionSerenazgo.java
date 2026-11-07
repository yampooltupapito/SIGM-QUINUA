package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_intervenciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidencia_id", nullable = false)
    private IncidenciaSeguridadCiudadana incidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atencion_id")
    private AtencionIncidenciaSerenazgo atencion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_intervencion", nullable = false, length = 50)
    @Builder.Default
    private TipoIntervencionSerenazgo tipoIntervencion = TipoIntervencionSerenazgo.PATRULLAJE_PREVENTIVO;

    @Column(name = "fecha_inicio", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaInicio = OffsetDateTime.now();

    @Column(name = "fecha_fin")
    private OffsetDateTime fechaFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = false)
    private PersonalSerenazgo responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_movil_id")
    private UnidadMovilSerenazgo unidadMovil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private SectorPatrullaje sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id")
    private ZonaPatrullaje zona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrullaje_id")
    private PatrullajeSerenazgo patrullaje;

    @Column(name = "ubicacion_descripcion", columnDefinition = "TEXT")
    private String ubicacionDescripcion;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @Column(name = "descripcion_actuacion", nullable = false, columnDefinition = "TEXT")
    private String descripcionActuacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 50)
    @Builder.Default
    private ResultadoIntervencionSerenazgo resultado = ResultadoIntervencionSerenazgo.ACTUACION_REGISTRADA;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoIntervencionSerenazgo estado = EstadoIntervencionSerenazgo.BORRADOR;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_confidencialidad", nullable = false, length = 30)
    @Builder.Default
    private NivelConfidencialidad nivelConfidencialidad = NivelConfidencialidad.PUBLICO;

    @Column(name = "fecha_validacion")
    private OffsetDateTime fechaValidacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_validador_id")
    private Usuario usuarioValidador;

    @Column(name = "motivo_observacion", columnDefinition = "TEXT")
    private String motivoObservacion;

    @Column(name = "motivo_archivo", columnDefinition = "TEXT")
    private String motivoArchivo;

    @Column(name = "registro_retrospectivo", nullable = false)
    @Builder.Default
    private Boolean registroRetrospectivo = false;

    @Column(name = "motivo_registro_retrospectivo", columnDefinition = "TEXT")
    private String motivoRegistroRetrospectivo;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToMany(mappedBy = "intervencion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ParticipanteIntervencionSerenazgo> participantes = new ArrayList<>();

    @OneToMany(mappedBy = "intervencion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IntervencionActuacionSerenazgo> actuaciones = new ArrayList<>();

    @OneToMany(mappedBy = "intervencion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IntervencionComunicacionSerenazgo> comunicaciones = new ArrayList<>();

    @OneToMany(mappedBy = "intervencion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IntervencionEquipamientoSerenazgo> equipamiento = new ArrayList<>();

    @OneToMany(mappedBy = "intervencion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IntervencionDocumentoSerenazgo> documentos = new ArrayList<>();

    @OneToMany(mappedBy = "intervencion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IntervencionHistorialSerenazgo> historial = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
