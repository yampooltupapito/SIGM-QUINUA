package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.RolRelacionDocumento;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoRelacionDocumentoSeguridad;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_documentos_relaciones", indexes = {
        @Index(name = "idx_serenazgo_doc_rel_doc", columnList = "documento_seguridad_id"),
        @Index(name = "idx_serenazgo_doc_rel_entidad", columnList = "tipo_entidad, entidad_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoSeguridadRelacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_seguridad_id", nullable = false)
    private DocumentoSeguridadCiudadana documentoSeguridad;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entidad", nullable = false, length = 50)
    private TipoRelacionDocumentoSeguridad tipoEntidad;

    @Column(name = "entidad_id", nullable = false)
    private UUID entidadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_relacion", nullable = false, length = 50)
    @Builder.Default
    private RolRelacionDocumento rolRelacion = RolRelacionDocumento.RESPALDO;

    @CreationTimestamp
    @Column(name = "fecha_asociacion", nullable = false, updatable = false)
    private OffsetDateTime fechaAsociacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_asocia_id", nullable = false)
    private Usuario usuarioAsocia;
}
