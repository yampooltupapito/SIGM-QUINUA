package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_incidencias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaSeguridadCiudadana {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "titulo", nullable = false, length = 200)
    @Builder.Default
    private String titulo = "Incidencia de Seguridad Ciudadana";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_incidencia_id", nullable = false)
    private TipoIncidenciaSeguridad tipoIncidencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 30)
    @Builder.Default
    private PrioridadIncidenciaSeguridad prioridad = PrioridadIncidenciaSeguridad.MEDIA;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_reporte", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaReporte = OffsetDateTime.now();

    @Column(name = "fecha_ocurrencia")
    private OffsetDateTime fechaOcurrencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_hecho", nullable = false, length = 40)
    @Builder.Default
    private OrigenHechoIncidencia origenHecho = OrigenHechoIncidencia.HECHO_REPORTADO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private SectorPatrullaje sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id")
    private ZonaPatrullaje zona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrullaje_id")
    private PatrullajeSerenazgo patrullaje;

    @Column(name = "ubicacion_referencia", nullable = false)
    private String ubicacionReferencia;

    @Column(name = "latitud", precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 7)
    private BigDecimal longitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoIncidenciaSeguridad estado = EstadoIncidenciaSeguridad.REGISTRADA;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_reporte", nullable = false, length = 50)
    @Builder.Default
    private CanalReporteIncidencia canalReporte = CanalReporteIncidencia.LLAMADA_OPERADOR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id", nullable = false)
    private Usuario usuarioRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private PersonalSerenazgo responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_reporta_id")
    private PersonalSerenazgo personalReporta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programacion_servicio_id")
    private ProgramacionServicioSerenazgo programacionServicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_confidencialidad", nullable = false, length = 30)
    @Builder.Default
    private NivelConfidencialidad nivelConfidencialidad = NivelConfidencialidad.ORDINARIO;

    @Column(name = "reportante_anonimo", nullable = false)
    @Builder.Default
    private Boolean reportanteAnonimo = false;

    @Column(name = "reportante_nombre", length = 150)
    private String reportanteNombre;

    @Column(name = "reportante_telefono", length = 30)
    private String reportanteTelefono;

    @Column(name = "reportante_direccion", length = 200)
    private String reportanteDireccion;

    @Column(name = "observaciones_internas", columnDefinition = "TEXT")
    private String observacionesInternas;

    @Column(name = "resultado_atencion", columnDefinition = "TEXT")
    private String resultadoAtencion;

    @Column(name = "motivo_cierre", columnDefinition = "TEXT")
    private String motivoCierre;

    @Column(name = "motivo_descarte", columnDefinition = "TEXT")
    private String motivoDescarte;

    @Column(name = "fecha_cierre")
    private OffsetDateTime fechaCierre;

    @OneToMany(mappedBy = "incidencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IncidenciaHistorial> historial = new ArrayList<>();

    @OneToMany(mappedBy = "incidencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IncidenciaDocumento> documentos = new ArrayList<>();

    @OneToMany(mappedBy = "incidencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IncidenciaComentario> comentarios = new ArrayList<>();

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
