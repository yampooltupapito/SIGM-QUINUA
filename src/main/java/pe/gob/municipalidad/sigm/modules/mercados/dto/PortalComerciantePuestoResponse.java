package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalComerciantePuestoResponse {
    private UUID puestoId;
    private String codigoPuesto;
    private String numeroPuesto;
    private UUID mercadoId;
    private String mercadoNombre;
    private String sectorNombre;
    private String pabellonNombre;
    private String tipoPuestoNombre;
    private BigDecimal areaM2;
    private EstadoPuesto estadoPuesto;
    private UUID asignacionId;
    private String codigoAsignacion;
    private EstadoAsignacion estadoAsignacion;
    private LocalDate fechaInicioAsignacion;
    private LocalDate fechaFinAsignacion;
    private UUID planoMercadoId;
}
