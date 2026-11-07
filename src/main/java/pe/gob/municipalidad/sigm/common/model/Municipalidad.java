package pe.gob.municipalidad.sigm.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * Entidad institucional que representa la entidad municipal (Municipalidad Distrital de Quinua).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "oficinas")
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "municipalidades")
public class Municipalidad extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "nombre_corto", nullable = false, length = 100)
    private String nombreCorto;

    @Column(name = "ubigeo", nullable = false, length = 6)
    private String ubigeo;

    @Column(name = "departamento", nullable = false, length = 50)
    private String departamento;

    @Column(name = "provincia", nullable = false, length = 50)
    private String provincia;

    @Column(name = "distrito", nullable = false, length = 50)
    private String distrito;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "correo_institucional", length = 100)
    private String correoInstitucional;

    @Builder.Default
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVO";

    @Builder.Default
    @OneToMany(mappedBy = "municipalidad")
    private List<Oficina> oficinas = new ArrayList<>();
}
