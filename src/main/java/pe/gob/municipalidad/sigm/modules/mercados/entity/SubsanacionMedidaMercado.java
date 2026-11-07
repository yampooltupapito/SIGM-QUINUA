package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "mercados_subsanaciones_medida")
public class SubsanacionMedidaMercado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medida_correctiva_id", nullable = false)
    private MedidaCorrectivaMercado medidaCorrectiva;

    @Column(name = "descripcion_subsanacion", nullable = false, columnDefinition = "TEXT")
    private String descripcionSubsanacion;

    @CreationTimestamp
    @Column(name = "fecha_presentacion", nullable = false, updatable = false)
    private OffsetDateTime fechaPresentacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presentado_por_id")
    private Usuario presentadoPor;

    @Column(name = "documento_evidencia_id")
    private UUID documentoEvidenciaId;

    @Column(name = "estado_verificacion", nullable = false, length = 30)
    @Builder.Default
    private String estadoVerificacion = "PENDIENTE_REVISION"; // PENDIENTE_REVISION, APROBADA_CONFORME, RECHAZADA_INSUFICIENTE

    @Column(name = "fecha_verificacion")
    private OffsetDateTime fechaVerificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_verificador_id")
    private Usuario inspectorVerificador;

    @Column(name = "observaciones_verificacion", columnDefinition = "TEXT")
    private String observacionesVerificacion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
