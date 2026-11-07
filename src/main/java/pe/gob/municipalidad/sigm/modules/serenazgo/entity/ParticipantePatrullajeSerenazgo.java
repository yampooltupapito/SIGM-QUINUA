package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_patrullajes_participantes", uniqueConstraints = {
        @UniqueConstraint(name = "uq_serenazgo_patrullaje_personal", columnNames = {"patrullaje_id", "personal_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantePatrullajeSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrullaje_id", nullable = false)
    private PatrullajeSerenazgo patrullaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private PersonalSerenazgo personal;

    @Column(name = "funcion_desempenada", nullable = false, length = 100)
    private String funcionDesempenada;

    @CreationTimestamp
    @Column(name = "fecha_incorporacion", nullable = false, updatable = false)
    private OffsetDateTime fechaIncorporacion;

    @Column(name = "fecha_retiro")
    private OffsetDateTime fechaRetiro;
}
