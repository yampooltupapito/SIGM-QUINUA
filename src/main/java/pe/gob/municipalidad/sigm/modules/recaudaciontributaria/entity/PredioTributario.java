package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity;

import jakarta.persistence.*;
import lombok.*;
import pe.gob.municipalidad.sigm.common.audit.AuditableEntity;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.SectorPatrullaje;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad de Predio Tributario Municipal (Urbano y Rural) del Distrito de Quinua.
 * Registra características físicas, superficies en m2 y hectáreas, servicios, coordenadas y relaciones.
 */
@Entity
@Table(name = "tributaria_predios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredioTributario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_predio", nullable = false, unique = true, length = 50)
    private String codigoPredio;

    @Column(name = "codigo_catastral", length = 50)
    private String codigoCatastral;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_predio", nullable = false, length = 30)
    @Builder.Default
    private TipoPredio tipoPredio = TipoPredio.URBANO;

    @Enumerated(EnumType.STRING)
    @Column(name = "clasificacion_predio", nullable = false, length = 50)
    @Builder.Default
    private ClasificacionPredio clasificacionPredio = ClasificacionPredio.CASA_HABITACION;

    @Enumerated(EnumType.STRING)
    @Column(name = "uso_declarado", nullable = false, length = 50)
    @Builder.Default
    private UsoDeclaradoPredio usoDeclarado = UsoDeclaradoPredio.VIVIENDA;

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

    @Column(name = "comunidad_centro_poblado", length = 150)
    private String comunidadCentroPoblado;

    @Column(name = "tipo_via", length = 50)
    private String tipoVia;

    @Column(name = "nombre_via", length = 150)
    private String nombreVia;

    @Column(name = "numero_municipal", length = 30)
    private String numeroMunicipal;

    @Column(name = "manzana", length = 30)
    private String manzana;

    @Column(name = "lote", length = 30)
    private String lote;

    @Column(name = "interior", length = 30)
    private String interior;

    @Column(name = "zona", length = 100)
    private String zona;

    @Column(name = "direccion", nullable = false, length = 255)
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private SectorPatrullaje sector;

    @Column(name = "sector_nombre", length = 120)
    private String sectorNombre;

    @Column(name = "referencia_ubicacion", columnDefinition = "TEXT")
    private String referenciaUbicacion;

    @Column(name = "area_terreno", nullable = false, precision = 12, scale = 4)
    private BigDecimal areaTerreno;

    @Column(name = "area_terreno_m2", nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal areaTerrenoM2 = BigDecimal.ZERO;

    @Column(name = "area_construida", nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal areaConstruida = BigDecimal.ZERO;

    @Column(name = "unidad_medida_area", nullable = false, length = 10)
    @Builder.Default
    private String unidadMedidaArea = "M2";

    @Column(name = "valor_arancelario_referencial", precision = 14, scale = 2)
    private BigDecimal valorArancelarioReferencial;

    @Column(name = "valor_autovaluo_referencial", precision = 14, scale = 2)
    private BigDecimal valorAutovaluoReferencial;

    // Servicios Básicos
    @Column(name = "tiene_agua", nullable = false)
    @Builder.Default
    private boolean tieneAgua = false;

    @Column(name = "tiene_desague", nullable = false)
    @Builder.Default
    private boolean tieneDesague = false;

    @Column(name = "tiene_luz", nullable = false)
    @Builder.Default
    private boolean tieneLuz = false;

    @Column(name = "tiene_alumbrado_publico", nullable = false)
    @Builder.Default
    private boolean tieneAlumbradoPublico = false;

    @Column(name = "tiene_pistas_veredas", nullable = false)
    @Builder.Default
    private boolean tienePistasVeredas = false;

    @Column(name = "tiene_limpieza_publica", nullable = false)
    @Builder.Default
    private boolean tieneLimpiezaPublica = false;

    // Coordenadas WGS84 Opcionales
    @Column(name = "latitud", precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 7)
    private BigDecimal longitud;

    @Column(name = "precision_metros")
    private Integer precisionMetros;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuente_coordenadas", nullable = false, length = 50)
    @Builder.Default
    private FuenteCoordenadas fuenteCoordenadas = FuenteCoordenadas.NO_DISPONIBLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoPredio estado = EstadoPredio.ACTIVO;

    @Column(name = "motivo_cambio_estado", columnDefinition = "TEXT")
    private String motivoCambioEstado;

    @Column(name = "fecha_cambio_estado")
    private OffsetDateTime fechaCambioEstado;

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private LocalDate fechaRegistro = LocalDate.now();

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToMany(mappedBy = "predio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ConstruccionPredial> construcciones = new ArrayList<>();

    @OneToMany(mappedBy = "predio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RelacionContribuyentePredio> relacionesContribuyente = new ArrayList<>();

    @OneToMany(mappedBy = "predio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentoPredio> documentos = new ArrayList<>();

    @OneToMany(mappedBy = "predio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HistorialEstadoPredio> historialEstados = new ArrayList<>();

    public boolean tieneCoordenadasValidas() {
        return latitud != null && longitud != null &&
               latitud.compareTo(BigDecimal.valueOf(-90)) >= 0 && latitud.compareTo(BigDecimal.valueOf(90)) <= 0 &&
               longitud.compareTo(BigDecimal.valueOf(-180)) >= 0 && longitud.compareTo(BigDecimal.valueOf(180)) <= 0;
    }

    public void recalcularAreaConstruidaTotal() {
        if (construcciones == null || construcciones.isEmpty()) {
            this.areaConstruida = BigDecimal.ZERO;
            return;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (ConstruccionPredial c : construcciones) {
            if (c.getAreaConstruida() != null) {
                total = total.add(c.getAreaConstruida());
            }
        }
        this.areaConstruida = total;
    }

    public String getDireccionCompleta() {
        if (direccion != null && !direccion.isBlank()) {
            return direccion;
        }
        StringBuilder sb = new StringBuilder();
        if (comunidadCentroPoblado != null && !comunidadCentroPoblado.isBlank()) {
            sb.append("C.P. / Comunidad: ").append(comunidadCentroPoblado).append(", ");
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
        if (manzana != null && !manzana.isBlank()) {
            sb.append("Mz. ").append(manzana).append(" ");
        }
        if (lote != null && !lote.isBlank()) {
            sb.append("Lt. ").append(lote).append(" ");
        }
        sb.append("- Quinua");
        return sb.toString();
    }
}
