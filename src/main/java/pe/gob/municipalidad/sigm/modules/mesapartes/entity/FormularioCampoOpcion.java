package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Opción de selección para campos dinámicos de tipo SELECCION.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mpv_formulario_opciones")
public class FormularioCampoOpcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campo_id", nullable = false)
    private FormularioCampo campo;

    @Column(name = "valor", length = 100, nullable = false)
    private String valor;

    @Column(name = "etiqueta", length = 200, nullable = false)
    private String etiqueta;

    @Column(name = "orden", nullable = false)
    @Builder.Default
    private Integer orden = 1;
}
