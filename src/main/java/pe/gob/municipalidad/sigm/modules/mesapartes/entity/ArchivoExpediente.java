package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mpv_archivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchivoExpediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false)
    private Expediente expediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_archivo_id", nullable = false)
    private Usuario usuarioArchivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_archivo_id", nullable = false)
    private Oficina oficinaArchivo;

    @Column(name = "codigo_archivador", length = 100)
    private String codigoArchivador;

    @Column(name = "ubicacion_fisica", length = 200)
    private String ubicacionFisica;

    @Column(name = "tomo_folio", length = 100)
    private String tomoFolio;

    @Column(name = "motivo_archivo", columnDefinition = "TEXT", nullable = false)
    private String motivoArchivo;

    @Column(name = "fecha_archivo", nullable = false)
    private OffsetDateTime fechaArchivo;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (fechaArchivo == null) fechaArchivo = OffsetDateTime.now();
    }
}
