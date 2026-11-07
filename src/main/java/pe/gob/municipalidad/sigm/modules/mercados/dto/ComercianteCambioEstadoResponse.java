package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.Builder;
import lombok.Getter;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;

import java.time.OffsetDateTime;

@Getter
@Builder
public class ComercianteCambioEstadoResponse {
    private Long id;
    private EstadoComerciante estadoAnterior;
    private EstadoComerciante estadoNuevo;
    private String motivo;
    private Long documentoSustentoId;
    private String documentoSustentoCodigo;
    private String observaciones;
    private Long usuarioId;
    private String usuarioUsername;
    private OffsetDateTime fechaCambio;
}
