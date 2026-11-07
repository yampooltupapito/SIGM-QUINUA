package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.AccionHistorialConfiguracion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConfiguracionConcepto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Registro inmutable de auditoría para el ciclo de vida y versionado de configuraciones tributarias.
 */
@Entity
@Table(name = "tributaria_historial_configuraciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialConfiguracionTributaria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuracion_id", nullable = false)
    private ConfiguracionConceptoTributario configuracion;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 50)
    private AccionHistorialConfiguracion accion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 30)
    private EstadoConfiguracionConcepto estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "nuevo_estado", nullable = false, length = 30)
    private EstadoConfiguracionConcepto nuevoEstado;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "resolucion_sustento", length = 150)
    private String resolucionSustento;

    @Column(name = "usuario_registro", nullable = false, length = 100)
    private String usuarioRegistro;

    @Column(name = "oficina_id")
    private Long oficinaId;

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaRegistro = OffsetDateTime.now();

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "detalles_cambios", columnDefinition = "jsonb")
    private String detallesCambios;
}
