package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;

import java.time.OffsetDateTime;

/**
 * Registro de la devolución o entrega física del puesto comercial al municipio.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"asignacion", "puesto", "documentoActa"})
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_asignaciones_devoluciones")
public class AsignacionDevolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_id", nullable = false)
    private AsignacionPuesto asignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id", nullable = false)
    private PuestoComercial puesto;

    @Builder.Default
    @Column(name = "fecha_devolucion", nullable = false)
    private OffsetDateTime fechaDevolucion = OffsetDateTime.now();

    @Column(name = "responsable_recepcion", nullable = false, length = 100)
    private String responsableRecepcion;

    @Builder.Default
    @Column(name = "estado_fisico_puesto", nullable = false, length = 50)
    private String estadoFisicoPuesto = "BUENO";

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_acta_id")
    private Documento documentoActa;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
