package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DuplicateCheckResponse {
    private boolean existeDuplicadoExacto;
    private String mensajeDuplicadoExacto;
    private List<String> coincidenciasPotenciales;
}
