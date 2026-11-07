package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_mapa_capas_institucionales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapaInstitucionalGeografica {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tipo_geometria", nullable = false, length = 50)
    private String tipoGeometria;

    @Column(name = "fuente_origen", nullable = false, length = 150)
    private String fuenteOrigen;

    @Column(name = "sistema_referencia", nullable = false, length = 30)
    @Builder.Default
    private String sistemaReferencia = "EPSG:4326";

    @Column(name = "version_capa", nullable = false)
    @Builder.Default
    private Integer versionCapa = 1;

    @Column(name = "estado_validacion", nullable = false, length = 30)
    @Builder.Default
    private String estadoValidacion = "APROBADA";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_registro_id")
    private Usuario responsableRegistro;

    @Column(name = "fecha_incorporacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaIncorporacion = OffsetDateTime.now();

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "geometria_geojson", columnDefinition = "TEXT")
    private String geometriaGeojson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
