package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoCatalogoTramite;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.UnidadPlazo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mpv_tramite_versiones")
public class TramiteVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_tramite_id", nullable = false)
    private TipoTramite tipoTramite;

    @Column(name = "numero_version", nullable = false)
    private Integer numeroVersion;

    @Column(name = "nombre", length = 200, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "codigo_tupa", length = 50)
    private String codigoTupa;

    @Column(name = "denominacion_oficial", length = 250)
    private String denominacionOficial;

    @Column(name = "base_normativa", columnDefinition = "TEXT")
    private String baseNormativa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @Column(name = "plazo_dias", nullable = false)
    @Builder.Default
    private Integer plazoDias = 30;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_plazo", nullable = false, length = 20)
    @Builder.Default
    private UnidadPlazo unidadPlazo = UnidadPlazo.DIAS_HABILES;

    @Column(name = "requiere_tasa", nullable = false)
    @Builder.Default
    private Boolean requiereTasa = false;

    @Column(name = "monto_tasa", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montoTasa = BigDecimal.ZERO;

    @Column(name = "cuenta_tasa", length = 100)
    private String cuentaTasa;

    @Column(name = "permite_virtual", nullable = false)
    @Builder.Default
    private Boolean permiteVirtual = true;

    @Column(name = "permite_presencial", nullable = false)
    @Builder.Default
    private Boolean permitePresencial = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formulario_id")
    private FormularioTramite formulario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formulario_version_id")
    private FormularioVersion formularioVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoCatalogoTramite estado = EstadoCatalogoTramite.PUBLICADO;

    @Column(name = "fecha_vigencia_desde")
    private OffsetDateTime fechaVigenciaDesde;

    @Column(name = "fecha_vigencia_hasta")
    private OffsetDateTime fechaVigenciaHasta;

    @Column(name = "motivo_cambio", length = 500)
    private String motivoCambio;

    @Column(name = "aprobado_por", length = 100)
    private String aprobadoPor;

    @Column(name = "fecha_aprobacion")
    private OffsetDateTime fechaAprobacion;

    @Column(name = "publicado_por", length = 100)
    private String publicadoPor;

    @Column(name = "fecha_publicacion")
    private OffsetDateTime fechaPublicacion;

    @Column(name = "snapshot_configuracion_json", columnDefinition = "TEXT")
    private String snapshotConfiguracionJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
