package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ComercianteExpedienteResponse {
    private Long id;
    private String numeroExpediente;
    private String asunto;
    private String estado;
    private String tipoTramite;
    private LocalDateTime fechaRegistro;
}
