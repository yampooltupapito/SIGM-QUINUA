package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPersonalSerenazgo;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalSerenazgoFilter {
    private String textoBusqueda;
    private EstadoPersonalSerenazgo estado;
    private UUID cargoOperativoId;
    private Long oficinaId;
    private LocalDate fechaIngresoDesde;
    private LocalDate fechaIngresoHasta;
    private Boolean soloDisponibles;
}
