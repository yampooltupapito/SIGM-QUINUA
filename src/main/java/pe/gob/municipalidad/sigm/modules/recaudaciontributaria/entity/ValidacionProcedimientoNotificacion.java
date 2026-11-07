package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ResultadoValidacionProcedimental;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Evaluación y validación de la eficacia y validez legal del acto de notificación.
 */
@Entity
@Table(name = "tributaria_validaciones_procedimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class ValidacionProcedimientoNotificacion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacion_id", nullable = false)
    private NotificacionTributaria notificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revisor_id", nullable = false)
    private Usuario revisor;

    @Column(name = "fecha_validacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaValidacion = OffsetDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 30)
    private ResultadoValidacionProcedimental resultado;

    @Column(name = "sustento", nullable = false, columnDefinition = "TEXT")
    private String sustento;

    @Column(name = "cumple_destinatario_identificado", nullable = false)
    @Builder.Default
    private Boolean cumpleDestinatarioIdentificado = false;

    @Column(name = "cumple_domicilio_valido", nullable = false)
    @Builder.Default
    private Boolean cumpleDomicilioValido = false;

    @Column(name = "cumple_documento_adjunto", nullable = false)
    @Builder.Default
    private Boolean cumpleDocumentoAdjunto = false;

    @Column(name = "cumple_plazo_procedimental", nullable = false)
    @Builder.Default
    private Boolean cumplePlazoProcedimental = false;

    @Column(name = "cumple_evidencia_minima", nullable = false)
    @Builder.Default
    private Boolean cumpleEvidenciaMinima = false;

    @Column(name = "cumple_datos_receptor_y_cargo", nullable = false)
    @Builder.Default
    private Boolean cumpleDatosReceptorYCargo = false;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}

