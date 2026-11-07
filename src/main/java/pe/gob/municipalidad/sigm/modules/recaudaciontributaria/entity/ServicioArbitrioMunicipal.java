package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoServicioArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.PeriodicidadArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoServicioArbitrio;

import java.util.UUID;

@Entity
@Table(name = "tributaria_servicios_arbitrio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioArbitrioMunicipal extends AuditableEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concepto_tributario_id")
    private ConceptoTributario conceptoTributario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", nullable = false, length = 40)
    private TipoServicioArbitrio tipoServicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodicidad", nullable = false, length = 30)
    @Builder.Default
    private PeriodicidadArbitrio periodicidad = PeriodicidadArbitrio.MENSUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoServicioArbitrio estado = EstadoServicioArbitrio.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
