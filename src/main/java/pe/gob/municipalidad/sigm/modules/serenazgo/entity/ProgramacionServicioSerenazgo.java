package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.RolServicioSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoServicioSerenazgo;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_programacion_servicios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramacionServicioSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id", nullable = false)
    private TurnoSerenazgo turno;

    @Column(name = "fecha_servicio", nullable = false)
    private LocalDate fechaServicio;

    @Column(name = "fecha_hora_inicio")
    private OffsetDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private OffsetDateTime fechaHoraFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private SectorPatrullaje sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id")
    private ZonaPatrullaje zona;

    // Puntero de compatibilidad con Prompt 36 y vehículo principal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_movil_id")
    private UnidadMovilSerenazgo unidadMovil;

    // Puntero de compatibilidad con Prompt 36 y responsable principal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id")
    private PersonalSerenazgo personal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_servicio_id")
    private PersonalSerenazgo responsableServicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", nullable = false, length = 50)
    @Builder.Default
    private TipoServicioSerenazgo tipoServicio = TipoServicioSerenazgo.PATRULLAJE_PREVENTIVO;

    // Rol de servicio mantenido para compatibilidad
    @Enumerated(EnumType.STRING)
    @Column(name = "rol_servicio", nullable = false, length = 50)
    @Builder.Default
    private RolServicioSerenazgo rolServicio = RolServicioSerenazgo.PATRULLAJE;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoProgramacionServicio estado = EstadoProgramacionServicio.PROGRAMADA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_programacion_id")
    private Usuario responsableProgramacion;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "motivo_reprogramacion", columnDefinition = "TEXT")
    private String motivoReprogramacion;

    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    @OneToMany(mappedBy = "programacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProgramacionPersonal> personalAsignado = new ArrayList<>();

    @OneToMany(mappedBy = "programacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProgramacionUnidad> unidadesReservadas = new ArrayList<>();

    @OneToMany(mappedBy = "programacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProgramacionEquipamiento> equipamientoReservado = new ArrayList<>();

    @OneToMany(mappedBy = "programacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProgramacionHistorial> historial = new ArrayList<>();

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

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
}
