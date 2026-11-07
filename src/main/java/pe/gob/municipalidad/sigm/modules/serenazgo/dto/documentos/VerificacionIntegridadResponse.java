package pe.gob.municipalidad.sigm.modules.serenazgo.dto.documentos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.ResultadoVerificacionIntegridad;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionIntegridadResponse {

    private String codigo;
    private String hashRegistrado;
    private String hashCalculado;
    private ResultadoVerificacionIntegridad resultado;
    private String resultadoDesc;
    private String badgeClase;
    private String fechaVerificacion;
    private String detalleTecnico;
    private String verificador;
    private boolean coincide;
}
