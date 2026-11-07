package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoEventoDocumentoPago;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Trazabilidad y auditoría de eventos del ciclo de vida de un documento tributario.
 */
@Entity
@Table(
        name = "tributaria_historial_documento_pago",
        indexes = {
                @Index(name = "idx_trib_hist_doc", columnList = "documento_id"),
                @Index(name = "idx_trib_hist_fecha", columnList = "fecha_evento")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class HistorialDocumentoPagoTributario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    private DocumentoPagoTributario documento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 50)
    private TipoEventoDocumentoPago tipoEvento;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "ip_origen", length = 50)
    private String ipOrigen;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "fecha_evento", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaEvento = OffsetDateTime.now();
}
