package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.CategoriaTipoNotificacion;

import java.util.UUID;

/**
 * Catálogo de tipos de comunicaciones y notificaciones tributarias municipales.
 */
@Entity
@Table(name = "tributaria_tipos_notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class TipoNotificacionTributaria extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 40)
    private CategoriaTipoNotificacion categoria;

    @Column(name = "requiere_documento", nullable = false)
    @Builder.Default
    private Boolean requiereDocumento = false;

    @Column(name = "requiere_validacion_legal", nullable = false)
    @Builder.Default
    private Boolean requiereValidacionLegal = false;

    @Column(name = "requiere_evidencia", nullable = false)
    @Builder.Default
    private Boolean requiereEvidencia = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "norma_id")
    private NormaTributariaConfigurada norma;

    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private String estado = "ACTIVO";

    @Version
    @Column(name = "version")
    @Builder.Default
    private Long version = 0L;
}

