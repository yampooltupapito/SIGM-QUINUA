package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pe.gob.municipalidad.sigm.common.model.Usuario;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_zonas_historial_sector")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialReasignacionZona {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id", nullable = false)
    private ZonaPatrullaje zona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_anterior_id", nullable = false)
    private SectorPatrullaje sectorAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_nuevo_id", nullable = false)
    private SectorPatrullaje sectorNuevo;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "documento_referencia", length = 150)
    private String documentoReferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id")
    private Usuario usuarioRegistro;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
