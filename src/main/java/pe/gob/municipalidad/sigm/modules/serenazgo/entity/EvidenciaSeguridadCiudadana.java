package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelAccesoEvidencia;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEvidenciaSeguridad;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_evidencias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenciaSeguridadCiudadana {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidencia_id")
    private IncidenciaSeguridadCiudadana incidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intervencion_id")
    private IntervencionSerenazgo intervencion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrullaje_id")
    private PatrullajeSerenazgo patrullaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evidencia", nullable = false, length = 40)
    private TipoEvidenciaSeguridad tipoEvidencia;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private OffsetDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id", nullable = false)
    private Usuario usuarioRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_acceso", nullable = false, length = 30)
    @Builder.Default
    private NivelAccesoEvidencia nivelAcceso = NivelAccesoEvidencia.CONFIDENCIAL;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
