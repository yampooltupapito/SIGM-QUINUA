package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO que representa un registro inmutable en el historial de cambios de estado de un puesto comercial.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PuestoCambioEstadoResponse {
    private Long id;
    private String tipoCambio;
    private String estadoAnterior;
    private String estadoNuevo;
    private String motivo;
    private String usuarioResponsable;
    private String usuarioUsername;
    private LocalDateTime fechaCambio;
}
