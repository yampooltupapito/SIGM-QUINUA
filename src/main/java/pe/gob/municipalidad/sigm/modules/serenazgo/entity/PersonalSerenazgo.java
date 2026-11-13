package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPersonalSerenazgo;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_personal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_interno", nullable = false, unique = true, length = 50)
    private String codigoInterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id")
    private Oficina oficina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_operativo_id")
    private CargoOperativoSerenazgo cargoOperativo;

    @Column(name = "tipo_documento_laboral", length = 50)
    private String tipoDocumentoLaboral;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoPersonalSerenazgo estado = EstadoPersonalSerenazgo.ACTIVO;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /** Alias de solo lectura para las vistas: nombre completo de la persona asociada. */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getNombreCompleto() {
        return persona != null ? persona.getNombreCompleto() : codigoInterno;
    }

    /** Alias de solo lectura para las vistas: nombre del cargo operativo. */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getCargo() {
        return cargoOperativo != null ? cargoOperativo.getNombre() : null;
    }

    public String getCodigoEmpleado() {
        return codigoInterno;
    }

    public void setCodigoEmpleado(String c) {
        this.codigoInterno = c;
    }
}
