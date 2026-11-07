package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.ClasificacionPredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoPredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.UsoDeclaradoPredio;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredioFilterDto {
    private String query;
    private TipoPredio tipo;
    private ClasificacionPredio clasificacion;
    private UsoDeclaradoPredio uso;
    private String sector;
    private String zona;
    private EstadoPredio estado;
    private UUID sectorId;
    private UUID contribuyenteId;
}
