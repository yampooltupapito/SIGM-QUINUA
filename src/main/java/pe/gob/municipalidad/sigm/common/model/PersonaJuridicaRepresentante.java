package pe.gob.municipalidad.sigm.common.model;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoRepresentacion;

import java.time.LocalDate;

/**
 * Representante legal, apoderado o administrador de una persona jurídica.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"personaJuridica", "persona", "documentoSustento"})
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "personas_juridicas_representantes")
public class PersonaJuridicaRepresentante extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_juridica_id", nullable = false)
    private PersonaJuridica personaJuridica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_representacion", nullable = false, length = 50)
    private TipoRepresentacion tipoRepresentacion = TipoRepresentacion.REPRESENTANTE_LEGAL;

    @Builder.Default
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio = LocalDate.now();

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_sustento_id")
    private Documento documentoSustento;

    @Builder.Default
    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "ACTIVO";

    public boolean isVigente() {
        if (!"ACTIVO".equalsIgnoreCase(estado)) {
            return false;
        }
        LocalDate hoy = LocalDate.now();
        if (fechaFin != null && hoy.isAfter(fechaFin)) {
            return false;
        }
        return !hoy.isBefore(fechaInicio);
    }
}
