package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.ResultadoVerificacionIntegridad;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_documentos_verificaciones", indexes = {
        @Index(name = "idx_serenazgo_doc_verif_doc", columnList = "documento_seguridad_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoSeguridadVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_seguridad_id", nullable = false)
    private DocumentoSeguridadCiudadana documentoSeguridad;

    @Column(name = "hash_registrado", nullable = false, length = 64)
    private String hashRegistrado;

    @Column(name = "hash_calculado", length = 64)
    private String hashCalculado;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 50)
    private ResultadoVerificacionIntegridad resultado;

    @CreationTimestamp
    @Column(name = "fecha_verificacion", nullable = false, updatable = false)
    private OffsetDateTime fechaVerificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "detalle_tecnico", columnDefinition = "TEXT")
    private String detalleTecnico;
}
