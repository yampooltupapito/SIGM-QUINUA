package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalAlertaCiudadana;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoAlertaCiudadana;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_alertas_ciudadanas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaCiudadanaSeguridad {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "titulo", nullable = false, length = 200)
    @Builder.Default
    private String titulo = "Reporte de Seguridad Ciudadana";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_reporte_id")
    private TipoIncidenciaSeguridad tipoReporte;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origen", nullable = false, length = 50)
    @Builder.Default
    private CanalAlertaCiudadana canalOrigen = CanalAlertaCiudadana.PORTAL_CIUDADANO;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_recepcion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaRecepcion = OffsetDateTime.now();

    @Column(name = "fecha_ocurrencia")
    private OffsetDateTime fechaOcurrencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 30)
    @Builder.Default
    private PrioridadIncidenciaSeguridad prioridad = PrioridadIncidenciaSeguridad.MEDIA;

    @Column(name = "ubicacion_referencia", nullable = false)
    private String ubicacionReferencia;

    @Column(name = "latitud", precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 7)
    private BigDecimal longitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private SectorPatrullaje sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id")
    private ZonaPatrullaje zona;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoAlertaCiudadana estado = EstadoAlertaCiudadana.RECIBIDA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportante_usuario_id")
    private Usuario reportanteUsuario;

    @Column(name = "reportante_anonimo", nullable = false)
    @Builder.Default
    private Boolean reportanteAnonimo = false;

    @Column(name = "reportante_nombre", length = 150)
    private String reportanteNombre;

    @Column(name = "reportante_email", length = 120)
    private String reportanteEmail;

    @Column(name = "reportante_telefono_contacto", length = 30)
    private String reportanteTelefonoContacto;

    @Column(name = "clave_consulta_hash", length = 120)
    private String claveConsultaHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidencia_id")
    private IncidenciaSeguridadCiudadana incidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_confidencialidad", nullable = false, length = 30)
    @Builder.Default
    private NivelConfidencialidad nivelConfidencialidad = NivelConfidencialidad.ORDINARIO;

    @Column(name = "observaciones_internas", columnDefinition = "TEXT")
    private String observacionesInternas;

    @Column(name = "motivo_cierre", columnDefinition = "TEXT")
    private String motivoCierre;

    @Column(name = "resultado_ciudadano", columnDefinition = "TEXT")
    private String resultadoCiudadano;

    @Column(name = "motivo_descarte", columnDefinition = "TEXT")
    private String motivoDescarte;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
