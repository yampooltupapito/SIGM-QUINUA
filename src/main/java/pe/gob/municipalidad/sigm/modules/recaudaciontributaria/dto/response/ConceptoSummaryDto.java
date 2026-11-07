package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConceptoSummaryDto {
    private UUID id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String tipoConcepto;
    private String periodicidad;
    private boolean aplicaAPredio;
    private String cuentaPresupuestal;
    private String estado;
    private String badgeClass;
    private String fechaInicioVigencia;
}
