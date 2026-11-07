package pe.gob.municipalidad.sigm.modules.serenazgo.dto.estadisticas;

import lombok.*;

import java.util.UUID;

/**
 * Agregación estadística por sector y zona de patrullaje.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasTerritorialesDto {

    private UUID sectorId;
    private String sectorCodigo;
    private String sectorNombre;

    private long totalIncidencias;
    private long totalPatrullajes;
    private long totalIntervenciones;

    private Double porcentajeIncidencias;
}
