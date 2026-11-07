package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredioDetailResponse {
    private UUID id;
    private String codigoPredio;
    private String codigoCatastral;
    private TipoPredio tipoPredio;
    private String tipoPredioDesc;
    private ClasificacionPredio clasificacionPredio;
    private String clasificacionPredioDesc;
    private UsoDeclaradoPredio usoDeclarado;
    private String usoDeclaradoDesc;

    private String departamento;
    private String provincia;
    private String distrito;
    private String ubigeo;
    private String comunidadCentroPoblado;
    private String tipoVia;
    private String nombreVia;
    private String numeroMunicipal;
    private String manzana;
    private String lote;
    private String interior;
    private String direccion;
    private String direccionCompleta;
    private String referenciaUbicacion;

    private UUID sectorId;
    private String sectorNombre;
    private String zona;

    private BigDecimal areaTerreno;
    private BigDecimal areaTerrenoM2;
    private BigDecimal areaConstruida;
    private String unidadMedidaArea;

    private BigDecimal valorArancelarioReferencial;
    private BigDecimal valorAutovaluoReferencial;

    private boolean tieneAgua;
    private boolean tieneDesague;
    private boolean tieneLuz;
    private boolean tieneAlumbradoPublico;
    private boolean tienePistasVeredas;
    private boolean tieneLimpiezaPublica;

    private BigDecimal latitud;
    private BigDecimal longitud;
    private Integer precisionMetros;
    private FuenteCoordenadas fuenteCoordenadas;
    private String fuenteCoordenadasDesc;
    private boolean tieneCoordenadas;

    private EstadoPredio estado;
    private String estadoDesc;
    private String badgeClass;
    private LocalDate fechaRegistro;
    private String observaciones;
    private Long version;

    private List<ConstruccionResponse> construcciones;
    private List<RelacionContribuyenteResponse> contribuyentesVinculados;
    private List<DocumentoPredioResponse> documentos;
    private List<HistorialEstadoPredioResponse> historialEstados;

    public List<RelacionContribuyenteResponse> getRelacionesContribuyente() {
        return contribuyentesVinculados;
    }

    public String getDireccionFormateada() {
        return direccionCompleta != null && !direccionCompleta.isBlank() ? direccionCompleta : direccion;
    }

    public String getUnidadArea() {
        return unidadMedidaArea != null ? unidadMedidaArea : "M2";
    }

    public String getReferencia() {
        return referenciaUbicacion;
    }

    public String getSector() {
        return sectorNombre;
    }

    public String getMotivoCambioEstado() {
        return observaciones;
    }

    public String getCreatedBy() {
        return "SISTEMA";
    }

    public String getUpdatedBy() {
        return "SISTEMA";
    }

    public LocalDateTime getCreatedAt() {
        return fechaRegistro != null ? fechaRegistro.atStartOfDay() : LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() {
        return fechaRegistro != null ? fechaRegistro.atStartOfDay() : LocalDateTime.now();
    }
}
