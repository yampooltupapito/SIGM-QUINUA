package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_documentos", indexes = {
        @Index(name = "idx_serenazgo_docs_codigo", columnList = "codigo"),
        @Index(name = "idx_serenazgo_docs_tipo", columnList = "tipo_documento"),
        @Index(name = "idx_serenazgo_docs_estado", columnList = "estado"),
        @Index(name = "idx_serenazgo_docs_origen", columnList = "origen_documento"),
        @Index(name = "idx_serenazgo_docs_incidencia", columnList = "incidencia_id"),
        @Index(name = "idx_serenazgo_docs_alerta", columnList = "alerta_id"),
        @Index(name = "idx_serenazgo_docs_atencion", columnList = "atencion_id"),
        @Index(name = "idx_serenazgo_docs_intervencion", columnList = "intervencion_id"),
        @Index(name = "idx_serenazgo_docs_patrullaje", columnList = "patrullaje_id"),
        @Index(name = "idx_serenazgo_docs_fecha_carga", columnList = "fecha_hora_carga"),
        @Index(name = "idx_serenazgo_docs_hash", columnList = "hash_sha256")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoSeguridadCiudadana {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 50)
    private TipoDocumentoSeguridad tipoDocumento;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "nombre_almacenado", nullable = false, length = 255)
    private String nombreAlmacenado;

    @Column(name = "extension", nullable = false, length = 20)
    private String extension;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "hash_sha256", nullable = false, length = 64)
    private String hashSha256;

    @Column(name = "fecha_hora_carga", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaHoraCarga = OffsetDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_carga_id", nullable = false)
    private Usuario usuarioCarga;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_documento", nullable = false, length = 50)
    @Builder.Default
    private OrigenDocumentoSeguridad origenDocumento = OrigenDocumentoSeguridad.ADMINISTRATIVO;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    @Builder.Default
    private EstadoDocumentoSeguridad estado = EstadoDocumentoSeguridad.REGISTRADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_confidencialidad", nullable = false, length = 30)
    @Builder.Default
    private NivelConfidencialidad nivelConfidencialidad = NivelConfidencialidad.CONFIDENCIAL;

    @Column(name = "fecha_hora_validacion")
    private OffsetDateTime fechaHoraValidacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_validador_id")
    private Usuario usuarioValidador;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "motivo_restriccion", columnDefinition = "TEXT")
    private String motivoRestriccion;

    @Column(name = "motivo_archivado", columnDefinition = "TEXT")
    private String motivoArchivado;

    @Column(name = "version_actual", nullable = false)
    @Builder.Default
    private Integer versionActual = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_padre_id")
    private DocumentoSeguridadCiudadana documentoPadre;

    // Vinculaciones directas con actuaciones municipales
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidencia_id")
    private IncidenciaSeguridadCiudadana incidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alerta_id")
    private AlertaCiudadanaSeguridad alerta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atencion_id")
    private AtencionIncidenciaSerenazgo atencion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intervencion_id")
    private IntervencionSerenazgo intervencion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrullaje_id")
    private PatrullajeSerenazgo patrullaje;

    // Metadatos de campo
    @Column(name = "fecha_captura")
    private OffsetDateTime fechaCaptura;

    @Column(name = "ubicacion_captura", length = 255)
    private String ubicacionCaptura;

    @Column(name = "latitud", precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 7)
    private BigDecimal longitud;

    @Column(name = "autoria_declarada", length = 200)
    private String autoriaDeclarada;

    // Control de Integridad Digital
    @Enumerated(EnumType.STRING)
    @Column(name = "ultimo_resultado_verificacion", nullable = false, length = 50)
    @Builder.Default
    private ResultadoVerificacionIntegridad ultimoResultadoVerificacion = ResultadoVerificacionIntegridad.NO_VERIFICADO;

    @Column(name = "fecha_ultima_verificacion")
    private OffsetDateTime fechaUltimaVerificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_ultima_verificacion_id")
    private Usuario usuarioUltimaVerificacion;

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

    @OneToMany(mappedBy = "documentoSeguridad", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentoSeguridadRelacion> relaciones = new ArrayList<>();

    @OneToMany(mappedBy = "documentoSeguridad", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentoSeguridadVerificacion> verificaciones = new ArrayList<>();

    @OneToMany(mappedBy = "documentoSeguridad", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentoSeguridadHistorial> historial = new ArrayList<>();
}
