package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudGestionOcupacionDetailResponse extends SolicitudGestionOcupacionResponse {

    private List<ObservacionGestionResponse> observaciones;
    private List<EvaluacionGestionResponse> evaluaciones;
    private List<HistorialTitularidadResponse> historialPuesto;
}
