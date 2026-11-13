package pe.gob.municipalidad.sigm.modules.serenazgo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoUnidadMovil;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoUnidadMovil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "serenazgo_unidades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadMovilSerenazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_interno", nullable = false, unique = true, length = 50)
    private String codigoInterno;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_unidad", nullable = false, length = 40)
    private TipoUnidadMovil tipoUnidad;

    @Column(name = "placa", unique = true, length = 20)
    private String placa;

    @Column(name = "marca", length = 100)
    private String marca;

    @Column(name = "modelo", length = 100)
    private String modelo;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "numero_motor", length = 100)
    private String numeroMotor;

    @Column(name = "numero_chasis", length = 100)
    private String numeroChasis;

    @Column(name = "codigo_patrimonial", length = 100)
    private String codigoPatrimonial;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_operativo", nullable = false, length = 30)
    @Builder.Default
    private EstadoUnidadMovil estadoOperativo = EstadoUnidadMovil.DISPONIBLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_responsable_id")
    private Oficina oficinaResponsable;

    @Column(name = "kilometraje_actual", precision = 10, scale = 2)
    private BigDecimal kilometrajeActual;

    @Column(name = "fecha_incorporacion")
    @Builder.Default
    private LocalDate fechaIncorporacion = LocalDate.now();

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

    /** Alias de solo lectura para las vistas: código interno de la unidad. */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getCodigo() {
        return codigoInterno;
    }

    /** Alias de solo lectura para las vistas: descripción del tipo de unidad. */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getTipo() {
        return tipoUnidad != null ? tipoUnidad.getDescripcion() : null;
    }
}
