package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacionFraccionamientoResponse {
    private String programaCodigo;
    private String programaNombre;
    private BigDecimal importeBase;
    private BigDecimal cuotaInicial;
    private BigDecimal importeFinanciado;
    private BigDecimal tasaInteresMensual;
    private BigDecimal totalIntereses;
    private BigDecimal importeTotalFinanciado;
    private Integer numeroCuotas;
    private LocalDate fechaPrimeraCuota;
    private List<CuotaSimuladaDto> cuotas;
    private String notaLegal;
}
