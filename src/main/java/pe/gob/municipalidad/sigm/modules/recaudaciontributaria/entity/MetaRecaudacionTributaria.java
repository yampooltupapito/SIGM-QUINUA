package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoMetaRecaudacion;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Meta presupuestal o institucional de recaudación tributaria municipal de Quinua.
 */
@Entity
@Table(name = "tributaria_metas_recaudacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaRecaudacionTributaria extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "ejercicio", nullable = false)
    private Integer ejercicio;

    /**
     * 0 indica meta anual total; 1 a 12 indica meta de un mes específico.
     */
    @Column(name = "periodo", nullable = false)
    @Builder.Default
    private Integer periodo = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concepto_id")
    private ConceptoTributario concepto;

    @Column(name = "oficina_id")
    private Long oficinaId;

    @Column(name = "importe_meta", precision = 14, scale = 2, nullable = false)
    private BigDecimal importeMeta;

    @Column(name = "documento_sustento_id")
    private UUID documentoSustentoId;

    @Column(name = "aprobado", nullable = false)
    @Builder.Default
    private Boolean aprobado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por")
    private Usuario aprobadoPor;

    @Column(name = "fecha_aprobacion")
    private OffsetDateTime fechaAprobacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 30, nullable = false)
    @Builder.Default
    private EstadoMetaRecaudacion estado = EstadoMetaRecaudacion.BORRADOR;

    @Column(name = "observacion", length = 500)
    private String observacion;

    public boolean isAprobado() {
        return Boolean.TRUE.equals(aprobado);
    }
}

