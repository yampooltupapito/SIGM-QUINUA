package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.AccionHistorialIntervencion;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_intervencion_historial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionHistorialSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intervencion_id", nullable = false)
    private IntervencionSerenazgo intervencion;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 60)
    private AccionHistorialIntervencion accion;

    @Column(name = "estado_anterior", length = 30)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", length = 30)
    private String estadoNuevo;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "usuario", nullable = false, length = 100)
    private String usuario;

    @CreationTimestamp
    @Column(name = "fecha", nullable = false, updatable = false)
    private OffsetDateTime fecha;
}
