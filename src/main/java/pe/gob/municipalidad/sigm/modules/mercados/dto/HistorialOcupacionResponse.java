package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialOcupacionResponse {

    private Long id;
    private UUID puestoId;
    private String puestoCodigo;
    private UUID comercianteId;
    private String comercianteCodigo;
    private String titularNombre;
    private UUID asignacionId;
    private String tipoMovimiento;
    private OffsetDateTime fecha;
    private String usuarioResponsable;
    private String motivo;
    private Long documentoSustentoId;
    private String documentoSustentoNombre;
    private String estadoAnterior;
    private String estadoNuevo;
    private String observaciones;
}
