package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "mercados_reporte_configuraciones")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteMercadoConfiguracion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tipo_fuente", nullable = false, length = 50)
    @Builder.Default
    private String tipoFuente = "INTERNO";

    @Column(name = "configuracion_columnas", columnDefinition = "jsonb")
    private String configuracionColumnas;

    @Column(name = "configuracion_filtros", columnDefinition = "jsonb")
    private String configuracionFiltros;

    @Column(name = "formato_permitido", nullable = false, length = 50)
    @Builder.Default
    private String formatoPermitido = "AMBOS";

    @Column(name = "permiso_requerido", length = 100)
    private String permisoRequerido;

    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private String estado = "ACTIVO";

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
