package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoRepresentanteLegal;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoRepresentacionLegal;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad de Representante Legal o Apoderado de un Contribuyente Tributario.
 */
@Entity
@Table(name = "tributaria_representantes_contribuyente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepresentanteContribuyente extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private ContribuyenteTributario contribuyente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_representante_id")
    private Persona personaRepresentante;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 30)
    private String numeroDocumento;

    @Column(name = "nombres_apellidos", nullable = false, length = 255)
    private String nombresApellidos;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_representacion", nullable = false, length = 50)
    @Builder.Default
    private TipoRepresentacionLegal tipoRepresentacion = TipoRepresentacionLegal.GERENTE_GENERAL;

    @Column(name = "partida_registral", length = 50)
    private String partidaRegistral;

    @Column(name = "asiento_registral", length = 30)
    private String asientoRegistral;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "documento_sustento_id")
    private UUID documentoSustentoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoRepresentanteLegal estado = EstadoRepresentanteLegal.ACTIVO;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    public String getNumeroDocumentoEnmascarado() {
        if (numeroDocumento == null || numeroDocumento.length() < 4) {
            return "***";
        }
        int len = numeroDocumento.length();
        return numeroDocumento.substring(0, 2) + "*".repeat(len - 4) + numeroDocumento.substring(len - 2);
    }
}
