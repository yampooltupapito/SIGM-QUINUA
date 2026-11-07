package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;

import java.util.UUID;

/**
 * Catálogo de canales y medios técnicos/legales de comunicación tributaria.
 */
@Entity
@Table(name = "tributaria_medios_notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class MedioNotificacionTributaria extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "es_medio_formal", nullable = false)
    @Builder.Default
    private Boolean esMedioFormal = false;

    @Column(name = "requiere_direccion_fisica", nullable = false)
    @Builder.Default
    private Boolean requiereDireccionFisica = false;

    @Column(name = "requiere_acuse_digital", nullable = false)
    @Builder.Default
    private Boolean requiereAcuseDigital = false;

    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private String estado = "ACTIVO";

    @Version
    @Column(name = "version")
    @Builder.Default
    private Long version = 0L;
}

