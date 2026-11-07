package pe.gob.municipalidad.sigm.modules.serenazgo.dto.documentos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.NivelConfidencialidad;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransicionEstadoRequest {

    private String motivo;
    private String observaciones;
    private NivelConfidencialidad nuevoNivel;
}
