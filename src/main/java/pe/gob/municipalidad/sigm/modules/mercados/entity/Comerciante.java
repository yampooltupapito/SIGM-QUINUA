package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaJuridica;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad que representa a un comerciante empadronado en el sistema de mercados de Quinua.
 * Reutiliza las entidades centrales Persona y PersonaJuridica sin duplicar datos de identidad.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"persona", "personaJuridica", "usuario", "actividades"})
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "mercados_comerciantes")
public class Comerciante extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comerciante", nullable = false, length = 20)
    private TipoComerciante tipoComerciante = TipoComerciante.PERSONA_NATURAL;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private Persona persona;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_juridica_id")
    private PersonaJuridica personaJuridica;

    @Column(name = "codigo_comerciante", nullable = false, unique = true, length = 50)
    private String codigoComerciante;

    @Builder.Default
    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro = LocalDate.now();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoComerciante estado = EstadoComerciante.ACTIVO;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "mercados_comerciante_actividades",
            joinColumns = @JoinColumn(name = "comerciante_id"),
            inverseJoinColumns = @JoinColumn(name = "actividad_id")
    )
    private Set<ActividadComercial> actividades = new HashSet<>();

    public boolean isPersonaNatural() {
        return tipoComerciante == TipoComerciante.PERSONA_NATURAL;
    }

    public boolean isPersonaJuridica() {
        return tipoComerciante == TipoComerciante.PERSONA_JURIDICA;
    }

    public String getNombreOTitular() {
        if (isPersonaNatural() && persona != null) {
            return persona.getNombreCompleto();
        } else if (isPersonaJuridica() && personaJuridica != null) {
            return personaJuridica.getNombreExposicion();
        }
        return "Titular no especificado";
    }

    public String getNumeroDocumentoTitular() {
        if (isPersonaNatural() && persona != null) {
            return persona.getNumeroDocumento();
        } else if (isPersonaJuridica() && personaJuridica != null) {
            return personaJuridica.getRuc();
        }
        return "";
    }

    public String getTipoDocumentoTitular() {
        if (isPersonaNatural() && persona != null) {
            return persona.getTipoDocumento() != null ? persona.getTipoDocumento() : "DNI";
        } else if (isPersonaJuridica()) {
            return "RUC";
        }
        return "";
    }

    public String getTelefonoContacto() {
        if (isPersonaNatural() && persona != null) {
            return persona.getTelefono();
        } else if (isPersonaJuridica() && personaJuridica != null) {
            return personaJuridica.getTelefono();
        }
        return null;
    }

    public String getCorreoContacto() {
        if (isPersonaNatural() && persona != null) {
            return persona.getCorreo();
        } else if (isPersonaJuridica() && personaJuridica != null) {
            return personaJuridica.getCorreo();
        }
        return null;
    }

    public String getDireccionContacto() {
        if (isPersonaNatural() && persona != null) {
            return persona.getDireccion();
        } else if (isPersonaJuridica() && personaJuridica != null) {
            return personaJuridica.getDomicilioFiscal();
        }
        return null;
    }

    public String getCodigo() {
        return codigoComerciante;
    }
}
