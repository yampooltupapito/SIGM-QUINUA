package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDomicilioContribuyente;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomicilioContribuyenteRequest {

    @NotNull(message = "El tipo de domicilio es obligatorio")
    private TipoDomicilioContribuyente tipoDomicilio;

    private String departamento;
    private String provincia;
    private String distrito;
    private String ubigeo;

    private String tipoVia;
    private String nombreVia;
    private String numeroMunicipal;
    private String interior;
    private String manzanaLote;
    private String comunidadCentroPoblado;
    private String referencia;

    private boolean esFiscal;
    private boolean esNotificacion;
}
