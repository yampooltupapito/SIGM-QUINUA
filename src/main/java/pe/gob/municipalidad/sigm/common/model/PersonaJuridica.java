package pe.gob.municipalidad.sigm.common.model;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoRegistroInterno;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad Institucional que representa a una Persona Jurídica (Empresa, Asociación, Cooperativa)
 * en el ámbito de la Municipalidad Distrital de Quinua.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "representantes")
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(
        name = "personas_juridicas",
        uniqueConstraints = @UniqueConstraint(name = "uq_personas_juridicas_ruc", columnNames = {"ruc"})
)
public class PersonaJuridica extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "ruc", nullable = false, unique = true, length = 11)
    private String ruc;

    @Column(name = "razon_social", nullable = false, length = 255)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 255)
    private String nombreComercial;

    @Column(name = "domicilio_fiscal", length = 255)
    private String domicilioFiscal;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "correo", length = 150)
    private String correo;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_registro_interno", nullable = false, length = 30)
    private EstadoRegistroInterno estadoRegistroInterno = EstadoRegistroInterno.PENDIENTE_VERIFICACION;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Builder.Default
    @OneToMany(mappedBy = "personaJuridica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonaJuridicaRepresentante> representantes = new ArrayList<>();

    public String getNombreExposicion() {
        if (nombreComercial != null && !nombreComercial.trim().isEmpty()) {
            return nombreComercial.trim() + " (" + razonSocial.trim() + ")";
        }
        return razonSocial != null ? razonSocial.trim() : "";
    }
}
