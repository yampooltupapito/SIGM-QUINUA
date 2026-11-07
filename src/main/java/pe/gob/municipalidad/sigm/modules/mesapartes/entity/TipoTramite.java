package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoCatalogoTramite;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.UnidadPlazo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "mpv_tipos_tramite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoTramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(name = "codigo_tupa", length = 50)
    private String codigoTupa;

    @Column(name = "denominacion_oficial", length = 250)
    private String denominacionOficial;

    @Column(name = "base_normativa", columnDefinition = "TEXT")
    private String baseNormativa;

    @Column(length = 100)
    @Builder.Default
    private String categoria = "GENERAL";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @Column(name = "requiere_documentos", nullable = false)
    @Builder.Default
    private Boolean requiereDocumentos = true;

    @Column(name = "permite_presentacion_virtual", nullable = false)
    @Builder.Default
    private Boolean permitePresentacionVirtual = true;

    @Column(name = "permite_presencial", nullable = false)
    @Builder.Default
    private Boolean permitePresencial = true;

    @Column(name = "plazo_dias", nullable = false)
    @Builder.Default
    private Integer plazoDias = 30;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_plazo", nullable = false, length = 20)
    @Builder.Default
    private UnidadPlazo unidadPlazo = UnidadPlazo.DIAS_HABILES;

    @Column(name = "version_actual", nullable = false)
    @Builder.Default
    private Integer versionActual = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_catalogo", nullable = false, length = 20)
    @Builder.Default
    private EstadoCatalogoTramite estadoCatalogo = EstadoCatalogoTramite.PUBLICADO;

    @Column(name = "requiere_tasa", nullable = false)
    @Builder.Default
    private Boolean requiereTasa = false;

    @Column(name = "monto_tasa", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montoTasa = BigDecimal.ZERO;

    @Column(name = "cuenta_tasa", length = 100)
    private String cuentaTasa;

    @Column(name = "fecha_vigencia_desde")
    private OffsetDateTime fechaVigenciaDesde;

    @Column(name = "fecha_vigencia_hasta")
    private OffsetDateTime fechaVigenciaHasta;

    @Column(name = "aprobado_por", length = 100)
    private String aprobadoPor;

    @Column(name = "fecha_aprobacion")
    private OffsetDateTime fechaAprobacion;

    @Column(name = "publicado_por", length = 100)
    private String publicadoPor;

    @Column(name = "fecha_publicacion")
    private OffsetDateTime fechaPublicacion;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "ACTIVO";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (estado == null) {
            estado = "ACTIVO";
        }
        if (requiereDocumentos == null) {
            requiereDocumentos = true;
        }
        if (permitePresentacionVirtual == null) {
            permitePresentacionVirtual = true;
        }
        if (permitePresencial == null) {
            permitePresencial = true;
        }
        if (plazoDias == null) {
            plazoDias = 30;
        }
        if (unidadPlazo == null) {
            unidadPlazo = UnidadPlazo.DIAS_HABILES;
        }
        if (versionActual == null) {
            versionActual = 1;
        }
        if (estadoCatalogo == null) {
            estadoCatalogo = EstadoCatalogoTramite.PUBLICADO;
        }
        if (requiereTasa == null) {
            requiereTasa = false;
        }
        if (montoTasa == null) {
            montoTasa = BigDecimal.ZERO;
        }
        if (categoria == null) {
            categoria = "GENERAL";
        }
        if (denominacionOficial == null && nombre != null) {
            denominacionOficial = nombre;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
