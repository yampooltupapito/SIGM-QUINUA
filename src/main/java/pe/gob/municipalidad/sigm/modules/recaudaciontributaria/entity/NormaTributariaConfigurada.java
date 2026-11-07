package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoValidacionNorma;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoNormaTributaria;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tributaria_normas_configuradas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormaTributariaConfigurada extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_norma", nullable = false, length = 40)
    private TipoNormaTributaria tipoNorma;

    @Column(name = "numero_norma", nullable = false, length = 120)
    private String numeroNorma;

    @Column(name = "entidad_emisora", nullable = false, length = 150)
    @Builder.Default
    private String entidadEmisora = "Municipalidad Distrital de Quinua";

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_publicacion")
    private LocalDate fechaPublicacion;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDate fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private LocalDate fechaFinVigencia;

    @Column(name = "fuente_documental_id")
    private UUID fuenteDocumentalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion", nullable = false, length = 30)
    @Builder.Default
    private EstadoValidacionNorma estadoValidacion = EstadoValidacionNorma.REGISTRADA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_validador_id")
    private Usuario usuarioValidador;

    @Column(name = "enlace_oficial", length = 500)
    private String enlaceOficial;

    @Column(name = "alcance_territorial", length = 100)
    @Builder.Default
    private String alcanceTerritorial = "DISTRITAL_QUINUA";

    @Column(name = "archivo_hash", length = 128)
    private String archivoHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "norma_modifica_id")
    private NormaTributariaConfigurada normaModifica;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
