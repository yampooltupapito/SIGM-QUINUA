package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVersionPlano;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa una versión histórica o borrador del plano de un mercado.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"plano", "elementos"})
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(
        name = "mercados_planos_versiones",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_mercados_planos_version",
                columnNames = {"plano_id", "numero_version"}
        )
)
public class VersionPlanoMercado extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id", nullable = false)
    private PlanoMercado plano;

    @Column(name = "numero_version", nullable = false)
    private Integer numeroVersion;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoVersionPlano estado = EstadoVersionPlano.BORRADOR;

    @Column(name = "descripcion_cambios", length = 500)
    private String descripcionCambios;

    @Column(name = "usuario_creador_id")
    private UUID usuarioCreadorId;

    @Column(name = "usuario_creador_nombre", length = 100)
    private String usuarioCreadorNombre;

    @Column(name = "usuario_publicador_id")
    private UUID usuarioPublicadorId;

    @Column(name = "usuario_publicador_nombre", length = 100)
    private String usuarioPublicadorNombre;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion = OffsetDateTime.now();

    @Column(name = "fecha_publicacion")
    private OffsetDateTime fechaPublicacion;

    @Column(name = "hash_contenido", length = 64)
    private String hashContenido;

    // Campos de imagen de referencia de fondo
    @Column(name = "imagen_referencia_url", length = 255)
    private String imagenReferenciaUrl;

    @Builder.Default
    @Column(name = "imagen_escala", precision = 6, scale = 3)
    private BigDecimal imagenEscala = new BigDecimal("1.000");

    @Builder.Default
    @Column(name = "imagen_pos_x", precision = 10, scale = 2)
    private BigDecimal imagenPosX = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "imagen_pos_y", precision = 10, scale = 2)
    private BigDecimal imagenPosY = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "imagen_opacidad", precision = 4, scale = 2)
    private BigDecimal imagenOpacidad = new BigDecimal("0.50");

    @Builder.Default
    @Column(name = "imagen_visible")
    private Boolean imagenVisible = false;

    @Builder.Default
    @Column(name = "imagen_bloqueada")
    private Boolean imagenBloqueada = false;

    @Builder.Default
    @OneToMany(mappedBy = "versionPlano", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordenCapa ASC, coordenadaY ASC, coordenadaX ASC")
    private List<ElementoPlanoMercado> elementos = new ArrayList<>();

    @Version
    @Column(name = "version")
    private Long version;
}
