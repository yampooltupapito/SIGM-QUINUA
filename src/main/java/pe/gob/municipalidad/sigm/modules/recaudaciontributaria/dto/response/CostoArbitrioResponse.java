package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.response;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.CategoriaCostoArbitrio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoValidacionCosto;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostoArbitrioResponse {
    private UUID id;
    private CategoriaCostoArbitrio categoriaCosto;
    private String categoriaDescripcion;
    private String descripcion;
    private BigDecimal importe;
    private String moneda;
    private String periodoCosto;
    private String documentoSustentoRef;
    private String fuenteSustento;
    private EstadoValidacionCosto estadoValidacion;
    private String estadoBadgeClass;
    private String observaciones;
}
