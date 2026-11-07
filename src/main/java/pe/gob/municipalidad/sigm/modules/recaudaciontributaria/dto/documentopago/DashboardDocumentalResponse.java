package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.documentopago;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDocumentalResponse {
    private long totalEmitidos;
    private long totalAnulados;
    private long totalSustituidos;
    private long totalPeriodo;
    private long recibosEmitidos;
    private long constanciasEmitidas;
}
