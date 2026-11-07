package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "mercados_tarifas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifaMercado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concepto_id", nullable = false)
    private ConceptoCobroMercado concepto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mercado_id")
    private MercadoMunicipal mercado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_puesto_id")
    private TipoPuestoComercial tipoPuesto;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "moneda", nullable = false, length = 3)
    @Builder.Default
    private String moneda = "PEN";

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDate fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private LocalDate fechaFinVigencia;

    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private String estado = "ACTIVA";

    @Column(name = "documento_sustento_id")
    private UUID documentoSustentoId;

    @Column(name = "fuente_normativa", length = 255)
    private String fuenteNormativa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_aprobacion_id")
    private Usuario usuarioAprobacion;

    @Column(name = "version_tarifa", nullable = false)
    @Builder.Default
    private Integer versionTarifa = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
