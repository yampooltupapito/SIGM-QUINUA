package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.ResultadoConsultaSeguimiento;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoConsultaSeguimiento;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mpv_consultas_seguimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaSeguimientoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_consulta", nullable = false, length = 30)
    private TipoConsultaSeguimiento tipoConsulta;

    @Column(name = "criterio_busqueda", length = 100)
    private String criterioBusqueda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id")
    private Expediente expediente;

    @Column(name = "ip_origen", nullable = false, length = 45)
    private String ipOrigen;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResultadoConsultaSeguimiento resultado;

    @Column(name = "fecha_consulta", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaConsulta = OffsetDateTime.now();

    @Column(name = "tiempo_respuesta_ms")
    private Integer tiempoRespuestaMs;

    @PrePersist
    public void prePersist() {
        if (fechaConsulta == null) fechaConsulta = OffsetDateTime.now();
    }
}
