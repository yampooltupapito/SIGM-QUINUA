package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevolverUnidadRequest {

    private OffsetDateTime fechaDevolucion;

    private BigDecimal kilometrajeRetorno;

    private String estadoRetornoUnidad;

    private String observacionesDevolucion;
}
