package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDatoParametro;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.UnidadMedidaParametro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Parámetro o alícuota cuantificable asociado a una versión de configuración tributaria.
 */
@Entity
@Table(name = "tributaria_parametros_configuracion", uniqueConstraints = {
        @UniqueConstraint(name = "uq_parametro_config_codigo", columnNames = {"configuracion_id", "codigo"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametroTributario extends AuditableEntity {

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

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dato", nullable = false, length = 30)
    @Builder.Default
    private TipoDatoParametro tipoDato = TipoDatoParametro.DECIMAL;

    @Column(name = "valor_decimal", precision = 16, scale = 6)
    private BigDecimal valorDecimal;

    @Column(name = "valor_texto", columnDefinition = "TEXT")
    private String valorTexto;

    @Column(name = "valor_booleano")
    private Boolean valorBooleano;

    @Column(name = "valor_fecha")
    private LocalDate valorFecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad", nullable = false, length = 50)
    @Builder.Default
    private UnidadMedidaParametro unidad = UnidadMedidaParametro.PEN;

    @Column(name = "origen_normativo", length = 255)
    private String origenNormativo;

    @Column(name = "es_obligatorio", nullable = false)
    @Builder.Default
    private Boolean esObligatorio = true;

    @Column(name = "orden_visual", nullable = false)
    @Builder.Default
    private Integer ordenVisual = 1;

    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private String estado = "ACTIVO";

    /**
     * Retorna una representación legible del valor actual según su tipo de dato.
     */
    public String getValorFormateado() {
        if (tipoDato == null) return "-";
        return switch (tipoDato) {
            case DECIMAL -> {
                if (valorDecimal == null) yield "Pendiente de definición";
                if (unidad == UnidadMedidaParametro.PORCENTAJE) {
                    yield valorDecimal.stripTrailingZeros().toPlainString() + " %";
                }
                if (unidad == UnidadMedidaParametro.PEN) {
                    yield "S/ " + valorDecimal.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
                }
                yield valorDecimal.stripTrailingZeros().toPlainString() + " " + unidad.getNombre();
            }
            case ENTERO -> valorDecimal != null ? String.valueOf(valorDecimal.longValue()) : "-";
            case TEXTO -> valorTexto != null ? valorTexto : "-";
            case BOOLEANO -> valorBooleano != null ? (valorBooleano ? "Sí" : "No") : "-";
            case FECHA -> valorFecha != null ? valorFecha.toString() : "-";
        };
    }
}
