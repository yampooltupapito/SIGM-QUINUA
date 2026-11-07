package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Valor individual capturado para un campo dinámico de formulario.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mpv_respuesta_valores",
       uniqueConstraints = @UniqueConstraint(name = "uq_mpv_resp_campo", columnNames = {"respuesta_id", "campo_id"}))
public class RespuestaValor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "respuesta_id", nullable = false)
    private RespuestaFormulario respuesta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campo_id", nullable = false)
    private FormularioCampo campo;

    @Column(name = "valor_texto", columnDefinition = "TEXT")
    private String valorTexto;
}
