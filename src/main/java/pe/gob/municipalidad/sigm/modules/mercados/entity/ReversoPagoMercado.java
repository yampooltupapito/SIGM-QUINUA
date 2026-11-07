package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "mercados_pagos_reversos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReversoPagoMercado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false)
    private PagoMercado pago;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "documento_sustento", length = 100)
    private String documentoSustento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_reverso_id")
    private Usuario usuarioReversoEntity;

    @Column(name = "usuario_reverso", nullable = false, length = 100)
    private String usuarioReverso;

    @Column(name = "fecha_reverso", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaReverso = OffsetDateTime.now();

    @Column(name = "monto_reversado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoReversado;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
