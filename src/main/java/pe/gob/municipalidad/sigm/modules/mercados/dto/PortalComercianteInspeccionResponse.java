package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoInspeccion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoInspeccionMercado;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalComercianteInspeccionResponse {
    private UUID inspeccionId;
    private String codigoInspeccion;
    private TipoInspeccionMercado tipoInspeccion;
    private String mercadoNombre;
    private String puestoCodigo;
    private LocalDate fechaProgramada;
    private LocalDate fechaEjecucion;
    private EstadoInspeccion estado;
    private String estadoLabel;
    private String calificacionFinal;
    private Integer puntajeTotal;
    private int cantidadHallazgosNotificados;
    private int cantidadMedidasPendientes;
    private List<String> observacionesNotificadas;
}
