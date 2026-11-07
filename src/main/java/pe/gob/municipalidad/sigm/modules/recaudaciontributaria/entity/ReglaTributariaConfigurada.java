package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoReglaTributaria;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Regla de negocio, beneficio o deducción estructurada con base legal explícita.
 */
@Entity
@Table(name = "tributaria_reglas_configuradas", uniqueConstraints = {
        @UniqueConstraint(name = "uq_regla_config_codigo", columnNames = {"configuracion_id", "codigo"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReglaTributariaConfigurada extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuracion_id", nullable = false)
    private ConfiguracionConceptoTributario configuracion;

    @Column(name = "codigo", nullable = false, length = 80)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_regla", nullable = false, length = 50)
    private TipoReglaTributaria tipoRegla;

    @Column(name = "condicion_aplicacion", nullable = false, columnDefinition = "TEXT")
    private String condicionAplicacion;

    @Column(name = "base_legal_articulo", length = 150)
    private String baseLegalArticulo;

    @Column(name = "porcentaje_beneficio", precision = 5, scale = 2)
    private BigDecimal porcentajeBeneficio;

    @Column(name = "monto_fijo_beneficio", precision = 12, scale = 2)
    private BigDecimal montoFijoBeneficio;

    @Column(name = "orden_aplicacion", nullable = false)
    @Builder.Default
    private Integer ordenAplicacion = 1;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
