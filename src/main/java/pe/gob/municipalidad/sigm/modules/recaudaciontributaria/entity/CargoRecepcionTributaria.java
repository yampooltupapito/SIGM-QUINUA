package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoVinculoReceptor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Cargo formal de recepción que acredita la entrega de la notificación.
 */
@Entity
@Table(name = "tributaria_cargos_recepcion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class CargoRecepcionTributaria extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacion_id", nullable = false, unique = true)
    private NotificacionTributaria notificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intento_id")
    private IntentoNotificacionTributaria intento;

    @Column(name = "fecha_recepcion_declarada", nullable = false)
    private OffsetDateTime fechaRecepcionDeclarada;

    @Column(name = "receptor_identificado", nullable = false, length = 255)
    private String receptorIdentificado;

    @Column(name = "receptor_numero_doc", length = 30)
    private String receptorNumeroDoc;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vinculo_receptor", nullable = false, length = 40)
    private TipoVinculoReceptor tipoVinculoReceptor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_evidencia_id")
    private Documento documentoEvidencia;

    @Column(name = "tiene_firma", nullable = false)
    @Builder.Default
    private Boolean tieneFirma = false;

    @Column(name = "tiene_sello", nullable = false)
    @Builder.Default
    private Boolean tieneSello = false;

    @Column(name = "estado_validacion", nullable = false, length = 30)
    @Builder.Default
    private String estadoValidacion = "REGISTRADO";

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}

