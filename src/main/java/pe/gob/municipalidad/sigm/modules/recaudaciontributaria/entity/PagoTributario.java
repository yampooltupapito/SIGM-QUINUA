package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad principal que representa un Pago Tributario (en ventanilla o por depósito/transferencia).
 * Gestiona el ciclo de vida del pago, su confirmación atómica y sus aplicaciones a obligaciones.
 */
@Entity
@Table(name = "tributaria_pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_pago", nullable = false, unique = true, length = 50)
    private String codigoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private ContribuyenteTributario contribuyente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_caja_id")
    private SesionCajaMunicipal sesionCaja;

    @Column(name = "fecha_hora_pago", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaHoraPago = OffsetDateTime.now();

    @Column(name = "fecha_confirmacion")
    private OffsetDateTime fechaConfirmacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_confirmacion_id")
    private Usuario usuarioConfirmacion;

    @Column(name = "fecha_reversion")
    private OffsetDateTime fechaReversion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_reversion_id")
    private Usuario usuarioReversion;

    @Column(name = "motivo_reversion", columnDefinition = "TEXT")
    private String motivoReversion;

    @Column(name = "importe", nullable = false, precision = 14, scale = 2)
    private BigDecimal importe;

    @Column(name = "moneda", nullable = false, length = 10)
    @Builder.Default
    private String moneda = "PEN";

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago", nullable = false, length = 40)
    private MedioPagoTributario medioPago;

    @Column(name = "referencia_externa", length = 100)
    private String referenciaExterna;

    @Column(name = "numero_operacion_bancaria", length = 100)
    private String numeroOperacionBancaria;

    @Column(name = "banco_origen", length = 100)
    private String bancoOrigen;

    @Column(name = "idempotency_key", length = 100, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoPagoTributario estado = EstadoPagoTributario.BORRADOR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id")
    private Usuario usuarioRegistro;

    @Column(name = "oficina_id")
    private Long oficinaId;

    @Column(name = "recibo_caja_id")
    private UUID reciboCajaId;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AplicacionPagoTributario> aplicaciones = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
