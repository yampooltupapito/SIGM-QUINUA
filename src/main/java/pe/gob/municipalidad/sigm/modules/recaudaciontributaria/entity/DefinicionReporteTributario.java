package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.CategoriaReporteTributario;

import java.util.UUID;

/**
 * Catálogo maestro de definiciones de reportes tributarios institucionales.
 */
@Entity
@Table(name = "tributaria_definiciones_reportes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefinicionReporteTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", length = 50, nullable = false, unique = true)
    private String codigo;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", length = 40, nullable = false)
    private CategoriaReporteTributario categoria;

    @Column(name = "permiso_requerido", length = 100, nullable = false)
    private String permisoRequerido;

    @Column(name = "formato_permitido", length = 50, nullable = false)
    @Builder.Default
    private String formatoPermitido = "PDF,EXCEL,CSV";

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    public boolean isActivo() {
        return Boolean.TRUE.equals(activo);
    }
}

