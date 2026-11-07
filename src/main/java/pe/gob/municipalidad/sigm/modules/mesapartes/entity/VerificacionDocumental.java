package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoVerificacionDocumento;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mpv_verificaciones_documentales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionDocumental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_verificacion", nullable = false, unique = true, length = 64)
    private String codigoVerificacion;

    @Column(name = "token_qr", nullable = false, unique = true, length = 128)
    private String tokenQr;

    @Column(name = "tipo_documento", nullable = false, length = 50)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 100)
    private String numeroDocumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id")
    private Expediente expediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id")
    private DecisionExpediente decision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @Column(name = "hash_sha256", nullable = false, length = 64)
    private String hashSha256;

    @Column(name = "fecha_emision", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaEmision = OffsetDateTime.now();

    @Column(name = "entidad_emisora", nullable = false, length = 150)
    @Builder.Default
    private String entidadEmisora = "Municipalidad Distrital de Quinua";

    @Column(name = "autoridad_firmante", length = 150)
    private String autoridadFirmante;

    @Column(name = "cargo_firmante", length = 100)
    private String cargoFirmante;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_documento", nullable = false, length = 20)
    @Builder.Default
    private EstadoVerificacionDocumento estadoDocumento = EstadoVerificacionDocumento.VIGENTE;

    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    @Column(name = "url_verificacion", nullable = false, length = 255)
    private String urlVerificacion;

    @Column(name = "veces_consultado", nullable = false)
    @Builder.Default
    private Integer vecesConsultado = 0;

    @Column(name = "ultima_consulta")
    private OffsetDateTime ultimaConsulta;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (fechaEmision == null) fechaEmision = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
