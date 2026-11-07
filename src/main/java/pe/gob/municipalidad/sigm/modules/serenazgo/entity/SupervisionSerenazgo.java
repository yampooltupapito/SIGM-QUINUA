package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSupervisionSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.ResultadoSupervisionSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoSupervisionSerenazgo;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_supervisiones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupervisionSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id", nullable = false)
    private PersonalSerenazgo supervisor;

    @Column(name = "fecha", nullable = false)
    @Builder.Default
    private OffsetDateTime fecha = OffsetDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private SectorPatrullaje sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrullaje_id")
    private PatrullajeSerenazgo patrullaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_supervision", nullable = false, length = 40)
    @Builder.Default
    private TipoSupervisionSerenazgo tipoSupervision = TipoSupervisionSerenazgo.RUTINA;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 50)
    @Builder.Default
    private ResultadoSupervisionSerenazgo resultado = ResultadoSupervisionSerenazgo.CONFORME;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoSupervisionSerenazgo estado = EstadoSupervisionSerenazgo.REGISTRADA;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
