package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoRelacionDocumentoExpediente;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mpv_expediente_documentos", uniqueConstraints = {
        @UniqueConstraint(name = "uq_mpv_exp_doc", columnNames = {"expediente_id", "documento_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpedienteDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private Expediente expediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_relacion", nullable = false, length = 50)
    @Builder.Default
    private TipoRelacionDocumentoExpediente tipoRelacion = TipoRelacionDocumentoExpediente.ANEXO;

    @Column(name = "fecha_asociacion", nullable = false)
    private OffsetDateTime fechaAsociacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_asocia_id")
    private Usuario usuarioAsocia;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 1;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "ACTIVO";

    @PrePersist
    protected void onCreate() {
        if (fechaAsociacion == null) {
            fechaAsociacion = OffsetDateTime.now();
        }
        if (orden == null) {
            orden = 1;
        }
        if (estado == null) {
            estado = "ACTIVO";
        }
        if (tipoRelacion == null) {
            tipoRelacion = TipoRelacionDocumentoExpediente.ANEXO;
        }
    }
}
