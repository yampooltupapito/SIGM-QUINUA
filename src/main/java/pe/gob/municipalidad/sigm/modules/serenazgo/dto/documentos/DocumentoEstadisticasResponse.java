package pe.gob.municipalidad.sigm.modules.serenazgo.dto.documentos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoEstadisticasResponse {

    private long totalDocumentos;
    private long totalFotografias;
    private long totalVideos;
    private long totalEnRevision;
    private long totalValidados;
    private long totalRestringidos;
    private long totalIntegridadNoCoincidente;
    private long totalVerificados;
}
