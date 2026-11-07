package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPagoMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.MedioPagoMercado;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mercados_pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoMercado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_pago", nullable = false, unique = true, length = 50)
    private String codigoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comerciante_id", nullable = false)
    private Comerciante comerciante;

    @Column(name = "fecha_pago", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaPago = OffsetDateTime.now();

    @Column(name = "monto_recibido", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoRecibido;

    @Column(name = "monto_aplicado", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montoAplicado = BigDecimal.ZERO;

    @Column(name = "moneda", nullable = false, length = 3)
    @Builder.Default
    private String moneda = "PEN";

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago", nullable = false, length = 40)
    private MedioPagoMercado medioPago;

    @Column(name = "referencia_operacion", length = 100)
    private String referenciaOperacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoPagoMercado estado = EstadoPagoMercado.CONFIRMADO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_sesion_id")
    private SesionCajaMercado sesionCaja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id")
    private Usuario usuarioRegistroEntity;

    @Column(name = "usuario_registro", nullable = false, length = 100)
    private String usuarioRegistro;

    @Column(name = "codigo_seguro", unique = true, length = 64)
    private String codigoSeguro;

    @Column(name = "hash_comprobante", length = 128)
    private String hashComprobante;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AplicacionPagoMercado> aplicaciones = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
