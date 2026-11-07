package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoVisitaPunto;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.MetodoRegistroVisita;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_patrullaje_visitas_puntos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatrullajeVisitaPunto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrullaje_id", nullable = false)
    private PatrullajeSerenazgo patrullaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_control_id", nullable = false)
    private PuntoControlRuta puntoControl;

    @Column(name = "orden_visita", nullable = false)
    private Integer ordenVisita;

    @Column(name = "fecha_hora_registro", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaHoraRegistro = OffsetDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_registro_id", nullable = false)
    private PersonalSerenazgo personalRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_registro", nullable = false, length = 40)
    @Builder.Default
    private MetodoRegistroVisita metodoRegistro = MetodoRegistroVisita.MANUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_visita", nullable = false, length = 30)
    @Builder.Default
    private EstadoVisitaPunto estadoVisita = EstadoVisitaPunto.VISITADO;

    @Column(name = "motivo_omision", columnDefinition = "TEXT")
    private String motivoOmision;

    @Column(name = "latitud_registro", precision = 10, scale = 7)
    private BigDecimal latitudRegistro;

    @Column(name = "longitud_registro", precision = 10, scale = 7)
    private BigDecimal longitudRegistro;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
