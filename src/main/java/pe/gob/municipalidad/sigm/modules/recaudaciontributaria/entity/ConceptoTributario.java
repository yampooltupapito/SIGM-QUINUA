package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConceptoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.PeriodicidadConcepto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoConceptoTributario;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tributaria_conceptos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptoTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_concepto", nullable = false, length = 40)
    private TipoConceptoTributario tipoConcepto;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodicidad", nullable = false, length = 30)
    @Builder.Default
    private PeriodicidadConcepto periodicidad = PeriodicidadConcepto.ANUAL;

    @Column(name = "aplica_a_predio", nullable = false)
    @Builder.Default
    private Boolean aplicaAPredio = true;

    @Column(name = "cuenta_presupuestal", length = 50)
    private String cuentaPresupuestal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoConceptoTributario estado = EstadoConceptoTributario.VIGENTE;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDate fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private LocalDate fechaFinVigencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "naturaleza_tributaria", length = 40)
    @Builder.Default
    private pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.NaturalezaTributaria naturalezaTributaria = pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.NaturalezaTributaria.IMPUESTO;

    @Column(name = "oficina_responsable_id")
    private Long oficinaResponsableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "norma_legal_id")
    private NormaTributariaConfigurada normaLegal;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
