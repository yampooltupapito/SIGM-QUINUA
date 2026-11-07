package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoFormularioVersion;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Versión inmutable de un formulario dinámico para preservar trazabilidad de expedientes históricos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mpv_formulario_versiones",
       uniqueConstraints = @UniqueConstraint(name = "uq_mpv_form_version", columnNames = {"formulario_id", "version"}))
public class FormularioVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formulario_id", nullable = false)
    private FormularioTramite formulario;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    @Builder.Default
    private EstadoFormularioVersion estado = EstadoFormularioVersion.PUBLICADO;

    @Column(name = "fecha_publicacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaPublicacion = OffsetDateTime.now();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<FormularioCampo> campos = new ArrayList<>();
}
