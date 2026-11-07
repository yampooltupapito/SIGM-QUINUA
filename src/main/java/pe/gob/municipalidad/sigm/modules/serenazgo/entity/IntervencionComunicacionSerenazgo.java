package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CanalComunicacionDespacho;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EntidadComunicacion;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoComunicacionIntervencion;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_intervencion_comunicaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntervencionComunicacionSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intervencion_id", nullable = false)
    private IntervencionSerenazgo intervencion;

    @Enumerated(EnumType.STRING)
    @Column(name = "entidad_destinataria", nullable = false, length = 60)
    private EntidadComunicacion entidadDestinataria;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "fecha_comunicacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaComunicacion = OffsetDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false, length = 50)
    @Builder.Default
    private CanalComunicacionDespacho canal = CanalComunicacionDespacho.RADIO_TETRA_VHF;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_comunicador_id")
    private PersonalSerenazgo personalComunicador;

    @Column(name = "numero_referencia", length = 100)
    private String numeroReferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_comunicacion", nullable = false, length = 40)
    @Builder.Default
    private EstadoComunicacionIntervencion estadoComunicacion = EstadoComunicacionIntervencion.ENVIADA;

    @Column(name = "confirmado_por", length = 150)
    private String confirmadoPor;

    @Column(name = "resultado_comunicacion", columnDefinition = "TEXT")
    private String resultadoComunicacion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
