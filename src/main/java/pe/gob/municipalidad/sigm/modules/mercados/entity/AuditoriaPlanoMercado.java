package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Registro de auditoría inmutable de eventos sobre planos y versiones.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_planos_auditoria")
public class AuditoriaPlanoMercado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "plano_id", nullable = false)
    private UUID planoId;

    @Column(name = "version_plano_id")
    private UUID versionPlanoId;

    @Column(name = "tipo_evento", nullable = false, length = 50)
    private String tipoEvento;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "detalles_json", columnDefinition = "TEXT")
    private String detallesJson;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
