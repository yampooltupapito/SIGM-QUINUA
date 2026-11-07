package pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas;

import lombok.*;

import java.util.UUID;

/**
 * Ficha técnica transferible de un indicador del catálogo oficial.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoIndicadorDto {

    private UUID id;
    private String codigo;
    private String nombre;
    private String moduloOrigen;
    private String definicion;
    private String fuenteDatos;
    private String formulaCalculo;
    private String unidadMedida;
    private String limitacionesInterpretacion;
    private String estado;
}
