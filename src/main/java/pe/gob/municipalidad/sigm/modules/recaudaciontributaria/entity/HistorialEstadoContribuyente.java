package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoContribuyente;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Trazabilidad y auditoría de transiciones de estados del contribuyente.
 */
@Entity
@Table(name = "tributaria_historial_estados_contribuyente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoContribuyente extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private ContribuyenteTributario contribuyente;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", nullable = false, length = 40)
    private EstadoContribuyente estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "nuevo_estado", nullable = false, length = 40)
    private EstadoContribuyente nuevoEstado;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "resolucion_sustento", length = 100)
    private String resolucionSustento;

    @Column(name = "usuario_registro", nullable = false, length = 100)
    private String usuarioRegistro;

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaRegistro = OffsetDateTime.now();
}
