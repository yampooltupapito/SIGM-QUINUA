package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ResultadoGeneracionItem;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleLoteResponse {
    private UUID id;
    private UUID determinacionId;
    private String codigoDeterminacion;
    private UUID contribuyenteId;
    private String contribuyenteNombre;
    private String contribuyenteDocumento;
    private UUID obligacionId;
    private String obligacionCodigo;
    private ResultadoGeneracionItem resultado;
    private BigDecimal importeGenerado;
    private String mensajeDetalle;
    private OffsetDateTime createdAt;

    public String getResultadoStyle() {
        return resultado != null ? resultado.getStyleClass() : "";
    }
}
