package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class ComercianteAutorizacionResponse {
    private UUID autorizacionId;
    private String numeroResolucion;
    private String tipoAutorizacion;
    private String estado;
    private LocalDate fechaEmision;
    private LocalDate fechaVigenciaFin;
    private String mercadoNombre;
    private String puestoCodigo;
    private Long documentoId;
}
