package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoDocumentalPlantilla;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mpv_plantillas_documentales")
public class PlantillaDocumental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documental", nullable = false, length = 50)
    private TipoDocumentalPlantilla tipoDocumental;

    @Column(name = "cuerpo_plantilla", nullable = false, columnDefinition = "TEXT")
    private String cuerpoPlantilla;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "PUBLICADO"; // BORRADOR, PUBLICADO, INACTIVO

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
