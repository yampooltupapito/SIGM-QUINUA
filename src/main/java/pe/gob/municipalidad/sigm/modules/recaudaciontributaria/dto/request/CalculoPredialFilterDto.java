package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoPredial;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoCalculoPredial;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculoPredialFilterDto {
    private String query;
    private Integer ejercicioFiscal;
    private UUID periodoId;
    private UUID contribuyenteId;
    private EstadoCalculoPredial estado;
    private TipoCalculoPredial tipoCalculo;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
}
