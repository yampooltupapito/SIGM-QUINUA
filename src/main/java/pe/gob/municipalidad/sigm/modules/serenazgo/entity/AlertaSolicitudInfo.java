package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSolicitudInfoAlerta;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_alerta_solicitudes_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaSolicitudInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alerta_id", nullable = false)
    private AlertaCiudadanaSeguridad alerta;

    @Column(name = "solicitud", nullable = false, columnDefinition = "TEXT")
    private String solicitud;

    @Column(name = "fecha_solicitud", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaSolicitud = OffsetDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_solicita_id")
    private Usuario usuarioSolicita;

    @Column(name = "respuesta_ciudadano", columnDefinition = "TEXT")
    private String respuestaCiudadano;

    @Column(name = "fecha_respuesta")
    private OffsetDateTime fechaRespuesta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoSolicitudInfoAlerta estado = EstadoSolicitudInfoAlerta.PENDIENTE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
