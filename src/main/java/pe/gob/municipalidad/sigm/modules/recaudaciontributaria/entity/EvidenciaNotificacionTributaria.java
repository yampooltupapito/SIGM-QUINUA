package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoEvidenciaNotificacion;

import java.util.UUID;

/**
 * Registro de evidencias adjuntadas para sustentar la diligencia o cargo.
 */
@Entity
@Table(name = "tributaria_evidencias_notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class EvidenciaNotificacionTributaria extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacion_id", nullable = false)
    private NotificacionTributaria notificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intento_id")
    private IntentoNotificacionTributaria intento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evidencia", nullable = false, length = 40)
    private TipoEvidenciaNotificacion tipoEvidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "hash_archivo", nullable = false, length = 64)
    private String hashArchivo;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}

