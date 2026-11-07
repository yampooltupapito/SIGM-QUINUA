package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoVinculacionContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoVinculacionContribuyente;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad que vincula de manera explícita, auditable y verificada una cuenta de usuario
 * autenticado con un registro de Contribuyente Tributario en SIGM-QUINUA.
 */
@Entity
@Table(name = "tributaria_vinculaciones_usuario_contribuyente", uniqueConstraints = {
        @UniqueConstraint(name = "uq_vinculacion_usuario_contribuyente", columnNames = {"usuario_id", "contribuyente_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VinculacionUsuarioContribuyente extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private ContribuyenteTributario contribuyente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vinculacion", nullable = false, length = 40)
    private TipoVinculacionContribuyente tipoVinculacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoVinculacionContribuyente estado = EstadoVinculacionContribuyente.PENDIENTE;

    @Column(name = "fecha_solicitud", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaSolicitud = OffsetDateTime.now();

    @Column(name = "fecha_verificacion")
    private OffsetDateTime fechaVerificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verificado_por")
    private Usuario verificadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_sustento_id")
    private Documento documentoSustento;

    @Column(name = "fecha_vigencia_hasta")
    private LocalDate fechaVigenciaHasta;

    @Column(name = "motivo_observacion", columnDefinition = "TEXT")
    private String motivoObservacion;

    @Column(name = "motivo_rechazo_revocacion", columnDefinition = "TEXT")
    private String motivoRechazoRevocacion;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    public boolean isActivo() {
        return Boolean.TRUE.equals(activo);
    }

    public boolean isVigente() {
        if (!Boolean.TRUE.equals(activo) || estado != EstadoVinculacionContribuyente.APROBADA) {
            return false;
        }
        if (fechaVigenciaHasta != null && fechaVigenciaHasta.isBefore(LocalDate.now())) {
            return false;
        }
        return true;
    }
}

