package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ObligacionDetailResponse extends ObligacionResponse {
    private List<CuotaResponse> cuotas = new ArrayList<>();
    private List<AplicacionResponse> aplicaciones = new ArrayList<>();
}
