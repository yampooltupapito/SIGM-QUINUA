package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredioCiudadanoDto {
    private UUID id;
    private String codigoPredio;
    private String codigoCatastral;
    private String direccionCompleta;
    private String referencia;
    private String tipoPredio;
    private String estadoPredio;
    private String tipoRelacion;
    private BigDecimal porcentajeParticipacion;
    private Boolean esPrincipal;
    private BigDecimal areaTerreno;
    private BigDecimal areaConstruidaTotal;
    private BigDecimal autoavaluoTotal;
    private LocalDate fechaRegistro;
}

