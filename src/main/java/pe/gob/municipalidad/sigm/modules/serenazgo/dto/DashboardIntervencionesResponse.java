package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardIntervencionesResponse {

    private long total;
    private long borradores;
    private long registradas;
    private long enRevision;
    private long validadas;
    private long observadas;
    private long archivadas;

    private long totalActuaciones;
    private long totalComunicaciones;
}
