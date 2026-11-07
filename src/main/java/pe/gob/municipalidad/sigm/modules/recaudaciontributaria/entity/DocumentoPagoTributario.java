package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoDocumentoPagoTributario;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad principal de Documentos de Pago Tributario (Recibos de Caja y Constancias Oficiales).
 */
@Entity
@Table(
        name = "tributaria_documentos_pago",
        indexes = {
                @Index(name = "idx_trib_docs_pago", columnList = "pago_tributario_id"),
                @Index(name = "idx_trib_docs_contrib", columnList = "contribuyente_id"),
                @Index(name = "idx_trib_docs_tipo", columnList = "tipo_documento_id"),
                @Index(name = "idx_trib_docs_serie", columnList = "serie_id"),
                @Index(name = "idx_trib_docs_estado", columnList = "estado"),
                @Index(name = "idx_trib_docs_cod_verif", columnList = "codigo_verificacion")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class DocumentoPagoTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo_documento", nullable = false, unique = true, length = 60)
    private String codigoDocumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_documento_id", nullable = false)
    private TipoDocumentoPagoTributario tipoDocumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serie_id", nullable = false)
    private SerieDocumentoPagoTributario serie;

    @Column(name = "numero_correlativo", nullable = false)
    private Long numeroCorrelativo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_tributario_id", nullable = false)
    private PagoTributario pagoTributario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private ContribuyenteTributario contribuyente;

    @Column(name = "fecha_emision", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaEmision = OffsetDateTime.now();

    @Column(name = "importe_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal importeTotal;

    @Column(name = "moneda", nullable = false, length = 10)
    @Builder.Default
    private String moneda = "PEN";

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoDocumentoPagoTributario estado = EstadoDocumentoPagoTributario.EMITIDO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_emisor_id")
    private Usuario usuarioEmisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_emisora_id")
    private Oficina oficinaEmisora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archivo_documento_id")
    private Documento archivoDocumento;

    @Column(name = "nombre_archivo", length = 255)
    private String nombreArchivo;

    @Column(name = "hash_documento", nullable = false, length = 64)
    private String hashDocumento;

    @Column(name = "codigo_verificacion", nullable = false, unique = true, length = 64)
    private String codigoVerificacion;

    @Column(name = "url_verificacion", nullable = false, length = 255)
    private String urlVerificacion;

    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    @Column(name = "fecha_anulacion")
    private OffsetDateTime fechaAnulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_anulacion_id")
    private Usuario usuarioAnulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_sustitutorio_id")
    private DocumentoPagoTributario documentoSustitutorio;

    @Column(name = "numero_reimpresiones", nullable = false)
    @Builder.Default
    private Integer numeroReimpresiones = 0;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
