package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSesionCaja;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "mercados_cajas_sesiones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionCajaMercado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id", nullable = false)
    private CajaMercado caja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cajero_id", nullable = false)
    private Usuario usuarioCajero;

    @Column(name = "cajero_username", nullable = false, length = 100)
    private String cajeroUsername;

    @Column(name = "fecha_apertura", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaApertura = OffsetDateTime.now();

    @Column(name = "fecha_cierre")
    private OffsetDateTime fechaCierre;

    @Column(name = "monto_inicial", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montoInicial = BigDecimal.ZERO;

    @Column(name = "total_ingresos", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalIngresos = BigDecimal.ZERO;

    @Column(name = "total_reversos", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalReversos = BigDecimal.ZERO;

    @Column(name = "monto_final_esperado", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montoFinalEsperado = BigDecimal.ZERO;

    @Column(name = "monto_final_declarado", precision = 12, scale = 2)
    private BigDecimal montoFinalDeclarado;

    @Column(name = "diferencia", precision = 12, scale = 2)
    private BigDecimal diferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoSesionCaja estado = EstadoSesionCaja.ABIERTA;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

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

    public String getCodigoSesion() {
        return id != null ? id.toString().substring(0, 8) : "-";
    }
}
