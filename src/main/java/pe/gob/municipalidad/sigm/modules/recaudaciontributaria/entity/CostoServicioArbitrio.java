package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.CategoriaCostoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoValidacionCosto;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tributaria_costos_servicio_arbitrio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostoServicioArbitrio extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuracion_id", nullable = false)
    private ConfiguracionArbitrioMunicipal configuracion;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_costo", nullable = false, length = 50)
    private CategoriaCostoArbitrio categoriaCosto;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "importe", nullable = false, precision = 14, scale = 2)
    private BigDecimal importe;

    @Column(name = "moneda", nullable = false, length = 10)
    @Builder.Default
    private String moneda = "PEN";

    @Column(name = "periodo_costo", nullable = false, length = 40)
    @Builder.Default
    private String periodoCosto = "ANUAL";

    @Column(name = "documento_sustento_ref", length = 200)
    private String documentoSustentoRef;

    @Column(name = "fuente_sustento", length = 255)
    private String fuenteSustento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion", nullable = false, length = 30)
    @Builder.Default
    private EstadoValidacionCosto estadoValidacion = EstadoValidacionCosto.DECLARADO;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
