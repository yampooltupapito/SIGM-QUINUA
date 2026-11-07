package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad para la trazabilidad y auditoría de exportaciones de reportes institucionales.
 */
@Entity
@Table(name = "mpv_reporte_exportaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteExportacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "tipo_reporte", nullable = false, length = 80)
    private String tipoReporte;

    @Column(nullable = false, length = 20)
    private String formato;

    @Column(name = "filtros_json", columnDefinition = "TEXT")
    private String filtrosJson;

    @Column(name = "cantidad_registros", nullable = false)
    @Builder.Default
    private Integer cantidadRegistros = 0;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String estado = "COMPLETADO";

    @Column(name = "tiempo_generacion_ms")
    private Long tiempoGeneracionMs;

    @Column(name = "ip_origen", length = 50)
    private String ipOrigen;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "fecha_exportacion", nullable = false)
    private OffsetDateTime fechaExportacion;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (fechaExportacion == null) {
            fechaExportacion = OffsetDateTime.now();
        }
        if (estado == null) {
            estado = "COMPLETADO";
        }
        if (cantidadRegistros == null) {
            cantidadRegistros = 0;
        }
    }
}
