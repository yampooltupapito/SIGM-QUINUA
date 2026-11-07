package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa un mercado municipal en la jurisdicción de Quinua.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"oficinaAdministradora", "sectores", "puestos"})
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "mercados_mercados")
public class MercadoMunicipal extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "direccion", nullable = false, length = 255)
    private String direccion;

    @Column(name = "referencia_ubicacion", length = 255)
    private String referenciaUbicacion;

    @Builder.Default
    @Column(name = "distrito", nullable = false, length = 100)
    private String distrito = "Quinua";

    @Builder.Default
    @Column(name = "provincia", nullable = false, length = 100)
    private String provincia = "Huamanga";

    @Builder.Default
    @Column(name = "departamento", nullable = false, length = 100)
    private String departamento = "Ayacucho";

    @Column(name = "latitud", precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 11, scale = 8)
    private BigDecimal longitud;

    @Builder.Default
    @Column(name = "capacidad_referencial", nullable = false)
    private Integer capacidadReferencial = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoMercado estado = EstadoMercado.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_administradora_id")
    private Oficina oficinaAdministradora;

    @Builder.Default
    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro = LocalDate.now();

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Builder.Default
    @OneToMany(mappedBy = "mercado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SectorMercado> sectores = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "mercado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PuestoComercial> puestos = new ArrayList<>();
}
