package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.OrigenIncidencia;
import pe.gob.municipalidad.sigm.modules.mercados.enums.PrioridadIncidencia;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_incidencias")
public class IncidenciaMercado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo_incidencia", nullable = false, unique = true, length = 50)
    private String codigoIncidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mercado_id", nullable = false)
    private MercadoMunicipal mercado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private SectorMercado sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pabellon_id")
    private PabellonMercado pabellon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id")
    private PuestoComercial puesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comerciante_id")
    private Comerciante comerciante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_incidencia_id", nullable = false)
    private TipoIncidenciaMercado tipoIncidencia;

    @jakarta.persistence.Convert(converter = pe.gob.municipalidad.sigm.modules.mercados.enums.OrigenIncidenciaConverter.class)
    @Column(nullable = false, length = 50)
    private OrigenIncidencia origen;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_reporte", nullable = false)
    private OffsetDateTime fechaReporte;

    @Column(name = "fecha_ocurrencia")
    private OffsetDateTime fechaOcurrencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PrioridadIncidencia prioridad = PrioridadIncidencia.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoIncidencia estado = EstadoIncidencia.REGISTRADA;

    @Column(name = "reportante_nombre", length = 150)
    private String reportanteNombre;

    @Column(name = "reportante_contacto", length = 100)
    private String reportanteContacto;

    @Column(name = "es_anonimo", nullable = false)
    @Builder.Default
    private Boolean esAnonimo = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportante_usuario_id")
    private Usuario reportanteUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspeccion_id")
    private InspeccionMercado inspeccion;

    @Column(name = "expediente_id")
    private UUID expedienteId;

    @Column(name = "expediente_numero", length = 64)
    private String expedienteNumero;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_resolucion")
    private OffsetDateTime fechaResolucion;

    @Column(name = "motivo_resolucion", columnDefinition = "TEXT")
    private String motivoResolucion;

    @Column(name = "fecha_cierre")
    private OffsetDateTime fechaCierre;

    @Column(name = "motivo_cierre", columnDefinition = "TEXT")
    private String motivoCierre;

    @OneToMany(mappedBy = "incidencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ActuacionIncidenciaMercado> actuaciones = new ArrayList<>();

    @OneToMany(mappedBy = "incidencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EvidenciaIncidenciaMercado> evidencias = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;
}
