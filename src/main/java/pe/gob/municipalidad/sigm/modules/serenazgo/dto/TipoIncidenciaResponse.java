package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoIncidenciaResponse {
    private UUID id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String prioridadSugerida;
    private boolean requiereUnidad;
    private boolean activo;
}
