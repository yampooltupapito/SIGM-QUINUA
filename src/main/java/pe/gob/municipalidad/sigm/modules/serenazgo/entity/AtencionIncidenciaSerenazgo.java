package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_atenciones_incidencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionIncidenciaSerenazgo {

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
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_asignador_id")
    private Usuario supervisorAsignador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_operativo_id")
    private PersonalSerenazgo responsableOperativo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_movil_id")
    private UnidadMovilSerenazgo unidadMovil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private SectorPatrullaje sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id")
    private ZonaPatrullaje zona;

    @Column(name = "fecha_hora_asignacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaHoraAsignacion = OffsetDateTime.now();

    @Column(name = "fecha_hora_despacho")
    private OffsetDateTime fechaHoraDespacho;

    @Column(name = "fecha_hora_aceptacion")
    private OffsetDateTime fechaHoraAceptacion;

    @Column(name = "fecha_hora_rechazo")
    private OffsetDateTime fechaHoraRechazo;

    @Column(name = "fecha_hora_inicio")
    private OffsetDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_finalizacion")
    private OffsetDateTime fechaHoraFinalizacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoAtencionIncidencia estado = EstadoAtencionIncidencia.PENDIENTE_ASIGNACION;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad_operativa", nullable = false, length = 30)
    @Builder.Default
    private PrioridadIncidenciaSeguridad prioridadOperativa = PrioridadIncidenciaSeguridad.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_comunicacion", nullable = false, length = 50)
    @Builder.Default
    private CanalComunicacionDespacho canalComunicacion = CanalComunicacionDespacho.RADIO_TETRA_VHF;

    @Column(name = "instrucciones_despacho", columnDefinition = "TEXT")
    private String instruccionesDespacho;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "motivo_espera", columnDefinition = "TEXT")
    private String motivoEspera;

    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado_administrativo", length = 50)
    private ResultadoAtencionIncidencia resultadoAdministrativo;

    @Column(name = "motivo_finalizacion", columnDefinition = "TEXT")
    private String motivoFinalizacion;

    @Column(name = "observaciones_operativas", columnDefinition = "TEXT")
    private String observacionesOperativas;

    @OneToMany(mappedBy = "atencion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AtencionPersonalSerenazgo> personalAsignado = new ArrayList<>();

    @OneToMany(mappedBy = "atencion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AtencionEquipamientoSerenazgo> equipamientoAsignado = new ArrayList<>();

    @OneToMany(mappedBy = "atencion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AtencionEventoSerenazgo> eventos = new ArrayList<>();

    @OneToMany(mappedBy = "atencion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AtencionEscalamientoSerenazgo> escalamientos = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

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
