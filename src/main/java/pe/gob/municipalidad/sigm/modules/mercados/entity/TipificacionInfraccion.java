package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.modules.mercados.enums.GravedadHallazgo;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_tipificaciones_infraccion")
public class TipificacionInfraccion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "norma_referencia", nullable = false)
    private String normaReferencia;

    @Column(length = 100)
    private String articulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "gravedad_base", nullable = false, length = 30)
    @Builder.Default
    private GravedadHallazgo gravedadBase = GravedadHallazgo.LEVE;

    @Column(name = "autoridad_competente", nullable = false, length = 150)
    private String autoridadCompetente;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDate fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private LocalDate fechaFinVigencia;

    @Column(name = "version_normativa", nullable = false)
    @Builder.Default
    private Integer versionNormativa = 1;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "ACTIVO"; // ACTIVO, INACTIVO, DEROGADO

    @Column(name = "documento_fuente_id")
    private UUID documentoFuenteId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
