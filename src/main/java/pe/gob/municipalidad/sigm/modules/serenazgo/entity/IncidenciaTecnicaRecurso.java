package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoIncidenciaTecnica;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelGravedadIncidencia;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoIncidenciaTecnica;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoRecursoSeguridad;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_incidencias_tecnicas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaTecnicaRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recurso", nullable = false, length = 30)
    private TipoRecursoSeguridad tipoRecurso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id")
    private UnidadMovilSerenazgo unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipamiento_id")
    private EquipamientoSerenazgo equipamiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_incidencia", nullable = false, length = 50)
    private TipoIncidenciaTecnica tipoIncidencia;

    @Column(name = "fecha_reporte", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaReporte = OffsetDateTime.now();

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_reporta_id")
    private PersonalSerenazgo personalReporta;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_gravedad", nullable = false, length = 20)
    @Builder.Default
    private NivelGravedadIncidencia nivelGravedad = NivelGravedadIncidencia.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoIncidenciaTecnica estado = EstadoIncidenciaTecnica.REGISTRADA;

    @Column(name = "acciones_tomadas", columnDefinition = "TEXT")
    private String accionesTomadas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_evidencia_id")
    private Documento documentoEvidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id")
    private Usuario usuarioRegistro;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
