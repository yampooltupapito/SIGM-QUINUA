package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConceptoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.NaturalezaTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.PeriodicidadConcepto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoConceptoTributario;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptoFilterDto {
    private String query;
    private TipoConceptoTributario tipoConcepto;
    private NaturalezaTributaria naturalezaTributaria;
    private PeriodicidadConcepto periodicidad;
    private EstadoConceptoTributario estado;
    private UUID periodoId;
    private Boolean aplicaAPredio;
}
