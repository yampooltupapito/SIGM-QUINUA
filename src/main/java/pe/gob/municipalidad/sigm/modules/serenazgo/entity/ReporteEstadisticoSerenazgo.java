package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoReporteEstadistico;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.FormatoReporteEstadistico;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoPeriodoEstadistico;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoReporteEstadistico;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad que registra el historial de reportes estadísticos e institucionales
 * generados por funcionarios autorizados de Seguridad Ciudadana.
 */
@Entity
@Table(name = "serenazgo_reportes_estadisticos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteEstadisticoSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_reporte", nullable = false, length = 60)
    private TipoReporteEstadistico tipoReporte;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "formato", nullable = false, length = 20)
    private FormatoReporteEstadistico formato;

    @Column(name = "parametros_json", columnDefinition = "TEXT")
    private String parametrosJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodo_tipo", nullable = false, length = 40)
    private TipoPeriodoEstadistico periodoTipo;

    @Column(name = "fecha_inicio")
    private OffsetDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private OffsetDateTime fechaFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private SectorPatrullaje sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id")
    private ZonaPatrullaje zona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_generador_id", nullable = false)
    private Usuario usuarioGenerador;

    @Column(name = "username_generador", nullable = false, length = 100)
    private String usernameGenerador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id")
    private Oficina oficina;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoReporteEstadistico estado = EstadoReporteEstadistico.COMPLETADO;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "tamanio_bytes", nullable = false)
    @Builder.Default
    private Long tamanioBytes = 0L;

    @Column(name = "ruta_archivo", length = 500)
    private String rutaArchivo;

    @Column(name = "documento_id")
    private Long documentoId;

    @Column(name = "error_detalle", columnDefinition = "TEXT")
    private String errorDetalle;

    @Column(name = "fecha_generacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaGeneracion = OffsetDateTime.now();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
