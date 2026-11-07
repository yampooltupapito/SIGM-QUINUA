package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoGestionDeuda;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Registro de gestión administrativa individual realizada durante el seguimiento de una deuda.
 */
@Entity
@Table(name = "tributaria_gestiones_deuda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class GestionSeguimientoDeuda extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seguimiento_id", nullable = false)
    private SeguimientoDeudaTributaria seguimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_gestion", nullable = false, length = 40)
    private TipoGestionDeuda tipoGestion;

    @Column(name = "fecha_gestion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaGestion = OffsetDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @Column(name = "resultado", nullable = false, length = 150)
    private String resultado;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_sustento_id")
    private Documento documentoSustento;
}
