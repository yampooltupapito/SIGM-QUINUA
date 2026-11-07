package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoOperacionTitularidad;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Registro inmutable de cambios de titularidad, transferencias y renovaciones por puesto comercial.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"puesto", "solicitud", "asignacionAnterior", "asignacionNueva", "titularAnterior", "titularNuevo", "documentoSustento"})
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_historial_titularidad")
public class HistorialTitularidadPuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id", nullable = false)
    private PuestoComercial puesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id")
    private SolicitudGestionOcupacion solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_anterior_id")
    private AsignacionPuesto asignacionAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_nueva_id")
    private AsignacionPuesto asignacionNueva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titular_anterior_id", nullable = false)
    private Comerciante titularAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titular_nuevo_id", nullable = false)
    private Comerciante titularNuevo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false, length = 50)
    private TipoOperacionTitularidad tipoOperacion;

    @Column(name = "fecha_efectiva", nullable = false)
    private LocalDate fechaEfectiva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_sustento_id")
    private Documento documentoSustento;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "usuario_registro", nullable = false, length = 100)
    private String usuarioRegistro;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
