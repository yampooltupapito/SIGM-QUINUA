package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoTurnoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoTurnoSerenazgo;

import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_turnos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnoSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_turno", nullable = false, length = 30)
    @Builder.Default
    private TipoTurnoSerenazgo tipoTurno = TipoTurnoSerenazgo.DIURNO;

    @Column(name = "color_hex", length = 20)
    @Builder.Default
    private String colorHex = "#2563eb";

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "cruza_medianoche", nullable = false)
    @Builder.Default
    private Boolean cruzaMedianoche = false;

    @Column(name = "duracion_referencial_minutos")
    private Integer duracionReferencialMinutos;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoTurnoSerenazgo estado = EstadoTurnoSerenazgo.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Calcula la duración referencial en minutos garantizando que turnos que cruzan
     * medianoche no generen duraciones negativas.
     */
    public int calcularDuracionMinutos() {
        if (horaInicio == null || horaFin == null) {
            return 0;
        }
        if (Boolean.TRUE.equals(cruzaMedianoche) || horaFin.isBefore(horaInicio)) {
            // Cruza medianoche: minutos desde inicio hasta las 24:00 + minutos desde 00:00 hasta fin
            int minutosHastaMedianoche = 1440 - (horaInicio.getHour() * 60 + horaInicio.getMinute());
            int minutosDesdeMedianoche = horaFin.getHour() * 60 + horaFin.getMinute();
            return minutosHastaMedianoche + minutosDesdeMedianoche;
        } else {
            return (int) Duration.between(horaInicio, horaFin).toMinutes();
        }
    }
}
