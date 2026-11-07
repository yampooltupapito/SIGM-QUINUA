package pe.gob.municipalidad.sigm.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una oficina o unidad orgánica municipal.
 * Soporta jerarquía institucional sin ciclos.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"municipalidad", "oficinaPadre", "suboficinas"})
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "oficinas")
public class Oficina extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipalidad_id", nullable = false)
    private Municipalidad municipalidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_padre_id")
    private Oficina oficinaPadre;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "sigla", length = 30)
    private String sigla;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Builder.Default
    @Column(name = "nivel_jerarquico", nullable = false)
    private Integer nivelJerarquico = 1;

    @Builder.Default
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVO";

    @Builder.Default
    @OneToMany(mappedBy = "oficinaPadre")
    private List<Oficina> suboficinas = new ArrayList<>();
}
