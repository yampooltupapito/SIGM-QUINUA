package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDomicilioContribuyente;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Domicilio fiscal, procesal, declarado o rural de un Contribuyente Tributario.
 * Soporta zonas urbanas y comunidades campesinas/centros poblados del distrito de Quinua.
 */
@Entity
@Table(name = "tributaria_domicilios_contribuyente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomicilioContribuyente extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private ContribuyenteTributario contribuyente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_domicilio", nullable = false, length = 40)
    @Builder.Default
    private TipoDomicilioContribuyente tipoDomicilio = TipoDomicilioContribuyente.DECLARADO;

    @Column(name = "departamento", nullable = false, length = 100)
    @Builder.Default
    private String departamento = "Ayacucho";

    @Column(name = "provincia", nullable = false, length = 100)
    @Builder.Default
    private String provincia = "Huamanga";

    @Column(name = "distrito", nullable = false, length = 100)
    @Builder.Default
    private String distrito = "Quinua";

    @Column(name = "ubigeo", nullable = false, length = 10)
    @Builder.Default
    private String ubigeo = "050108";

    @Column(name = "tipo_via", length = 50)
    private String tipoVia;

    @Column(name = "nombre_via", length = 150)
    private String nombreVia;

    @Column(name = "numero_municipal", length = 30)
    private String numeroMunicipal;

    @Column(name = "interior", length = 30)
    private String interior;

    @Column(name = "manzana_lote", length = 50)
    private String manzanaLote;

    @Column(name = "comunidad_centro_poblado", length = 150)
    private String comunidadCentroPoblado;

    @Column(name = "referencia", columnDefinition = "TEXT")
    private String referencia;

    @Column(name = "es_fiscal", nullable = false)
    @Builder.Default
    private boolean esFiscal = false;

    @Column(name = "es_notificacion", nullable = false)
    @Builder.Default
    private boolean esNotificacion = false;

    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private String estado = "ACTIVO";

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private LocalDate fechaRegistro = LocalDate.now();

    @Column(name = "fecha_fin_vigencia")
    private LocalDate fechaFinVigencia;

    public String getDireccionCompleta() {
        StringBuilder sb = new StringBuilder();
        if (comunidadCentroPoblado != null && !comunidadCentroPoblado.isBlank()) {
            sb.append("C.P./Comunidad: ").append(comunidadCentroPoblado).append(", ");
        }
        if (tipoVia != null && !tipoVia.isBlank()) {
            sb.append(tipoVia).append(" ");
        }
        if (nombreVia != null && !nombreVia.isBlank()) {
            sb.append(nombreVia).append(" ");
        }
        if (numeroMunicipal != null && !numeroMunicipal.isBlank()) {
            sb.append("N° ").append(numeroMunicipal).append(" ");
        }
        if (manzanaLote != null && !manzanaLote.isBlank()) {
            sb.append("Mz/Lt: ").append(manzanaLote).append(" ");
        }
        if (interior != null && !interior.isBlank()) {
            sb.append("Int: ").append(interior).append(" ");
        }
        sb.append("- ").append(distrito).append(", ").append(provincia).append(", ").append(departamento);
        return sb.toString();
    }
}
