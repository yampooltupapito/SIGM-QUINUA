package pe.gob.municipalidad.sigm.modules.serenazgo.dto.documentos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoFiltroRequest {

    private String busqueda;
    private TipoDocumentoSeguridad tipoDocumento;
    private EstadoDocumentoSeguridad estado;
    private OrigenDocumentoSeguridad origenDocumento;
    private NivelConfidencialidad nivelConfidencialidad;
    private ResultadoVerificacionIntegridad resultadoIntegridad;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate desde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hasta;

    private UUID incidenciaId;
    private UUID intervencionId;
    private UUID patrullajeId;
}
