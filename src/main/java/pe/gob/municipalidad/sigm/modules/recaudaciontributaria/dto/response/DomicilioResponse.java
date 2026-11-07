package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoDomicilioContribuyente;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomicilioResponse {
    private UUID id;
    private TipoDomicilioContribuyente tipoDomicilio;
    private String tipoDomicilioDesc;
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
    private String estado;
    private LocalDate fechaRegistro;
    private String direccionCompleta;
}
