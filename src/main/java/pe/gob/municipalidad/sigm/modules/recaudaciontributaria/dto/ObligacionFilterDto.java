package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoExigibilidadObligacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoFinancieroObligacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDeterminacionTributaria;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObligacionFilterDto {
    private String query;
    private String codigo;
    private UUID contribuyenteId;
    private UUID predioId;
    private UUID conceptoId;
    private UUID periodoId;
    private Integer anioPeriodo;
    private TipoDeterminacionTributaria tipoDeterminacion;
    private EstadoObligacionTributaria estado;
    private EstadoFinancieroObligacion estadoFinanciero;
    private EstadoExigibilidadObligacion estadoExigibilidad;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
}
