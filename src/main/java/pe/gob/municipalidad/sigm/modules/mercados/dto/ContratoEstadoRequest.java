package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoEstadoRequest {

    private String motivo;
    private String documentoReferencia;
    private LocalDate fechaEfectiva;
    private LocalDate fechaFin;
}
