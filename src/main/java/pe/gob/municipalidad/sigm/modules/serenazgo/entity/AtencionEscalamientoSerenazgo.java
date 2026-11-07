package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EntidadEscalamiento;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_atencion_escalamientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionEscalamientoSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atencion_id", nullable = false)
    private AtencionIncidenciaSerenazgo atencion;

    @Enumerated(EnumType.STRING)
    @Column(name = "entidad_destinataria", nullable = false, length = 50)
    private EntidadEscalamiento entidadDestinataria;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "canal_comunicacion", nullable = false, length = 50)
    @Builder.Default
    private String canalComunicacion = "TELEFONO_DIRECTO";

    @Column(name = "referencia_comunicacion", length = 100)
    private String referenciaComunicacion;

    @Column(name = "contacto_nombre", length = 150)
    private String contactoNombre;

    @CreationTimestamp
    @Column(name = "fecha_escalamiento", nullable = false, updatable = false)
    private OffsetDateTime fechaEscalamiento;

    @Column(name = "confirmacion_recepcion", nullable = false)
    @Builder.Default
    private Boolean confirmacionRecepcion = false;

    @Column(name = "fecha_confirmacion")
    private OffsetDateTime fechaConfirmacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
