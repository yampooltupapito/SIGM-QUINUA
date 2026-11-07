package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoCiudadanoDto {
    private UUID id;
    private String codigoPago;
    private OffsetDateTime fechaPago;
    private BigDecimal montoTotal;
    private String medioPago;
    private String estado;
    private String reciboNumero;
    private UUID reciboId;
    private String observaciones;
}

