package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCalculoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoCalculoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoServicioArbitrio;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArbitrioFilterDto {
    private String query;
    private Integer ejercicioFiscal;
    private UUID periodoId;
    private UUID servicioId;
    private TipoServicioArbitrio tipoServicio;
    private UUID contribuyenteId;
    private UUID predioId;
    private EstadoCalculoArbitrio estado;
    private TipoCalculoArbitrio tipoCalculo;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
}
