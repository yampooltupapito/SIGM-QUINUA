package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Registro histórico inmutable de cambios de estado físico u operativo en puestos comerciales.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_puestos_cambios_estado")
public class PuestoCambioEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id", nullable = false)
    private PuestoComercial puesto;

    @Builder.Default
    @Column(name = "tipo_cambio", nullable = false, length = 30)
    private String tipoCambio = "ESTADO_FISICO";

    @Column(name = "estado_anterior", nullable = false, length = 30)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 30)
    private String estadoNuevo;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @Column(name = "usuario_responsable", nullable = false, length = 100)
    private String usuarioResponsable;

    @Column(name = "usuario_username", length = 100)
    private String usuarioUsername;

    @Builder.Default
    @Column(name = "fecha_cambio", nullable = false, updatable = false)
    private OffsetDateTime fechaCambio = OffsetDateTime.now();
}
