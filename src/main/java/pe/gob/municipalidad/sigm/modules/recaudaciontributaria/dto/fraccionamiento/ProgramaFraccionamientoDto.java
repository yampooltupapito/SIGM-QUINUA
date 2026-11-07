package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.fraccionamiento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoProgramaFraccionamiento;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramaFraccionamientoDto {
    private UUID id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private UUID normaId;
    private String normaNumero;
    private LocalDate fechaInicioVigencia;
    private LocalDate fechaFinVigencia;
    private EstadoProgramaFraccionamiento estado;
    private String estadoDescripcion;
    private String estadoBadgeClass;
    private String oficinaResponsableNombre;
    private List<ParametroFraccionamientoDto> parametros;
}
