package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.util.UUID;

/**
 * Preferencias de comunicación y avisos tributarios configuradas por el ciudadano.
 */
@Entity
@Table(name = "tributaria_preferencias_notificacion", uniqueConstraints = {
        @UniqueConstraint(name = "uq_preferencias_usuario_contribuyente", columnNames = {"usuario_id", "contribuyente_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenciaNotificacionTributaria extends AuditableEntity {

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

    @Column(name = "notificar_vencimientos", nullable = false)
    @Builder.Default
    private Boolean notificarVencimientos = true;

    @Column(name = "notificar_pagos_confirmados", nullable = false)
    @Builder.Default
    private Boolean notificarPagosConfirmados = true;

    @Column(name = "notificar_recibos", nullable = false)
    @Builder.Default
    private Boolean notificarRecibos = true;

    @Column(name = "notificar_notificaciones_formales", nullable = false)
    @Builder.Default
    private Boolean notificarNotificacionesFormales = true;

    @Column(name = "correo_notificacion", length = 150)
    private String correoNotificacion;

    @Column(name = "telefono_notificacion", length = 30)
    private String telefonoNotificacion;

    @Column(name = "canal_preferido", nullable = false, length = 30)
    @Builder.Default
    private String canalPreferido = "PORTAL";

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}

