package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoNovedadPatrullaje;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_patrullaje_novedades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatrullajeNovedad {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrullaje_id", nullable = false)
    private PatrullajeSerenazgo patrullaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_novedad", nullable = false, length = 50)
    private TipoNovedadPatrullaje tipoNovedad;

    @Column(name = "fecha_hora", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaHora = OffsetDateTime.now();

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_reporta_id", nullable = false)
    private PersonalSerenazgo personalReporta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidencia_vinculada_id")
    private IncidenciaSeguridadCiudadana incidenciaVinculada;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
