package pe.gob.municipalidad.sigm.common.document.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad que representa un documento o archivo en el acervo digital del SIGM-QUINUA.
 */
@Entity
@Table(name = "documentos", indexes = {
        @Index(name = "idx_documentos_uuid", columnList = "identificador_publico"),
        @Index(name = "idx_documentos_hash", columnList = "hash_sha256"),
        @Index(name = "idx_documentos_categoria", columnList = "categoria"),
        @Index(name = "idx_documentos_estado", columnList = "estado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identificador_publico", nullable = false, unique = true, updatable = false)
    private UUID identificadorPublico;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "nombre_interno", nullable = false, unique = true, length = 255)
    private String nombreInterno;

    @Column(name = "extension", nullable = false, length = 20)
    private String extension;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "hash_sha256", nullable = false, length = 64)
    private String hashSha256;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 50)
    private CategoriaDocumento categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "clasificacion_acceso", nullable = false, length = 30)
    private ClasificacionAcceso clasificacionAcceso;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoDocumento estado;

    @Column(name = "version_actual", nullable = false)
    @Builder.Default
    private Integer versionActual = 1;

    @Column(name = "bloqueo_eliminacion", nullable = false)
    @Builder.Default
    private Boolean bloqueoEliminacion = false;

    @Column(name = "motivo_eliminacion", length = 500)
    private String motivoEliminacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_propietario_id")
    private Usuario usuarioPropietario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @Column(name = "modulo_origen", length = 50)
    @Builder.Default
    private String moduloOrigen = "SISTEMA";

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;

    @Column(name = "fecha_eliminacion_logica")
    private OffsetDateTime fechaEliminacionLogica;

    @PrePersist
    public void prePersist() {
        if (identificadorPublico == null) {
            identificadorPublico = UUID.randomUUID();
        }
        if (categoria == null) {
            categoria = CategoriaDocumento.OTRO;
        }
        if (clasificacionAcceso == null) {
            clasificacionAcceso = ClasificacionAcceso.INTERNO;
        }
        if (estado == null) {
            estado = EstadoDocumento.ACTIVO;
        }
        if (versionActual == null) {
            versionActual = 1;
        }
        if (bloqueoEliminacion == null) {
            bloqueoEliminacion = false;
        }
        if (moduloOrigen == null) {
            moduloOrigen = "SISTEMA";
        }
        OffsetDateTime now = OffsetDateTime.now();
        fechaCreacion = now;
        fechaActualizacion = now;
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = OffsetDateTime.now();
    }

    public boolean isVisualizable() {
        if (mimeType == null) return false;
        String mime = mimeType.toLowerCase();
        return mime.equals("application/pdf")
                || mime.startsWith("image/");
    }
}
