package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad que registra el historial de cambios de estado operativo de un mercado municipal.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "mercado")
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_cambios_estado")
public class MercadoCambioEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mercado_id", nullable = false)
    private MercadoMunicipal mercado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", nullable = false, length = 30)
    private EstadoMercado estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 30)
    private EstadoMercado estadoNuevo;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @Column(name = "usuario_responsable", nullable = false, length = 100)
    private String usuarioResponsable;

    @Builder.Default
    @Column(name = "fecha_cambio", nullable = false, updatable = false)
    private OffsetDateTime fechaCambio = OffsetDateTime.now();
}
