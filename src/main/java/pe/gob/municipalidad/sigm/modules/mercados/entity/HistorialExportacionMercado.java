package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mercados.enums.FormatoExportacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoReporteMercado;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "mercados_historial_exportaciones")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialExportacionMercado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_reporte", nullable = false, length = 50)
    private TipoReporteMercado tipoReporte;

    @Enumerated(EnumType.STRING)
    @Column(name = "formato", nullable = false, length = 20)
    private FormatoExportacion formato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mercado_id")
    private MercadoMunicipal mercado;

    @Column(name = "filtros_aplicados", columnDefinition = "TEXT")
    private String filtrosAplicados;

    @Column(name = "fecha_generacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaGeneracion = OffsetDateTime.now();

    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private String estado = "COMPLETADO";

    @Column(name = "cantidad_registros", nullable = false)
    @Builder.Default
    private Integer cantidadRegistros = 0;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "tamanio_bytes", nullable = false)
    @Builder.Default
    private Long tamanioBytes = 0L;

    @Column(name = "documento_id")
    private Long documentoId;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "error_detalle", columnDefinition = "TEXT")
    private String errorDetalle;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
