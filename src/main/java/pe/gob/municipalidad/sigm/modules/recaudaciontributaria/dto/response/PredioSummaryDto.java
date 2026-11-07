package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredioSummaryDto {
    private UUID id;
    private String codigoPredio;
    private String codigoCatastral;
    private String tipoPredio;
    private String tipoPredioDesc;
    private String clasificacionPredioDesc;
    private String direccion;
    private String sectorNombre;
    private BigDecimal areaTerreno;
    private BigDecimal areaTerrenoM2;
    private BigDecimal areaConstruida;
    private String unidadMedidaArea;
    private String primerContribuyenteNombre;
    private String referencia;
    private String estado;
    private String estadoDesc;
    private String badgeClass;
    private String fechaRegistro;
}
