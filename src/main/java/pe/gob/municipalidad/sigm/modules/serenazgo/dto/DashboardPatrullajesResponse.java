package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardPatrullajesResponse {
    private long total;
    private long programados;
    private long asignados;
    private long enCurso;
    private long enPausa;
    private long finalizados;
    private long cancelados;
    private long aPie;
    private long motorizados;
    private long integradosPNP;
}
