package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasPlanoResponse {

    private UUID mercadoId;
    private String mercadoNombre;
    private UUID planoId;
    private Integer versionNumero;
    private String versionEstado;

    private long totalPuestos;
    private long puestosDisponibles;
    private long puestosOcupados;
    private long puestosReservados;
    private long puestosInactivos;
    private long puestosRestringidos;

    private long totalSectores;
    private long totalPabellones;
    private long totalZonas;

    private BigDecimal porcentajeOcupacion;
    private BigDecimal porcentajeDisponibilidad;
}
