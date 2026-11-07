package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCajaMunicipal;

import java.util.UUID;

/**
 * Entidad que representa un punto de atención o ventanilla física de recaudación municipal.
 */
@Entity
@Table(name = "tributaria_cajas_municipales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CajaMunicipal extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "oficina_id")
    private Long oficinaId;

    @Column(name = "ubicacion", length = 255)
    private String ubicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cajero_asignado_id")
    private Usuario cajeroAsignado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoCajaMunicipal estado = EstadoCajaMunicipal.ACTIVA;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
