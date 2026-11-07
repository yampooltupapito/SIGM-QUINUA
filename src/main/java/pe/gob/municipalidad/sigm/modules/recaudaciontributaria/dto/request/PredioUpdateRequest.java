package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredioUpdateRequest {

    @NotNull(message = "El tipo de predio es obligatorio")
    private TipoPredio tipoPredio;

    @NotNull(message = "La clasificación del predio es obligatoria")
    private ClasificacionPredio clasificacionPredio;

    @NotNull(message = "El uso declarado es obligatorio")
    private UsoDeclaradoPredio usoDeclarado;

    private String codigoCatastral;

    // Jurisdicción territorial
    @Builder.Default
    private String departamento = "AYACUCHO";
    @Builder.Default
    private String provincia = "HUAMANGA";
    @Builder.Default
    private String distrito = "QUINUA";
    @Builder.Default
    private String ubigeo = "050110";

    private String comunidadCentroPoblado;
    private String tipoVia;
    private String nombreVia;
    private String numeroMunicipal;
    private String manzana;
    private String lote;
    private String interior;
    private String direccion;
    private String referenciaUbicacion;
    private String referencia;

    private UUID sectorId;
    private String sectorNombre;
    private String zona;
    private String sector;

    @NotNull(message = "El área del terreno es obligatoria")
    @DecimalMin(value = "0.01", message = "El área debe ser mayor a cero")
    private BigDecimal areaTerreno;

    @Builder.Default
    private String unidadMedidaArea = "M2";

    @Builder.Default
    private String unidadArea = "M2";

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

    private String observaciones;

    public String getUnidadArea() {
        if (unidadArea != null && !unidadArea.isBlank() && !"M2".equalsIgnoreCase(unidadArea)) {
            return unidadArea;
        }
        if (unidadMedidaArea != null && !unidadMedidaArea.isBlank() && !"M2".equalsIgnoreCase(unidadMedidaArea)) {
            return unidadMedidaArea;
        }
        return (unidadArea != null && !unidadArea.isBlank()) ? unidadArea : (unidadMedidaArea != null ? unidadMedidaArea : "M2");
    }

    public String getUnidadMedidaArea() {
        return getUnidadArea();
    }

    public String getReferencia() {
        return referencia != null ? referencia : referenciaUbicacion;
    }

    public String getReferenciaUbicacion() {
        return referenciaUbicacion != null ? referenciaUbicacion : referencia;
    }
}
