package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.AccionHistorialObligacion;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialObligacionResponse {
    private UUID id;
    private AccionHistorialObligacion accion;
    private String estadoAnterior;
    private String estadoNuevo;
    private BigDecimal importeAnterior;
    private BigDecimal importeNuevo;
    private String motivo;
    private String resolucionSustento;
    private String usuarioRegistro;
    private OffsetDateTime fechaRegistro;
}
