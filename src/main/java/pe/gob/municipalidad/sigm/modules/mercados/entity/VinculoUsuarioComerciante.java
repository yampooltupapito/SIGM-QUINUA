package pe.gob.municipalidad.sigm.modules.mercados.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVinculoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoVinculoComerciante;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad que vincula de manera segura y verificada una cuenta ciudadana (Usuario)
 * con un registro de comerciante de mercados (Comerciante).
 * Soporta titularidad individual y representación legal con fecha de vigencia.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"usuario", "comerciante", "verificadoPor"})
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "mercados_vinculos_usuario_comerciante")
public class VinculoUsuarioComerciante extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comerciante_id", nullable = false)
    private Comerciante comerciante;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vinculo", nullable = false, length = 50)
    private TipoVinculoComerciante tipoVinculo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoVinculoComerciante estado;

    @Builder.Default
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio = LocalDate.now();

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "documento_sustento_id")
    private UUID documentoSustentoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verificado_por_id")
    private Usuario verificadoPor;

    @Column(name = "fecha_verificacion")
    private OffsetDateTime fechaVerificacion;

    @Column(name = "motivo_rechazo_revocacion", columnDefinition = "TEXT")
    private String motivoRechazoRevocacion;

    @Column(name = "facultades_representacion", columnDefinition = "TEXT")
    private String facultadesRepresentacion;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public boolean isVigente() {
        if (estado != EstadoVinculoComerciante.VERIFICADO) {
            return false;
        }
        LocalDate hoy = LocalDate.now();
        if (fechaInicio != null && hoy.isBefore(fechaInicio)) {
            return false;
        }
        return fechaFin == null || !hoy.isAfter(fechaFin);
    }
}
