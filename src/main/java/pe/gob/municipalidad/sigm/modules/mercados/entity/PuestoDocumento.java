package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;

import java.time.OffsetDateTime;

/**
 * Asociación entre un puesto comercial y un documento del sistema de gestión documental.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(
        name = "mercados_puestos_documentos",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_mercados_puestos_documentos",
                columnNames = {"puesto_id", "documento_id"}
        )
)
public class PuestoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id", nullable = false)
    private PuestoComercial puesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    @Builder.Default
    @Column(name = "tipo_documento", nullable = false, length = 50)
    private String tipoDocumento = "PLANO_PUESTO";

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
