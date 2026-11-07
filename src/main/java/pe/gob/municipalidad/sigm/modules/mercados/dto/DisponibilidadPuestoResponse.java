package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadPuestoResponse {

    private UUID puestoId;
    private String puestoCodigo;
    private String puestoNumero;
    private String puestoEstado;
    private boolean disponible;
    private String motivoNoDisponible;
    private BigDecimal areaMetrosCuadrados;
    private String tipoPuestoNombre;
    private UUID mercadoId;
    private String mercadoNombre;
    private String sectorNombre;
    private String pabellonNombre;
    private String ocupanteActualCodigo;
    private String ocupanteActualNombre;
    private String fechaInicioOcupacion;
    private String fechaFinOcupacion;
}
