package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Oficina;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Serie y control de numeración correlativa para emisión de documentos tributarios.
 */
@Entity
@Table(
        name = "tributaria_series_documento_pago",
        uniqueConstraints = @UniqueConstraint(name = "uq_trib_serie_tipo_codigo", columnNames = {"tipo_documento_id", "codigo_serie"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class SerieDocumentoPagoTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_documento_id", nullable = false)
    private TipoDocumentoPagoTributario tipoDocumento;

    @Column(name = "codigo_serie", nullable = false, length = 20)
    private String codigoSerie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id")
    private Oficina oficina;

    @Column(name = "numero_correlativo_actual", nullable = false)
    @Builder.Default
    private Long numeroCorrelativoActual = 0L;

    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private String estado = "ACTIVA";

    @Column(name = "fecha_inicio", nullable = false)
    @Builder.Default
    private LocalDate fechaInicio = LocalDate.now();

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
}
