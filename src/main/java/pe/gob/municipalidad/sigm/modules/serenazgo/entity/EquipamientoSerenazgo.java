package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoEquipamientoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEquipamientoSerenazgo;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_equipamiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipamientoSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_interno", nullable = false, unique = true, length = 50)
    private String codigoInterno;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_equipo", nullable = false, length = 50)
    private TipoEquipamientoSerenazgo tipoEquipo;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "marca", length = 100)
    private String marca;

    @Column(name = "modelo", length = 100)
    private String modelo;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Column(name = "codigo_patrimonial", length = 100)
    private String codigoPatrimonial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoEquipamientoSerenazgo estado = EstadoEquipamientoSerenazgo.DISPONIBLE;

    @Column(name = "es_controlado_por_cantidad", nullable = false)
    @Builder.Default
    private Boolean esControladoPorCantidad = false;

    @Column(name = "stock_total", nullable = false)
    @Builder.Default
    private Integer stockTotal = 1;

    @Column(name = "stock_disponible", nullable = false)
    @Builder.Default
    private Integer stockDisponible = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private PersonalSerenazgo responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_movil_id")
    private UnidadMovilSerenazgo unidadMovil;

    @Column(name = "fecha_asignacion")
    private LocalDate fechaAsignacion;

    @Column(name = "fecha_incorporacion")
    @Builder.Default
    private LocalDate fechaIncorporacion = LocalDate.now();

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
