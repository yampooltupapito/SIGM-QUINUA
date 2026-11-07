package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CategoriaIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.PrioridadIncidenciaSeguridad;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_tipos_incidencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoIncidenciaSeguridad {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 50)
    @Builder.Default
    private CategoriaIncidenciaSeguridad categoria = CategoriaIncidenciaSeguridad.ORDEN_PUBLICO;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad_sugerida", nullable = false, length = 30)
    @Builder.Default
    private PrioridadIncidenciaSeguridad prioridadSugerida = PrioridadIncidenciaSeguridad.MEDIA;

    @Column(name = "requiere_unidad", nullable = false)
    @Builder.Default
    private Boolean requiereUnidad = false;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
