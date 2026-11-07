package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;

import java.time.OffsetDateTime;

/**
 * Trazabilidad inmutable de transiciones de estado de un comerciante.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_comerciantes_cambios_estado")
public class ComercianteCambioEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comerciante_id", nullable = false)
    private Comerciante comerciante;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", nullable = false, length = 30)
    private EstadoComerciante estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 30)
    private EstadoComerciante estadoNuevo;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_sustento_id")
    private Documento documentoSustento;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_username", length = 100)
    private String usuarioUsername;

    @Builder.Default
    @Column(name = "fecha_cambio", nullable = false)
    private OffsetDateTime fechaCambio = OffsetDateTime.now();
}
