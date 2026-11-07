package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.AccionHistorialObligacion;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Registro de trazabilidad y auditoría de transiciones y eventos de una obligación tributaria.
 */
@Entity
@Table(name = "tributaria_historial_obligaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialObligacionTributaria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obligacion_id", nullable = false)
    private ObligacionTributaria obligacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 50)
    private AccionHistorialObligacion accion;

    @Column(name = "estado_anterior", length = 40)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 40)
    private String estadoNuevo;

    @Column(name = "importe_anterior", precision = 14, scale = 2)
    private BigDecimal importeAnterior;

    @Column(name = "importe_nuevo", precision = 14, scale = 2)
    private BigDecimal importeNuevo;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "resolucion_sustento", length = 150)
    private String resolucionSustento;

    @Column(name = "usuario_registro", length = 100)
    private String usuarioRegistro;

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaRegistro = OffsetDateTime.now();
}
