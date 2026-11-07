package pe.gob.municipalidad.sigm.common.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Entidad que almacena las preferencias de canales y categorías de notificación de un usuario.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "usuario")
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "notificacion_preferencias")
public class NotificacionPreferencias implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Builder.Default
    @Column(name = "canal_interno", nullable = false)
    private Boolean canalInterno = true;

    @Builder.Default
    @Column(name = "canal_email", nullable = false)
    private Boolean canalEmail = true;

    @Builder.Default
    @Column(name = "alertas_seguridad", nullable = false)
    private Boolean alertasSeguridad = true;

    @Builder.Default
    @Column(name = "avisos_tramites", nullable = false)
    private Boolean avisosTramites = true;

    @Builder.Default
    @Column(name = "avisos_documentos", nullable = false)
    private Boolean avisosDocumentos = true;

    @Builder.Default
    @Column(name = "avisos_pagos", nullable = false)
    private Boolean avisosPagos = true;

    @Builder.Default
    @Column(name = "comunicaciones_generales", nullable = false)
    private Boolean comunicacionesGenerales = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
