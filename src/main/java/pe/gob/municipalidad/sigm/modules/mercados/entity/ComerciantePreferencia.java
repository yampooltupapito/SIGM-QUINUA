package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Preferencias configurables de notificación para el comerciante.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "comerciante")
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_comerciante_preferencias")
public class ComerciantePreferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comerciante_id", nullable = false, unique = true)
    private Comerciante comerciante;

    @Builder.Default
    @Column(name = "notif_email_solicitudes", nullable = false)
    private Boolean notifEmailSolicitudes = true;

    @Builder.Default
    @Column(name = "notif_email_inspecciones", nullable = false)
    private Boolean notifEmailInspecciones = true;

    @Builder.Default
    @Column(name = "notif_email_pagos", nullable = false)
    private Boolean notifEmailPagos = true;

    @Builder.Default
    @Column(name = "notif_email_vigencias", nullable = false)
    private Boolean notifEmailVigencias = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
