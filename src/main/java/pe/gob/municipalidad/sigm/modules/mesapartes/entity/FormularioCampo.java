package pe.gob.municipalidad.sigm.modules.mesapartes.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCampoFormulario;

import java.util.ArrayList;
import java.util.List;

/**
 * Definición individual de un campo dinámico perteneciente a una versión de formulario.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mpv_formulario_campos",
       uniqueConstraints = @UniqueConstraint(name = "uq_mpv_campo_version", columnNames = {"version_id", "nombre_campo"}))
public class FormularioCampo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_id", nullable = false)
    private FormularioVersion version;

    @Column(name = "nombre_campo", length = 100, nullable = false)
    private String nombreCampo;

    @Column(name = "etiqueta", length = 200, nullable = false)
    private String etiqueta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_campo", length = 30, nullable = false)
    private TipoCampoFormulario tipoCampo;

    @Column(name = "obligatorio", nullable = false)
    @Builder.Default
    private Boolean obligatorio = false;

    @Column(name = "longitud_maxima")
    private Integer longitudMaxima;

    @Column(name = "valor_defecto", length = 255)
    private String valorDefecto;

    @Column(name = "placeholder", length = 255)
    private String placeholder;

    @Column(name = "mensaje_ayuda", length = 500)
    private String mensajeAyuda;

    @Column(name = "regla_validacion", length = 255)
    private String reglaValidacion;

    @Column(name = "orden", nullable = false)
    @Builder.Default
    private Integer orden = 1;

    @OneToMany(mappedBy = "campo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<FormularioCampoOpcion> opciones = new ArrayList<>();
}
