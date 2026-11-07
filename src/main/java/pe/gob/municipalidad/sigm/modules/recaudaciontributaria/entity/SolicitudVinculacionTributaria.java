package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;

import java.util.UUID;

/**
 * Registro de trámite de solicitud ciudadana para vincular una cuenta de usuario
 * con un contribuyente tributario titular o representado.
 */
@Entity
@Table(name = "tributaria_solicitudes_vinculacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudVinculacionTributaria extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vinculacion_id", nullable = false)
    private VinculacionUsuarioContribuyente vinculacion;

    @Column(name = "codigo_solicitud", nullable = false, unique = true, length = 50)
    private String codigoSolicitud;

    @Column(name = "tipo_solicitud", nullable = false, length = 40)
    private String tipoSolicitud;

    @Column(name = "sustento", nullable = false, columnDefinition = "TEXT")
    private String sustento;

    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private String estado = "REGISTRADA";

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}

