package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoExportacionReporte;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.FormatoExportacionReporte;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Trazabilidad y auditoría de descargas y exportaciones del centro de reportes tributarios.
 */
@Entity
@Table(name = "tributaria_historial_exportaciones_reporte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialExportacionReporteTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "reporte_codigo", length = 50, nullable = false)
    private String reporteCodigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "oficina_id")
    private Long oficinaId;

    @Column(name = "fecha_solicitud", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaSolicitud = OffsetDateTime.now();

    @Column(name = "fecha_finalizacion")
    private OffsetDateTime fechaFinalizacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "formato", length = 10, nullable = false)
    private FormatoExportacionReporte formato;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    @Builder.Default
    private EstadoExportacionReporte estado = EstadoExportacionReporte.PENDIENTE;

    @Column(name = "filtros_aplicados", columnDefinition = "TEXT")
    private String filtrosAplicados;

    @Column(name = "tamanio_bytes")
    private Long tamanioBytes;

    @Column(name = "nombre_archivo", length = 200)
    private String nombreArchivo;

    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    @Column(name = "mensaje_error", length = 500)
    private String mensajeError;
}

