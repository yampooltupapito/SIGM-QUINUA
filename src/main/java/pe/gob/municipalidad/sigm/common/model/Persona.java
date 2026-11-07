package pe.gob.municipalidad.sigm.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
 * Entidad que representa a una persona natural o jurídica en el ámbito de la Municipalidad de Quinua.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "asignacionesOficina")
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(
        name = "personas",
        uniqueConstraints = @UniqueConstraint(name = "uq_personas_documento", columnNames = {"tipo_documento", "numero_documento"})
)
public class Persona extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 100)
    private String apellidoMaterno;

    @Column(name = "correo", length = 150)
    private String correo;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "foto_url", length = 255)
    private String fotoUrl;

    @Builder.Default
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVO";

    @Builder.Default
    @OneToMany(mappedBy = "persona")
    private List<PersonaOficina> asignacionesOficina = new ArrayList<>();

    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder(nombres).append(" ").append(apellidoPaterno);
        if (apellidoMaterno != null && !apellidoMaterno.trim().isEmpty()) {
            sb.append(" ").append(apellidoMaterno);
        }
        return sb.toString();
    }
}
