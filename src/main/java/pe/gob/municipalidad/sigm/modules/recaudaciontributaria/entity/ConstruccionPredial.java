package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConservacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MaterialPredominante;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad de Construcción o Edificación dentro de un Predio Tributario.
 * Registra pisos, áreas techadas/construidas, materiales predominantes y estado de conservación.
 */
@Entity
@Table(name = "tributaria_construcciones_predio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstruccionPredial extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predio_id", nullable = false)
    private PredioTributario predio;

    @Column(name = "codigo_construccion", nullable = false, length = 50)
    private String codigoConstruccion;

    @Column(name = "seccion_bloque", length = 100)
    private String seccionBloque;

    @Column(name = "nivel_piso", nullable = false)
    @Builder.Default
    private Integer nivelPiso = 1;

    @Column(name = "area_construida", nullable = false, precision = 12, scale = 4)
    private BigDecimal areaConstruida;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_predominante", nullable = false, length = 50)
    @Builder.Default
    private MaterialPredominante materialPredominante = MaterialPredominante.ADOBE_TAPIA;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_conservacion", nullable = false, length = 50)
    @Builder.Default
    private EstadoConservacion estadoConservacion = EstadoConservacion.REGULAR;

    @Column(name = "anio_construccion")
    private Integer anioConstruccion;

    @Column(name = "clasificacion_edificacion", nullable = false, length = 50)
    @Builder.Default
    private String clasificacionEdificacion = "PREDOMINANTE";

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
}
