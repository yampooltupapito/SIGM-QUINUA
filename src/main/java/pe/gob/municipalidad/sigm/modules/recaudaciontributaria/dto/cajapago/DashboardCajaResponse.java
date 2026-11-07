package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.cajapago;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardCajaResponse {
    private long cajasActivas;
    private long sesionesAbiertas;
    private long pagosConfirmados;
    private long pagosPendientes;
    private BigDecimal recaudacionPeriodo;
    private BigDecimal efectivoRecaudado;
    private long operacionesNoConciliadas;
    private BigDecimal diferenciasArqueo;
}
