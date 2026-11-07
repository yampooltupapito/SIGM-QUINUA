package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.util.UUID;

/**
 * Filtros y configuraciones preferidas guardadas por usuarios del Centro de Reportes.
 */
@Entity
@Table(name = "tributaria_preferencias_reportes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenciaReporteTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "reporte_codigo", length = 50, nullable = false)
    private String reporteCodigo;

    @Column(name = "nombre_configuracion", length = 100, nullable = false)
    private String nombreConfiguracion;

    @Column(name = "filtros_json", nullable = false, columnDefinition = "TEXT")
    private String filtrosJson;

    @Column(name = "es_predeterminado", nullable = false)
    @Builder.Default
    private Boolean esPredeterminado = false;

    public boolean isEsPredeterminado() {
        return Boolean.TRUE.equals(esPredeterminado);
    }
}

