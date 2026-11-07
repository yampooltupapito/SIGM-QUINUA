package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Auditoría individual de consultas ciudadanas en el portal tributario.
 * Permite al contribuyente auditar cuándo y desde qué IP se accedió a su información.
 */
@Entity
@Table(name = "tributaria_historial_acceso_portal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialAccesoPortalTributario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private ContribuyenteTributario contribuyente;

    @Column(name = "seccion_accedida", nullable = false, length = 60)
    private String seccionAccedida;

    @Column(name = "ip_origen", length = 60)
    private String ipOrigen;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "fecha_acceso", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaAcceso = OffsetDateTime.now();
}

