package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoContribuyente;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContribuyenteFilterDto {
    private String query;
    private TipoContribuyente tipo;
    private EstadoContribuyente estado;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private Long oficinaId;
}
