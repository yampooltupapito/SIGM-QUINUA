package pe.gob.municipalidad.sigm.modules.administracion.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar los 10 módulos municipales oficiales en el sidebar dinámico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuloMenuDto {

    private String codigo;
    private String nombre;
    private String descripcion;
    private String icono;
    private String ruta;
    private boolean disponible;
    private boolean activo;
    private boolean autorizado;
}
