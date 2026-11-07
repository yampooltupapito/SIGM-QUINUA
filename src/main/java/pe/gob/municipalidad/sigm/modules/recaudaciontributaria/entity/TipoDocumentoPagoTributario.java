package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;

import java.util.UUID;

/**
 * Catálogo configurable de tipos documentales de pago y recaudación tributaria.
 */
@Entity
@Table(name = "tributaria_tipos_documento_pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class TipoDocumentoPagoTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "requiere_numeracion", nullable = false)
    @Builder.Default
    private Boolean requiereNumeracion = true;

    @Column(name = "requiere_autorizacion", nullable = false)
    @Builder.Default
    private Boolean requiereAutorizacion = false;

    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private String estado = "ACTIVO";
}
