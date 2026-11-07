package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.CanalIngreso;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.ConfidencialidadExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.PrioridadExpediente;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mpv_expedientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "numero_expediente", unique = true, length = 50)
    private String numeroExpediente;

    @Column(name = "codigo_seguimiento", unique = true, length = 64)
    private String codigoSeguimiento;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false, length = 255)
    private String asunto;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_tramite_id", nullable = false)
    private TipoTramite tipoTramite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Persona solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_origen_id")
    private Oficina oficinaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id", nullable = false)
    private Oficina oficinaResponsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_actual_id")
    private Usuario responsableActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_actual", nullable = false, length = 30)
    @Builder.Default
    private EstadoExpediente estadoActual = EstadoExpediente.BORRADOR;

    @Column(name = "fecha_presentacion")
    private OffsetDateTime fechaPresentacion;

    @Column(name = "fecha_recepcion")
    private OffsetDateTime fechaRecepcion;

    @Column(name = "fecha_limite")
    private OffsetDateTime fechaLimite;

    @Column(name = "fecha_cierre")
    private OffsetDateTime fechaCierre;

    @Column(name = "fecha_inicio_atencion")
    private OffsetDateTime fechaInicioAtencion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_atencion_id")
    private Usuario usuarioAtencion;

    @Column(name = "resultado_final", length = 40)
    private String resultadoFinal;

    @Column(name = "ubicacion_archivo", length = 200)
    private String ubicacionArchivo;

    @Column(name = "es_reabierto", nullable = false)
    @Builder.Default
    private Boolean esReabierto = false;

    @Column(name = "motivo_reapertura", columnDefinition = "TEXT")
    private String motivoReapertura;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_ingreso", nullable = false, length = 20)
    @Builder.Default
    private CanalIngreso canalIngreso = CanalIngreso.VIRTUAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PrioridadExpediente prioridad = PrioridadExpediente.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConfidencialidadExpediente confidencialidad = ConfidencialidadExpediente.INTERNO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id")
    private Usuario usuarioRegistro;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToMany(mappedBy = "expediente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExpedienteDocumento> documentos = new ArrayList<>();

    @OneToMany(mappedBy = "expediente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExpedienteHistorial> historial = new ArrayList<>();

    @OneToMany(mappedBy = "expediente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DerivacionExpediente> derivaciones = new ArrayList<>();

    @OneToMany(mappedBy = "expediente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ObservacionExpediente> observaciones = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (estadoActual == null) {
            estadoActual = EstadoExpediente.BORRADOR;
        }
        if (canalIngreso == null) {
            canalIngreso = CanalIngreso.VIRTUAL;
        }
        if (prioridad == null) {
            prioridad = PrioridadExpediente.NORMAL;
        }
        if (confidencialidad == null) {
            confidencialidad = ConfidencialidadExpediente.INTERNO;
        }
        if (anio == null) {
            anio = OffsetDateTime.now().getYear();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
