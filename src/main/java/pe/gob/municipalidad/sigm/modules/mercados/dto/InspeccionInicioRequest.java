package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspeccionInicioRequest {

    private OffsetDateTime fechaInicio;
    private String observaciones;
}
