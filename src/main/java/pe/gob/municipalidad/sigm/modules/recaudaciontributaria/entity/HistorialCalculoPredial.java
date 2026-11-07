package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.AccionHistorialCalculoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoPredial;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Auditoría inmutable de transiciones de estado, revisiones y anulaciones
 * de los cálculos del Impuesto Predial.
 */
@Entity
@Table(name = "tributaria_historial_calculo_predial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialCalculoPredial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculo_id", nullable = false)
    private CalculoImpuestoPredial calculo;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 50)
    private AccionHistorialCalculoPredial accion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 30)
    private EstadoCalculoPredial estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "nuevo_estado", nullable = false, length = 30)
    private EstadoCalculoPredial nuevoEstado;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "resolucion_sustento", length = 150)
    private String resolucionSustento;

    @Column(name = "usuario_registro", nullable = false, length = 100)
    private String usuarioRegistro;

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaRegistro = OffsetDateTime.now();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalles_cambios", columnDefinition = "jsonb")
    private String detallesCambios;
}
