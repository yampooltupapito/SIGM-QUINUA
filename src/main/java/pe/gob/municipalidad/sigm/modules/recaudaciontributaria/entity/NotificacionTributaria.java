package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAdministrativoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoEntregaNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoValidezLegalNotificacion;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad principal del Subsistema de Notificaciones Tributarias y Seguimiento de Obligaciones.
 */
@Entity
@Table(name = "tributaria_notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class NotificacionTributaria extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_notificacion", nullable = false, unique = true, length = 50)
    private String codigoNotificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_notificacion_id", nullable = false)
    private TipoNotificacionTributaria tipoNotificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private ContribuyenteTributario contribuyente;

    @Column(name = "destinatario_nombre", nullable = false, length = 255)
    private String destinatarioNombre;

    @Column(name = "destinatario_numero_doc", nullable = false, length = 30)
    private String destinatarioNumeroDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representante_id")
    private RepresentanteContribuyente representante;

    @Column(name = "domicilio_notificacion", nullable = false, length = 500)
    private String domicilioNotificacion;

    @Column(name = "referencia_ubicacion", length = 255)
    private String referenciaUbicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medio_notificacion_id", nullable = false)
    private MedioNotificacionTributaria medioNotificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_origen_id")
    private Documento documentoOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_notificacion_id")
    private Documento documentoNotificacion;

    @Column(name = "hash_documento", length = 64)
    private String hashDocumento;

    @Column(name = "codigo_qr", length = 255)
    private String codigoQr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obligacion_id")
    private ObligacionTributaria obligacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convenio_id")
    private ConvenioFraccionamientoTributario convenio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seguimiento_deuda_id")
    private SeguimientoDeudaTributaria seguimientoDeuda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private LoteNotificacionesTributarias lote;

    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaCreacion = OffsetDateTime.now();

    @Column(name = "fecha_programada")
    private LocalDate fechaProgramada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_administrativo", nullable = false, length = 30)
    @Builder.Default
    private EstadoAdministrativoNotificacion estadoAdministrativo = EstadoAdministrativoNotificacion.BORRADOR;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_entrega", nullable = false, length = 35)
    @Builder.Default
    private EstadoEntregaNotificacion estadoEntrega = EstadoEntregaNotificacion.NO_PROGRAMADA;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validez_legal", nullable = false, length = 35)
    @Builder.Default
    private EstadoValidezLegalNotificacion estadoValidezLegal = EstadoValidezLegalNotificacion.NO_APLICA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsable_id")
    private Usuario usuarioResponsable;

    @Column(name = "idempotency_key", unique = true, length = 120)
    private String idempotencyKey;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    @Version
    @Column(name = "version")
    @Builder.Default
    private Long version = 0L;

    @OneToMany(mappedBy = "notificacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DetalleNotificacionObligacion> detallesObligacion = new ArrayList<>();

    @OneToMany(mappedBy = "notificacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AsignacionNotificadorTributario> asignaciones = new ArrayList<>();

    @OneToMany(mappedBy = "notificacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<IntentoNotificacionTributaria> intentos = new ArrayList<>();

    @OneToMany(mappedBy = "notificacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EvidenciaNotificacionTributaria> evidencias = new ArrayList<>();

    @OneToOne(mappedBy = "notificacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CargoRecepcionTributaria cargoRecepcion;

    @OneToOne(mappedBy = "notificacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ControlPlazoNotificacion controlPlazo;
}

